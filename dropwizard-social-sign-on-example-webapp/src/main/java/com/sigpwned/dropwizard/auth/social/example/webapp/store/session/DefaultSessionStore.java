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
package com.sigpwned.dropwizard.auth.social.example.webapp.store.session;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.sigpwned.dropwizard.auth.social.example.webapp.SessionStore;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.TwitterAccount;

/**
 * Normally you'd use some kind of shared storage, like a database or redis, but for an example
 * webapp, this will do just fine.
 */
public class DefaultSessionStore implements SessionStore {
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

  private final ConcurrentMap<String, TwitterAccount> sessions;

  public DefaultSessionStore(String username, String password) {
    this.username = username;
    this.password = password;
    this.sessions = new ConcurrentHashMap<>();
  }

  @Override
  public void putSession(String token, TwitterAccount session) {
    sessions.put(token, session);
  }

  @Override
  public Optional<TwitterAccount> getSession(String token) {
    return Optional.ofNullable(sessions.get(token));
  }
}
