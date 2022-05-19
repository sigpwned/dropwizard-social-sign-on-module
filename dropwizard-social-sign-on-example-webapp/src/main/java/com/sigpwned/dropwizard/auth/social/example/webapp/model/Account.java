package com.sigpwned.dropwizard.auth.social.example.webapp.model;

import java.security.Principal;
import java.util.Objects;

public class Account implements Principal {
  public static Account of(String accessToken, String accessTokenSecret) {
    return new Account(accessToken, accessTokenSecret);
  }

  private final String accessToken;

  private final String accessTokenSecret;

  public Account(String accessToken, String accessTokenSecret) {
    this.accessToken = accessToken;
    this.accessTokenSecret = accessTokenSecret;
  }

  /**
   * @return the accessToken
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * @return the accessTokenSecret
   */
  public String getAccessTokenSecret() {
    return accessTokenSecret;
  }

  @Override
  public String getName() {
    return "New User";
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, accessTokenSecret);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Account other = (Account) obj;
    return Objects.equals(accessToken, other.accessToken)
        && Objects.equals(accessTokenSecret, other.accessTokenSecret);
  }

  @Override
  public String toString() {
    return "Account [accessToken=" + accessToken + ", accessTokenSecret=" + accessTokenSecret + "]";
  }
}
