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
package com.sigpwned.dropwizard.auth.social.example.webapp.store.oauth;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.sigpwned.dropwizard.auth.social.example.webapp.OAuthTokenStore;

public class DefaultOAuthTokenStore implements OAuthTokenStore {
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

  private final Map<String, String> tokens;

  public DefaultOAuthTokenStore(String username, String password) {
    this.username = username;
    this.password = password;
    this.tokens = new ConcurrentHashMap<>();
  }

  @Override
  public void putTwitterOAuth1TokenSecret(String token, String tokenSecret) throws IOException {
    tokens.put(token, tokenSecret);
  }

  @Override
  public Optional<String> getTwitterOAuth1TokenSecret(String token) throws IOException {
    return Optional.ofNullable(tokens.get(token));
  }
}
