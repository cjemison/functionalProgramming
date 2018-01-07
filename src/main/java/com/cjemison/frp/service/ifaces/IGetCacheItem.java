package com.cjemison.frp.service.ifaces;

import com.cjemison.frp.domain.cache.CacheItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IGetCacheItem {

  Flux<CacheItem> get(final Mono<String> id);
}
