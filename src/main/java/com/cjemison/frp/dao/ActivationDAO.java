package com.cjemison.frp.dao;

import com.cjemison.frp.domain.ActivationDO;

import reactor.core.publisher.Flux;

public interface ActivationDAO {
  String TABLE_NAME = "IFS.ACTIVATION";
  String INDEX_NAME = "uxdIdIndex";
  String ID = "id";
  String UXDID = "uxdid";
  String USER_TYPE = "user_type";
  String USER_NAME = "user_name";
  String USER_INFO = "user_info";
  String EMAIL_ADDRESS = "email_address";
  String EPOCH = "epoch";
  String TTL = "ttl";

  Flux<ActivationDO> get(final String id);

  void store(final Flux<ActivationDO> flux);
}
