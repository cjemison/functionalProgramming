package com.cjemison.frp.service.ifaces;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IValidation<T> {
  Mono<T> validate(final Mono<T> mono);
}
