package com.cjemison.frp.service;

import com.cjemison.frp.domain.ActivationDO;
import com.cjemison.frp.domain.ActivationVO;
import com.cjemison.frp.service.ifaces.IValidation;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IActivationService {

  Mono<ActivationDO> store(final IValidation<ActivationVO> iValidation,
                           final Mono<ActivationVO> mono);
}
