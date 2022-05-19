/*-
 * =================================LICENSE_START==================================
 * dropwizard-social-sign-on-example-webapp
 * ====================================SECTION=====================================
 * Copyright (C) 2022 Andy Boothe
 * ====================================SECTION=====================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================LICENSE_END===================================
 */
package com.sigpwned.dropwizard.auth.social.example.webapp.model;

import java.util.Objects;

/**
 * Models a Twitter OAuth 1.0a access token with its owning user's ID
 */
public class TwitterOAuth1AccessToken {
  public static TwitterOAuth1AccessToken of(long userId, String accessToken,
      String accessTokenSecret) {
    return new TwitterOAuth1AccessToken(userId, accessToken, accessTokenSecret);
  }

  private final long userId;
  private final String accessToken;
  private final String accessTokenSecret;

  public TwitterOAuth1AccessToken(long userId, String accessToken, String accessTokenSecret) {
    this.userId = userId;
    this.accessToken = accessToken;
    this.accessTokenSecret = accessTokenSecret;
  }

  /**
   * @return the userId
   */
  public long getUserId() {
    return userId;
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
  public int hashCode() {
    return Objects.hash(accessToken, accessTokenSecret, userId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TwitterOAuth1AccessToken other = (TwitterOAuth1AccessToken) obj;
    return Objects.equals(accessToken, other.accessToken)
        && Objects.equals(accessTokenSecret, other.accessTokenSecret) && userId == other.userId;
  }

  @Override
  public String toString() {
    return "TwitterAccessToken [userId=" + userId + ", accessToken=" + accessToken
        + ", accessTokenSecret=" + accessTokenSecret + "]";
  }
}
