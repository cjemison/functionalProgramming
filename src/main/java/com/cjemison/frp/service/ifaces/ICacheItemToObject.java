package com.cjemison.frp.service.ifaces;

import com.cjemison.frp.domain.cache.CacheItem;

import java.util.List;

import reactor.core.publisher.Flux;

public interface ICacheItemToObject {

  <T> List<T> convert(final Flux<CacheItem> flux);
}
