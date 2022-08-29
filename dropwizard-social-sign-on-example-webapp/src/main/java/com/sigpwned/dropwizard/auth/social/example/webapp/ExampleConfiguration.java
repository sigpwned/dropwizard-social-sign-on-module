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
package com.sigpwned.dropwizard.auth.social.example.webapp;

import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sigpwned.dropwizard.auth.social.example.webapp.configuration.AccessTokenStoreFactory;
import com.sigpwned.dropwizard.auth.social.example.webapp.configuration.SessionStoreFactory;
import com.sigpwned.dropwizard.auth.social.example.webapp.configuration.SocialAuthConfiguration;
import com.sigpwned.dropwizard.auth.social.linting.Generated;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1BundleConfiguration;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.configuration.TwitterOAuth1Configuration;
import io.dropwizard.core.Configuration;

public class ExampleConfiguration extends Configuration
    implements TwitterOAuth1BundleConfiguration {
  @Valid
  private SocialAuthConfiguration socialAuth;

  @Valid
  private AccessTokenStoreFactory accessTokenStore;

  @Valid
  private SessionStoreFactory sessionStore;

  /**
   * @return the socialAuth
   */
  @Generated
  public SocialAuthConfiguration getSocialAuth() {
    return socialAuth;
  }

  /**
   * @param socialAuth the socialAuth to set
   */
  @Generated
  public void setSocialAuth(SocialAuthConfiguration socialAuth) {
    this.socialAuth = socialAuth;
  }

  /**
   * @return the accessTokenStore
   */
  @Generated
  public AccessTokenStoreFactory getAccessTokenStore() {
    return accessTokenStore;
  }

  /**
   * @param accessTokenStore the accessTokenStore to set
   */
  @Generated
  public void setAccessTokenStore(AccessTokenStoreFactory accessTokenStore) {
    this.accessTokenStore = accessTokenStore;
  }

  /**
   * @return the sessionStore
   */
  @Generated
  public SessionStoreFactory getSessionStore() {
    return sessionStore;
  }

  /**
   * @param sessionStore the sessionStore to set
   */
  @Generated
  public void setSessionStore(SessionStoreFactory sessionStore) {
    this.sessionStore = sessionStore;
  }

  @Override
  @JsonIgnore
  public TwitterOAuth1Configuration getTwitterOAuth1Configuration() {
    return getSocialAuth().getTwitter1();
  }
}
