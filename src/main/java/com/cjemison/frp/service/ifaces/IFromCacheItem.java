package com.cjemison.frp.service.ifaces;

import com.cjemison.frp.domain.cache.CacheItem;

import java.util.Optional;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IFromCacheItem {

  <T> Mono<Optional<T>> from(final Mono<CacheItem> mono,
                             final Class<T> tClass);
}
