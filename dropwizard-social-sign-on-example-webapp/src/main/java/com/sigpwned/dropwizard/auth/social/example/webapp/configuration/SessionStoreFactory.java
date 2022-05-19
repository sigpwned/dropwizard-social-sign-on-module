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
package com.sigpwned.dropwizard.auth.social.example.webapp.configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigpwned.dropwizard.auth.social.example.webapp.store.session.DefaultSessionStore;

/**
 * Our session store is in-memory, so obviously we don't need this. However, this demonstrates how
 * the user might make configuration data available to the bundle at initialization time.
 */
public class SessionStoreFactory {
  /**
   * Username to access database
   */
  @NotNull
  @NotEmpty
  @NotBlank
  private String username;

  /**
   * Password to access database
   */
  @NotNull
  @NotEmpty
  @NotBlank
  private String password;

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  @JsonIgnore
  private DefaultSessionStore instance;

  public DefaultSessionStore build() {
    if (instance == null)
      instance = new DefaultSessionStore(getUsername(), getPassword());
    return instance;
  }
}
