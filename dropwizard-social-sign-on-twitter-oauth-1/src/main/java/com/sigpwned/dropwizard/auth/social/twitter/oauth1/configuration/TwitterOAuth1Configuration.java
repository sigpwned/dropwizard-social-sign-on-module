/*-
 * =================================LICENSE_START==================================
 * dropwizard-social-sign-on
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
package com.sigpwned.dropwizard.auth.social.twitter.oauth1.configuration;

import com.sigpwned.dropwizard.auth.social.linting.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
  @Generated
  public String getConsumerKey() {
    return consumerKey;
  }

  /**
   * @param consumerKey the consumerKey to set
   */
  @Generated
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * @return the consumerSecret
   */
  @Generated
  public String getConsumerSecret() {
    return consumerSecret;
  }

  /**
   * @param consumerSecret the consumerSecret to set
   */
  @Generated
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }
}
