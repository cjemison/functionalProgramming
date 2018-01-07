package com.cjemison.frp.domain.cache;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;
import java.util.Objects;

public class CacheItem implements Serializable {
  private final String id;
  private final String json;
  private final String cacheType;
  private final Long epoch = DateTime.now(DateTimeZone.UTC).getMillis();

  @JsonCreator
  public CacheItem(@JsonProperty("id") final String id,
                   @JsonProperty("json") final String json,
                   @JsonProperty("cacheType") final String cacheType) {
    this.id = id;
    this.json = json;
    this.cacheType = cacheType;
  }

  public String getId() {
    return id;
  }

  public String getJson() {
    return json;
  }

  public String getCacheType() {
    return cacheType;
  }

  public Long getEpoch() {
    return epoch;
  }

  @JsonIgnore
  public String getCacheKey() {
    return String.format("%s.%s", this.getId(), getCacheType());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof CacheItem)) return false;
    final CacheItem cacheItem = (CacheItem) o;
    return Objects.equals(getId(), cacheItem.getId()) &&
          Objects.equals(getCacheType(), cacheItem.getCacheType());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId(), getCacheType());
  }

  @Override
  public String toString() {
    return "CacheItem{" +
          "id='" + id + '\'' +
          ", json='" + json + '\'' +
          ", cacheType='" + cacheType + '\'' +
          ", epoch=" + epoch +
          '}';
  }
}
