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
package com.sigpwned.dropwizard.socialsignon.twitter.oauth1.authorizer;

import static java.util.stream.Collectors.joining;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import com.sigpwned.dropwizard.socialsignon.core.util.OAuth;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.TwitterOAuth1HttpRequestAuthorizer;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.TwitterOAuth1HttpRequestSigner;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.signer.HmacSha1OAuthHttpRequestSigner;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.util.Parameter;
import com.sigpwned.httpmodel.ModelHttpHeader;
import com.sigpwned.httpmodel.ModelHttpRequest;
import com.sigpwned.httpmodel.util.ModelHttpEncodings;
import com.sigpwned.httpmodel.util.ModelHttpHeaderNames;

public class DefaultTwitterOAuth1HttpRequestAuthorizer implements TwitterOAuth1HttpRequestAuthorizer {
  public static final String DEFAULT_OAUTH_VERSION_VALUE = OAuth.ONE_DOT_OH_OAUTH_VERSION_VALUE;

  public static final TwitterOAuth1HttpRequestSigner DEFAULT_SIGNER =
      HmacSha1OAuthHttpRequestSigner.INSTANCE;

  public static final DefaultTwitterOAuth1HttpRequestAuthorizer INSTANCE =
      new DefaultTwitterOAuth1HttpRequestAuthorizer();

  private final TwitterOAuth1HttpRequestSigner signer;
  private final String oauthVersion;

  public DefaultTwitterOAuth1HttpRequestAuthorizer() {
    this(DEFAULT_SIGNER, DEFAULT_OAUTH_VERSION_VALUE);
  }

  public DefaultTwitterOAuth1HttpRequestAuthorizer(TwitterOAuth1HttpRequestSigner signer,
      String oauthVersion) {
    this.signer = signer;
    this.oauthVersion = oauthVersion;
  }

  /**
   * @return the signer
   */
  public TwitterOAuth1HttpRequestSigner getSigner() {
    return signer;
  }

  /**
   * @return the oAuthVersion
   */
  public String getOAuthVersion() {
    return oauthVersion;
  }

  @Override
  public ModelHttpRequest authorize(ModelHttpRequest request, String consumerKey,
      String consumerSecret, String token, String tokenSecret) {

    long timestamp = now();

    String nonce = nonce();

    byte[] signature = getSigner().sign(request, nonce, timestamp, getOAuthVersion(), consumerKey,
        consumerSecret, token, tokenSecret);
    String signatureString = Base64.getEncoder().encodeToString(signature);

    List<Parameter> parameters = new ArrayList<>();
    parameters.add(Parameter.of(OAuth.OAUTH_CONSUMER_KEY_NAME, consumerKey));
    parameters.add(Parameter.of(OAuth.OAUTH_NONCE_NAME, nonce));
    parameters.add(Parameter.of(OAuth.OAUTH_SIGNATURE_NAME, signatureString));
    parameters.add(
        Parameter.of(OAuth.OAUTH_SIGNATURE_METHOD_NAME, getSigner().getOAuthSignatureMethod()));
    parameters.add(Parameter.of(OAuth.OAUTH_TIMESTAMP, Long.toString(timestamp)));
    parameters.add(Parameter.of(OAuth.OAUTH_VERSION_NAME, getOAuthVersion()));
    if (token != null)
      parameters.add(Parameter.of(OAuth.OAUTH_TOKEN_NAME, token));

    String authorization = "OAuth " + parameters
        .stream().sorted().map(p -> String.format("%s=\"%s\"",
            ModelHttpEncodings.urlencode(p.getKey()), ModelHttpEncodings.urlencode(p.getValue())))
        .collect(joining(", "));

    List<ModelHttpHeader> headers = new ArrayList<>();
    headers.addAll(request.getHeaders());
    headers.add(ModelHttpHeader.of(ModelHttpHeaderNames.AUTHORIZATION, authorization));

    return ModelHttpRequest.of(request.getVersion(), request.getMethod(), request.getUrl(), headers,
        request.getEntity());
  }

  /**
   * test hook
   */
  protected long now() {
    return Instant.now().toEpochMilli();
  }

  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * test hook
   * 
   * @return
   */
  protected String nonce() {
    byte[] nonce = new byte[16];
    RANDOM.nextBytes(nonce);
    return Base64.getEncoder().encodeToString(nonce);
  }
}
