package com.cjemison.frp.handler.impl;

import com.cjemison.frp.dao.ActivationDAO;
import com.cjemison.frp.domain.ActivationDO;
import com.cjemison.frp.domain.ActivationVO;
import com.cjemison.frp.domain.cache.CacheItem;
import com.cjemison.frp.handler.ActivationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ActivationHandlerImpl implements ActivationHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivationHandlerImpl.class);
  private final ObjectMapper objectMapper;
  private final JedisPool jedisPool;
  private final ActivationDAO activationDAO;

  public ActivationHandlerImpl(final ObjectMapper objectMapper,
                               final JedisPool jedisPool, final ActivationDAO activationDAO) {
    this.objectMapper = objectMapper;
    this.jedisPool = jedisPool;
    this.activationDAO = activationDAO;
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public Mono<ServerResponse> get(final ServerRequest serverRequest) {
    final String uxdId = serverRequest.pathVariable("uxdId");
    if (StringUtils.isNotBlank(uxdId)) {
      final Flux<ActivationDO> flux = Flux.create(activationDOFluxSink -> {
        LOGGER.debug("uri: {} uxdId: {}", serverRequest.uri(), uxdId);
        final Jedis jedis = jedisPool.getResource();
        final String key = String.format("%s.%s", uxdId.trim(), ActivationDO.class.getName());
        final Set<String> set = jedis.smembers(key);
        jedis.close();
        if (CollectionUtils.isNotEmpty(set)) {
          for (String s : set) {
            String value = new String(Base64.getDecoder().decode(s));
            try {
              CacheItem cacheItem = objectMapper.readValue(value, CacheItem
                    .class);
              final ActivationDO activationDO = objectMapper
                    .readValue(Base64.getDecoder().decode(cacheItem.getJson()),
                          ActivationDO.class);
              activationDOFluxSink.next(activationDO);
            } catch (Exception e) {
              LOGGER.error("### Error - {}###", e);
            }
          }
        } else {
          activationDAO.get(uxdId).subscribe(activationDO -> {
            cache(activationDO);
            activationDOFluxSink.next(activationDO);
          });
        }
        activationDOFluxSink.complete();
      });
      flux.subscribeOn(Schedulers.elastic())
            .timeout(Duration.ofSeconds(30));
      return ServerResponse.ok().body(flux
            .sort(Comparator.comparing(ActivationDO::getEpoch)), ActivationDO.class);
    }
    return Mono.empty();
  }

  @Override
  public Mono<ServerResponse> store(final ServerRequest serverRequest) {
    return serverRequest.bodyToMono(ActivationVO.class)
          .subscribeOn(Schedulers.elastic())
          .timeout(Duration.ofSeconds(30))
          .flatMap(activationVO -> {
            LOGGER.debug("URI: {}", serverRequest.uri());
            LOGGER.debug("ActivationVO: {}", activationVO);
            Objects.requireNonNull(activationVO);
            if (StringUtils.isBlank(activationVO.getUxdId())) {
              throw new IllegalStateException("activationVO.uxdId is null or empty.");
            }

            if (StringUtils.isBlank(activationVO.getUserType())) {
              throw new IllegalStateException("activationVO.userType is null or empty.");
            }
            return Mono.just(activationVO);
          }).flatMap(activationVO -> {
            final ActivationDO activationDO = new ActivationDO(
                  activationVO.getUxdId(),
                  activationVO.getUserType(),
                  activationVO.getUserName(),
                  activationVO.getUserInfo(),
                  activationVO.getEmailAddress(),
                  DateTime.now(DateTimeZone.UTC).getMillis());
            LOGGER.debug("ActivationDO: {}", activationDO);
            return Mono.just(activationDO);
          }).flatMap(activationDO -> {
            cache(activationDO);
            activationDAO.store(Flux.just(activationDO));
            return Mono.just(activationDO);
          })
          .flatMap(activationDO -> ServerResponse.created(serverRequest.uri())
                .syncBody(activationDO)).onErrorResume(throwable -> ServerResponse.badRequest()
                .build());
  }

  private void cache(final ActivationDO activationDO) {
    String json = StringUtils.EMPTY;
    try {
      json = objectMapper.writeValueAsString(activationDO);
    } catch (IOException e) {
      LOGGER.error("### Error {} ###", e);
    }

    if (StringUtils.isBlank(json)) {
      throw new IllegalStateException("Couldn't generate json for ActivationDO");
    }

    final CacheItem cacheItem = new CacheItem(activationDO.getUxdId(), Base64.getEncoder
          ().encodeToString(json.getBytes()), activationDO
          .getClass().getName());

    String cacheItemJson = StringUtils.EMPTY;
    try {
      cacheItemJson = objectMapper.writeValueAsString(cacheItem);
    } catch (IOException e) {
      LOGGER.error("### Error {} ###", e);
    }

    if (StringUtils.isBlank(cacheItemJson)) {
      throw new IllegalStateException("Couldn't generate json for CacheItem");
    }
    final Jedis jedis = jedisPool.getResource();

    jedis.sadd(cacheItem.getCacheKey(), Base64.getEncoder().encodeToString(cacheItemJson
          .getBytes()));

    jedis.expire(cacheItem.getCacheKey(), 60 * 5);
    jedis.close();
  }
}
