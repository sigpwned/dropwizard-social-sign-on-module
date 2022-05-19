package com.sigpwned.dropwizard.auth.social.twitter.oauth1;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class TwitterOAuth1Configuration {
  @NotNull
  @NotEmpty
  @NotBlank
  private String consumerKey;

  @NotNull
  @NotEmpty
  @NotBlank
  private String consumerSecret;

  /**
   * @return the consumerKey
   */
  public String getConsumerKey() {
    return consumerKey;
  }

  /**
   * @param consumerKey the consumerKey to set
   */
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * @return the consumerSecret
   */
  public String getConsumerSecret() {
    return consumerSecret;
  }

  /**
   * @param consumerSecret the consumerSecret to set
   */
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }
}
