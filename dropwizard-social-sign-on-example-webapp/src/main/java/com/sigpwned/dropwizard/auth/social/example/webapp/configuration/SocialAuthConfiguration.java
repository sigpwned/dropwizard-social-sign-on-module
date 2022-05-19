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
