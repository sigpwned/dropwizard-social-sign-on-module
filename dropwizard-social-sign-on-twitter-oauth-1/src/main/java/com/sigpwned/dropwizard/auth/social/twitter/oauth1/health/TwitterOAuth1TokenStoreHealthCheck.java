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
package com.sigpwned.dropwizard.auth.social.twitter.oauth1.health;

import com.codahale.metrics.health.HealthCheck;
import com.sigpwned.dropwizard.auth.social.linting.Generated;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1TokenStore;

/**
 * You should always make sure your external dependencies are healthy.
 */
public class TwitterOAuth1TokenStoreHealthCheck extends HealthCheck {
  public static final String NAME = "TwitterOAuth1TokenStoreHealthCheck";

  private final TwitterOAuth1TokenStore oauthTokenStore;

  public TwitterOAuth1TokenStoreHealthCheck(TwitterOAuth1TokenStore oauthTokenStore) {
    this.oauthTokenStore = oauthTokenStore;
  }

  @Override
  protected Result check() throws Exception {
    try {
      // We don't care if the credentials work, only that we don't get an exception.
      getOAuthTokenStore().getTwitterOAuth1TokenSecret("example");
      return Result.healthy();
    } catch (Exception e) {
      return Result.unhealthy(e);
    }
  }

  /**
   * @return the accessTokenStore
   */
  @Generated
  private TwitterOAuth1TokenStore getOAuthTokenStore() {
    return oauthTokenStore;
  }
}
