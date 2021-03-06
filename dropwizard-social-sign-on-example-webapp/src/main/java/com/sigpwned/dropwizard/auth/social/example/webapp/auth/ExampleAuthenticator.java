/*-
 * =================================LICENSE_START==================================
 * dropwizard-jose-jwt-example-webapp
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
package com.sigpwned.dropwizard.auth.social.example.webapp.auth;

import java.util.Optional;
import javax.inject.Inject;
import com.sigpwned.dropwizard.auth.social.example.webapp.SessionStore;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.TwitterAccount;
import com.sigpwned.dropwizard.auth.social.linting.Generated;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

/**
 * We treat access tokens as session tokens here. In practice, you'd want to use some proper session
 * token or a JWT, but for our little demo here, it'll do just fine.
 */
public class ExampleAuthenticator implements Authenticator<String, TwitterAccount> {
  private final SessionStore store;

  @Inject
  @Generated
  public ExampleAuthenticator(SessionStore store) {
    this.store = store;
  }

  /**
   * We accept an access token if we have an existing session for it.
   */
  @Override
  public Optional<TwitterAccount> authenticate(String accessToken) throws AuthenticationException {
    // Let's see if we can't find our access token secret
    TwitterAccount me;
    try {
      me = getStore().getSession(accessToken).orElse(null);
    } catch (Exception e) {
      throw new AuthenticationException("Failed to communicate with session store", e);
    }
    return Optional.ofNullable(me);
  }

  /**
   * @return the store
   */
  @Generated
  private SessionStore getStore() {
    return store;
  }
}
