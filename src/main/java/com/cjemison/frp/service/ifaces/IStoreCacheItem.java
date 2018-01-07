package com.cjemison.frp.service.ifaces;

import com.cjemison.frp.domain.cache.CacheItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IStoreCacheItem {

  <T> Flux<CacheItem> storeCache(final IToCacheItem iToCacheItem,
                                 final Mono<T> tMono);
}
