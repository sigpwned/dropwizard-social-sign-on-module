/*-
 * =================================LICENSE_START==================================
 * oauth4j-core
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
package com.sigpwned.dropwizard.auth.social.util;

public final class OAuth1 {
  private OAuth1() {}

  public static final String POST_OAUTH_HTTP_METHOD = "POST";

  public static final String OAUTH_CONSUMER_KEY_NAME = "oauth_consumer_key";

  public static final String OAUTH_NONCE_NAME = "oauth_nonce";

  public static final String OAUTH_SIGNATURE_METHOD_NAME = "oauth_signature_method";

  public static final String HMAC_SHA1_OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";

  public static final String OAUTH_TIMESTAMP = "oauth_timestamp";

  public static final String OAUTH_TOKEN_NAME = "oauth_token";

  public static final String OAUTH_TOKEN_SECRET_NAME = "oauth_token_secret";

  public static final String OAUTH_VERSION_NAME = "oauth_version";

  public static final String ONE_DOT_OH_OAUTH_VERSION_VALUE = "1.0";

  public static final String OAUTH_SIGNATURE_NAME = "oauth_signature";

  public static final String OAUTH_CALLBACK_NAME = "oauth_callback";

  public static final String OAUTH_VERIFIER_NAME = "oauth_verifier";
}
