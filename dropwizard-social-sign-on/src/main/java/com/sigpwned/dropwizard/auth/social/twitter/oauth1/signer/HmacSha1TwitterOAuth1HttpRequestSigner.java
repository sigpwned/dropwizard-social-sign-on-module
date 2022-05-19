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
package com.sigpwned.dropwizard.auth.social.twitter.oauth1.signer;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1HttpRequestSigner;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.util.Parameter;
import com.sigpwned.dropwizard.auth.social.util.OAuth1;
import com.sigpwned.httpmodel.ModelHttpEntity;
import com.sigpwned.httpmodel.ModelHttpRequest;
import com.sigpwned.httpmodel.ModelHttpUrl;
import com.sigpwned.httpmodel.entity.ModelHttpFormData;
import com.sigpwned.httpmodel.util.ModelHttpEncodings;
import com.sigpwned.httpmodel.util.ModelHttpMediaTypes;

public class HmacSha1TwitterOAuth1HttpRequestSigner implements TwitterOAuth1HttpRequestSigner {
  public static final HmacSha1TwitterOAuth1HttpRequestSigner INSTANCE =
      new HmacSha1TwitterOAuth1HttpRequestSigner();

  @Override
  public String getOAuthSignatureMethod() {
    return OAuth1.HMAC_SHA1_OAUTH_SIGNATURE_METHOD_VALUE;
  }

  protected byte[] computeSignatureBaseString(ModelHttpRequest request, String oauthNonce,
      long oauthTimestamp, String oauthVersion, String consumerKey, String token) {

    List<Parameter> parameters = new ArrayList<>();
    if (request.getUrl().getQueryString().isPresent()) {
      parameters.addAll(request.getUrl().getQueryString().get().getParameters().stream()
          .map(Parameter::fromQueryStringParameter).collect(toList()));
    }
    if (request.getEntity().flatMap(ModelHttpEntity::getType)
        .map(t -> t.isCompatible(ModelHttpMediaTypes.APPLICATION_X_WWW_FORM_URLENCODED))
        .orElse(false)) {
      ModelHttpFormData form = ModelHttpFormData.fromEntity(request.getEntity().get());
      parameters.addAll(form.getEntries().stream().map(Parameter::fromFormEntry).collect(toList()));
    }
    parameters.add(Parameter.of(OAuth1.OAUTH_CONSUMER_KEY_NAME, consumerKey));
    parameters.add(Parameter.of(OAuth1.OAUTH_NONCE_NAME, oauthNonce));
    parameters.add(Parameter.of(OAuth1.OAUTH_SIGNATURE_METHOD_NAME, getOAuthSignatureMethod()));
    parameters.add(Parameter.of(OAuth1.OAUTH_TIMESTAMP, Long.toString(oauthTimestamp)));
    parameters.add(Parameter.of(OAuth1.OAUTH_VERSION_NAME, oauthVersion));
    if (token != null)
      parameters.add(Parameter.of(OAuth1.OAUTH_TOKEN_NAME, token));

    String parameterString =
        parameters.stream().sorted().map(Objects::toString).collect(joining("&"));

    return new StringBuilder().append(request.getMethod()).append("&")
        .append(ModelHttpEncodings.urlencode(toBaseUrl(request.getUrl()))).append("&")
        .append(ModelHttpEncodings.urlencode(parameterString)).toString()
        .getBytes(StandardCharsets.US_ASCII);
  }

  protected String toBaseUrl(ModelHttpUrl url) {
    String result = url.toString();

    int index = result.indexOf('?');
    if (index != -1)
      result = result.substring(0, index);

    return result;
  }

  protected byte[] computeSigningKey(String consumerSecret, String tokenSecret) {
    return new StringBuilder().append(ModelHttpEncodings.urlencode(consumerSecret)).append("&")
        .append(ModelHttpEncodings.urlencode(Optional.ofNullable(tokenSecret).orElse("")))
        .toString().getBytes(StandardCharsets.US_ASCII);
  }

  private static final String ALGORITHM = "HmacSHA1";

  @Override
  public byte[] sign(ModelHttpRequest request, String oAuthNonce, long oAuthTimestamp,
      String oAuthVersion, String consumerKey, String consumerSecret, String token,
      String tokenSecret) {
    byte[] signatureBase = computeSignatureBaseString(request, oAuthNonce, oAuthTimestamp,
        oAuthVersion, consumerKey, token);
    
    System.out.println(new String(signatureBase));

    byte[] signingKey = computeSigningKey(consumerSecret, tokenSecret);

    System.out.println(new String(signingKey));

    Key key = new SecretKeySpec(signingKey, 0, signingKey.length, ALGORITHM);

    Mac mac;
    try {
      mac = Mac.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      // The spec stipulates JDKs must implement this algorithm
      throw new AssertionError("Required algorithm is not supported", e);
    }

    try {
      mac.init(key);
    } catch (InvalidKeyException e) {
      // They key and mac use the same value for the algorithm
      throw new AssertionError("Mac impossibly does not support Key with same algorithm", e);
    }

    return mac.doFinal(signatureBase);
  }
}
