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
package com.sigpwned.dropwizard.auth.social.example.webapp.store.access;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sigpwned.dropwizard.auth.social.example.webapp.AccessTokenStore;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.TwitterOAuth1AccessToken;

public class DefaultAccessTokenStore implements AccessTokenStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAccessTokenStore.class);

  /**
   * This is an in-memory store, so we don't need this, but it shows how to pass configuration data
   * through the app.
   */
  @SuppressWarnings("unused")
  private final String username;

  /**
   * This is an in-memory store, so we don't need this, but it shows how to pass configuration data
   * through the app.
   */
  @SuppressWarnings("unused")
  private final String password;

  private final Map<String, TwitterOAuth1AccessToken> tokens;

  public DefaultAccessTokenStore(String username, String password) {
    this.username = username;
    this.password = password;
    this.tokens = new ConcurrentHashMap<>();
  }

  @Override
  public void putTwitterOAuth1AccessToken(long userId, String accessToken,
      String accessTokenSecret) {
    TwitterOAuth1AccessToken token =
        TwitterOAuth1AccessToken.of(userId, accessToken, accessTokenSecret);
    if (LOGGER.isInfoEnabled())
      LOGGER.info("Storing Twitter OAuth 1.0a access token {}", token);
    tokens.put(accessToken, token);
  }

  @Override
  public Optional<TwitterOAuth1AccessToken> getTwitterOAuth1AccessTokenSecret(String accessToken) {
    return Optional.ofNullable(tokens.get(accessToken));
  }
}
