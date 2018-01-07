package com.cjemison.frp.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

public interface ActivationHandler {

  Mono<ServerResponse> get(final ServerRequest serverRequest);

  Mono<ServerResponse> store(final ServerRequest serverRequest);
}
