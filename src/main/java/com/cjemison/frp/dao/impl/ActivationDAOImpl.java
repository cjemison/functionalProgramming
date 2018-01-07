package com.cjemison.frp.dao.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.cjemison.frp.dao.ActivationDAO;
import com.cjemison.frp.domain.ActivationDO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ActivationDAOImpl implements ActivationDAO {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivationDAOImpl.class);
  private AmazonDynamoDBClient amazonDynamoDBClient;

  public ActivationDAOImpl(final AmazonDynamoDBClient amazonDynamoDBClient) {
    this.amazonDynamoDBClient = amazonDynamoDBClient;
  }

  @Override
  public Flux<ActivationDO> get(final String id) {
    LOGGER.debug("id: {}", id);
    return DynamodbFunction.get(id,
          ActivationDAO.TABLE_NAME,
          ActivationDAO.INDEX_NAME,
          ActivationDAO.UXDID,
          new DynamoDB(amazonDynamoDBClient),
          DynamodbFunction.defaultQuerySpecFunction(),
          DynamodbFunction.defaultTableFunction(),
          DynamodbFunction.defaultIndexFunction()).flatMap(item -> {
      final ActivationDO activationDO = new ActivationDO(
            item.getString(ActivationDAO.UXDID),
            item.getString(ActivationDAO.USER_TYPE),
            item.getString(ActivationDAO.USER_NAME),
            item.getString(ActivationDAO.USER_INFO),
            item.getString(ActivationDAO.EMAIL_ADDRESS),
            item.getLong(ActivationDAO.EPOCH));
      return Mono.just(activationDO);
    });
  }

  @Override
  @Async
  public void store(final Flux<ActivationDO> flux) {
    DynamodbFunction.store(ActivationDAO.TABLE_NAME,
          flux.flatMap(activationDO -> {
            final Item item = new Item()
                  .withPrimaryKey(ActivationDAO.ID, UUID.randomUUID().toString())
                  .withString(ActivationDAO.UXDID, activationDO.getUxdId().trim())
                  .withString(ActivationDAO.USER_TYPE, activationDO.getUserType().trim())
                  .withString(ActivationDAO.USER_NAME, activationDO.getUserName().trim())
                  .withString(ActivationDAO.USER_INFO, activationDO.getUserInfo().trim())
                  .withString(ActivationDAO.EMAIL_ADDRESS, activationDO.getEmailAddress().trim())
                  .withNumber(ActivationDAO.EPOCH, DateTime.now(DateTimeZone.UTC).getMillis())
                  .withNumber(ActivationDAO.TTL, DateTime.now(DateTimeZone.UTC).plusHours(24 * 7)
                        .getMillis());
            return Mono.just(item);
          }),
          (item) -> new PutItemSpec().withItem(item).withReturnValues(ReturnValue.ALL_OLD),
          new DynamoDB(amazonDynamoDBClient),
          DynamodbFunction.defaultTableFunction());
  }
}
