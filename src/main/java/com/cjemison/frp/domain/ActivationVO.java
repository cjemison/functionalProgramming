package com.cjemison.frp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivationVO {
  @JsonProperty("uxdId")
  private final String uxdId;
  @JsonProperty("user_type")
  private final String userType;
  @JsonProperty("username")
  private final String userName;
  @JsonProperty("user_info")
  private final String userInfo;
  @JsonProperty("email_address")
  private final String emailAddress;

  @JsonCreator
  public ActivationVO(@JsonProperty("uxdId") final String uxdId,
                      @JsonProperty("user_type") final String userType,
                      @JsonProperty("username") final String userName,
                      @JsonProperty("user_info") final String userInfo,
                      @JsonProperty("email_address") final String emailAddress) {
    this.uxdId = uxdId;
    this.userType = userType;
    this.userName = userName;
    this.userInfo = userInfo;
    this.emailAddress = emailAddress;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof ActivationVO)) return false;
    final ActivationVO that = (ActivationVO) o;
    return Objects.equals(getUxdId(), that.getUxdId());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getUxdId());
  }

  @Override
  public String toString() {
    return "ActivationVO{" +
          "uxdId='" + uxdId + '\'' +
          ", userType='" + userType + '\'' +
          ", userName='" + userName + '\'' +
          ", userInfo='" + userInfo + '\'' +
          ", emailAddress='" + emailAddress + '\'' +
          '}';
  }
}
