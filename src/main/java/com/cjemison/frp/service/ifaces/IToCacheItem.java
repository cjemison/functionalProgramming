package com.cjemison.frp.service.ifaces;

import com.cjemison.frp.domain.cache.CacheItem;

import reactor.core.publisher.Mono;

public interface IToCacheItem {

  <T> Mono<CacheItem> toCache(final Mono<T> flux);
}
