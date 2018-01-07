package com.cjemison.frp.dao.impl;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import reactor.core.publisher.Flux;


public class DynamodbFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamodbFunction.class);

  public static QuerySpecFunction defaultQuerySpecFunction() {
    return (_id, _columnName) -> {

      if (StringUtils.isBlank(_id)) {
        throw new IllegalStateException("id is null or empty.");
      }

      if (StringUtils.isBlank(_columnName)) {
        throw new IllegalStateException("columnName is null or empty.");
      }

      final QuerySpec querySpec = new QuerySpec()
            .withKeyConditionExpression(String.format("#%s = :%s", _columnName.trim(), _id
                  .trim()));

      final String bindKey = String.format("#%s", _columnName.trim());
      final String bindValue = String.format(":%s", _columnName.trim());

      final NameMap nameMap = new NameMap().with(bindKey, _columnName.trim());
      final ValueMap valueMap = new ValueMap().with(bindValue, _id.trim());

      querySpec.withNameMap(nameMap);
      querySpec.withValueMap(valueMap);
      return querySpec;
    };
  }

  public static TableFunction defaultTableFunction() {
    return (_tableName, _dynamoDB) -> {
      if (StringUtils.isBlank(_tableName)) {
        throw new IllegalStateException("tableName is null or empty.");
      }
      Objects.requireNonNull(_dynamoDB, "dynamoDB is null.");
      return _dynamoDB.getTable(_tableName.trim());
    };
  }

  public static IndexFunction defaultIndexFunction() {
    return (_indexName, _table) -> {
      if (StringUtils.isBlank(_indexName)) {
        throw new IllegalStateException("indexName is null or empty.");
      }
      Objects.requireNonNull(_table, "table is null.");

      return _table.getIndex(_indexName.trim());
    };
  }

  public static Flux<Item> get(final String id,
                               final String tableName,
                               final String indexName,
                               final String columnName,
                               final DynamoDB dynamoDB,
                               final QuerySpecFunction querySpecFunction,
                               final TableFunction tableFunction,
                               final IndexFunction indexFunction) {
    LOGGER.debug("id: {} columnName: {} tableName: {}", id, columnName, tableName);

    return Flux.create(itemFluxSink -> {
      try {
        if (StringUtils.isBlank(columnName)) {
          throw new IllegalStateException("columnName is null or empty.");
        }
        Objects.requireNonNull(dynamoDB, "dynamoDB is null.");

        final ItemCollection<QueryOutcome> itemCollection = indexFunction.get(indexName,
              tableFunction.get(tableName, dynamoDB)).query(querySpecFunction
              .createQuerySpec(id, columnName));
        if (itemCollection != null) {
          itemCollection.forEach(itemFluxSink::next);
        }
      } catch (Exception e) {
        LOGGER.error("### Error - {} ###", e);
        throw new RuntimeException(e);
      }
      itemFluxSink.complete();
    });
  }

  public static void store(final String tableName,
                           final Flux<Item> flux,
                           final PutItemSpecFunction putItemSpecFunction,
                           final DynamoDB dynamoDB,
                           final TableFunction tableFunction) {

    if (StringUtils.isBlank(tableName)) {
      throw new IllegalStateException("tableName is null or empty.");
    }
    Objects.requireNonNull(flux, "flux is null.");
    Objects.requireNonNull(putItemSpecFunction, "putItemSpecFunction is null.");
    Objects.requireNonNull(dynamoDB, "dynamoDB is null.");

    flux.subscribe(item -> {
      try {
        tableFunction.get(tableName.trim(), dynamoDB).putItem(putItemSpecFunction.get(item));
      } catch (Exception e) {
        LOGGER.error("### Error - {} ###", e);
        throw new RuntimeException(e);
      }
    });
  }

  @FunctionalInterface
  public interface TableFunction {
    Table get(final String tableName,
              final DynamoDB dynamoDB);
  }

  @FunctionalInterface
  public interface IndexFunction {
    Index get(final String indexName,
              final Table table);
  }

  @FunctionalInterface
  public interface PutItemSpecFunction {
    PutItemSpec get(final Item item);
  }

  @FunctionalInterface
  public interface QuerySpecFunction {
    QuerySpec createQuerySpec(final String id,
                              final String columnHeader);
  }
}
