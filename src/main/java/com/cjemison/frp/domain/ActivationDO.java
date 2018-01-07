package com.cjemison.frp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class ActivationDO implements Serializable {

  private final String uxdId;
  private final String userType;
  private final String userName;
  private final String userInfo;
  private final String emailAddress;
  private final Long epoch;

  @JsonCreator
  public ActivationDO(@JsonProperty("uxdId") final String uxdId,
                      @JsonProperty("userType") final String userType,
                      @JsonProperty("userName") final String userName,
                      @JsonProperty("userInfo") final String userInfo,
                      @JsonProperty("emailAddress") final String emailAddress,
                      @JsonProperty("epoch") final Long epoch) {
    this.uxdId = uxdId;
    this.userType = userType;
    this.userName = userName;
    this.userInfo = userInfo;
    this.emailAddress = emailAddress;
    this.epoch = epoch;
  }

  public String getUxdId() {
    return uxdId;
  }

  public String getUserType() {
    return userType;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserInfo() {
    return userInfo;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public Long getEpoch() {
    return epoch;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof ActivationDO)) return false;
    final ActivationDO that = (ActivationDO) o;
    return Objects.equals(getUxdId(), that.getUxdId()) &&
          Objects.equals(getUserType(), that.getUserType()) &&
          Objects.equals(getUserName(), that.getUserName()) &&
          Objects.equals(getUserInfo(), that.getUserInfo()) &&
          Objects.equals(getEmailAddress(), that.getEmailAddress());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getUxdId(), getUserType(), getUserName(), getUserInfo(), getEmailAddress());
  }

  @Override
  public String toString() {
    return "ActivationDO{" +
          "uxdId='" + uxdId + '\'' +
          ", userType='" + userType + '\'' +
          ", userName='" + userName + '\'' +
          ", userInfo='" + userInfo + '\'' +
          ", emailAddress='" + emailAddress + '\'' +
          ", epoch=" + epoch +
          '}';
  }
}
