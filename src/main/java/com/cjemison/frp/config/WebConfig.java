package com.cjemison.frp.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.cjemison.frp.dao.ActivationDAO;
import com.cjemison.frp.dao.impl.ActivationDAOImpl;
import com.cjemison.frp.handler.ActivationHandler;
import com.cjemison.frp.handler.impl.ActivationHandlerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;

import javax.annotation.PreDestroy;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@ComponentScan(basePackages = "com.cjemison.frp")
@EnableAsync
@EnableWebFlux
public class WebConfig implements InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);
  private JedisPool jedisPool;

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public JedisPool jedisPool() {
    final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxWaitMillis(1000 * 60 * 5);

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(70);
    poolConfig.setMaxIdle(10);
    poolConfig.setMinIdle(5);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    poolConfig.setNumTestsPerEvictionRun(250);
    poolConfig.setTimeBetweenEvictionRunsMillis(15000);
    jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
    return jedisPool;
  }

  @Bean
  public ActivationHandler activationHandler(final ObjectMapper objectMapper,
                                             final JedisPool jedisPool,
                                             final ActivationDAO activationDAO) {
    return new ActivationHandlerImpl(objectMapper, jedisPool, activationDAO);
  }


  @Bean
  public AmazonDynamoDBClient amazonDynamoDBClient() {
    final AmazonDynamoDBClient client = new AmazonDynamoDBClient();
    client.withEndpoint("http://localhost:8000");
    return client;
  }

  @Bean
  public ActivationDAO activationDAO(final AmazonDynamoDBClient amazonDynamoDBClient) {
    return new ActivationDAOImpl(amazonDynamoDBClient);
  }


  @Bean
  public RouterFunction<ServerResponse> routerFunction(final ActivationHandler
                                                             activationHandler) {
    return nest(path("/activation").and(accept(MediaType.APPLICATION_JSON)),
          route(GET("/{uxdId}"), activationHandler::get)
                .andRoute(POST(""), activationHandler::store)
    );
  }


  @Bean
  public Executor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

  @PreDestroy
  public void tearDown() {
    jedisPool.destroy();
  }

  @Override
  public void afterPropertiesSet() {
    final ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
    keySchema.add(new KeySchemaElement()
          .withAttributeName(ActivationDAO.ID)
          .withKeyType(KeyType.HASH));

    final ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
    attributeDefinitions.add(new AttributeDefinition()
          .withAttributeName(ActivationDAO.ID)
          .withAttributeType("S"));

    attributeDefinitions.add(new AttributeDefinition()
          .withAttributeName(ActivationDAO.UXDID)
          .withAttributeType("S"));

    final GlobalSecondaryIndex globalSecondaryIndex = new GlobalSecondaryIndex()
          .withIndexName("uxdIdIndex")
          .withProvisionedThroughput(new ProvisionedThroughput()
                .withReadCapacityUnits(15l)
                .withWriteCapacityUnits(15l))
          .withProjection(new Projection().withProjectionType(ProjectionType.ALL));

    globalSecondaryIndex.setKeySchema(Arrays.asList(new KeySchemaElement()
          .withAttributeName(ActivationDAO.UXDID)
          .withKeyType(KeyType.HASH)));

    final CreateTableRequest request = new CreateTableRequest()
          .withTableName(ActivationDAO.TABLE_NAME)
          .withKeySchema(keySchema)
          .withProvisionedThroughput(new ProvisionedThroughput()
                .withReadCapacityUnits(20l)
                .withWriteCapacityUnits(20l))
          .withAttributeDefinitions(attributeDefinitions)
          .withGlobalSecondaryIndexes(Collections.singletonList(globalSecondaryIndex));

    try {
      final DynamoDB dynamoDB = new DynamoDB(amazonDynamoDBClient());
      dynamoDB.createTable(request);
      final Table table = dynamoDB.getTable(ActivationDAO.TABLE_NAME);
      table.waitForActive();
    } catch (Exception e) {
      if (!(e instanceof ResourceInUseException)) {
        LOGGER.warn("### CREATE TABLE ###", e);
      }
    }

  }
}
