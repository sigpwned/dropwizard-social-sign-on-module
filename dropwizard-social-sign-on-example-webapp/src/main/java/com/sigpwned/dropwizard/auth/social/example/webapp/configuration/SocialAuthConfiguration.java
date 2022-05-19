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
package com.sigpwned.dropwizard.auth.social.example.webapp.configuration;

import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1Configuration;

public class SocialAuthConfiguration {
  @Valid
  private TwitterOAuth1Configuration twitter1;

  @Valid
  @JsonProperty("oauthTokenStore")
  private OAuthTokenStoreFactory oauthTokenStore;

  /**
   * @return the twitter1
   */
  public TwitterOAuth1Configuration getTwitter1() {
    return twitter1;
  }

  /**
   * @param twitter1 the twitter1 to set
   */
  public void setTwitterOAuth1(TwitterOAuth1Configuration twitter1) {
    this.twitter1 = twitter1;
  }

  /**
   * @return the tokenStore
   */
  @JsonProperty("oauthTokenStore")
  public OAuthTokenStoreFactory getOAuthTokenStore() {
    return oauthTokenStore;
  }

  /**
   * @param tokenStore the tokenStore to set
   */
  @JsonProperty("oauthTokenStore")
  public void setOAuthTokenStore(OAuthTokenStoreFactory oauthTokenStore) {
    this.oauthTokenStore = oauthTokenStore;
  }
}
