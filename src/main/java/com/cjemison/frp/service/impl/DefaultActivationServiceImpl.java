package com.cjemison.frp.service.impl;

import com.cjemison.frp.domain.ActivationDO;
import com.cjemison.frp.domain.ActivationVO;
import com.cjemison.frp.service.IActivationService;
import com.cjemison.frp.service.ifaces.IValidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class DefaultActivationServiceImpl implements IActivationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActivationServiceImpl.class);


  @Override
  public Mono<ActivationDO> store(final IValidation<ActivationVO> iValidation, final
  Mono<ActivationVO> mono) {
    return null;
  }
}
