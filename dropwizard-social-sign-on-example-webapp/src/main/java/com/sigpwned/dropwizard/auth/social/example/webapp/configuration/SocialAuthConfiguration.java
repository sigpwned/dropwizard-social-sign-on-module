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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.configuration.TwitterOAuth1Configuration;

public class SocialAuthConfiguration {
  @Valid
  private TwitterOAuth1Configuration twitter1;

  @Valid
  @JsonProperty("oauthTokenStore")
  private OAuthTokenStoreFactory oauthTokenStore;


  /**
   * The base URL of the application to use. It must be a full absolute URL that does not end with a
   * slash. This is used to generate OAuth callback URLs. In general, if your domain is hosted on
   * www.example.com, then you should just use "https://www.example.com", which would map your
   * callback URLs to, e.g., "https://www.example.com/oauth/twitter/1/callback". However, if you
   * want to use a different prefix, you can add it to the end of this value. For example,
   * "https://www.example.com/prefix/goes/here" would change the callback URL to
   * "https://www.example.com/prefix/goes/here/oauth/twitter/1/callback".
   * 
   * Example: http://www.example.com
   */
  @NotNull
  @Pattern(regexp = "^https?://[-a-zA-Z0-9.]+(?:/[^/]+)*")
  private String baseUrl;

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

  /**
   * @return the baseUrl
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * @param baseUrl the baseUrl to set
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
