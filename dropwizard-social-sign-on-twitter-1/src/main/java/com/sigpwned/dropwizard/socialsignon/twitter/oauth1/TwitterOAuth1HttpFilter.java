/*-
 * =================================LICENSE_START==================================
 * dropwizard-social-sign-on-twitter-1
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
package com.sigpwned.dropwizard.socialsignon.twitter.oauth1;

import static java.lang.String.format;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sigpwned.dropwizard.socialsignon.core.util.OAuth;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.authorizer.DefaultTwitterOAuth1HttpRequestAuthorizer;
import com.sigpwned.httpmodel.ModelHttpEntity;
import com.sigpwned.httpmodel.ModelHttpHeader;
import com.sigpwned.httpmodel.ModelHttpQueryString;
import com.sigpwned.httpmodel.ModelHttpRequest;
import com.sigpwned.httpmodel.ModelHttpResponse;
import com.sigpwned.httpmodel.ModelHttpUrl;
import com.sigpwned.httpmodel.entity.ModelHttpFormData;
import com.sigpwned.httpmodel.servlet.util.ModelHttpServlets;
import com.sigpwned.httpmodel.util.ModelHttpClients;
import com.sigpwned.httpmodel.util.ModelHttpHeaderNames;
import com.sigpwned.httpmodel.util.ModelHttpMediaTypes;
import com.sigpwned.httpmodel.util.ModelHttpMethods;
import com.sigpwned.httpmodel.util.ModelHttpStatusCodes;
import com.sigpwned.httpmodel.util.ModelHttpVersions;

public class TwitterOAuth1HttpFilter extends HttpFilter {
  private static final long serialVersionUID = -4818000499514852808L;

  /* default */ static final String DEFAULT_TWITTER_REQUEST_TOKEN_URL =
      "https://api.twitter.com/oauth/request_token";

  /* default */ static final String DEFAULT_TWITTER_AUTHENTICATE_URL =
      "https://api.twitter.com/oauth/authenticate";

  /* default */ static final String DEFAULT_TWITTER_ACCESS_TOKEN_URL =
      "https://api.twitter.com/oauth/access_token";

  /* default */ static final String BASE_PATH = "oauth/twitter/1";

  /* default */ static final String AUTHENTICATE = "authenticate";

  /* default */ static final String CALLBACK = "callback";

  private final String baseUrl;
  private final String consumerKey;
  private final String consumerSecret;
  private final TokenStore store;
  private final AuthenticatedHandler handler;
  private final TwitterOAuth1HttpRequestAuthorizer authorizer;
  private final String twitterRequestTokenUrl;
  private final String twitterAuthenticateUrl;
  private final String twitterAccessTokenUrl;

  @Inject
  public TwitterOAuth1HttpFilter(@Named("oauth4jBaseUrl") String baseUrl,
      @Named("oauth4jTwitterConsumerKey") String consumerKey,
      @Named("oauth4jTwitterConsumerSecret") String consumerSecret, TokenStore store,
      AuthenticatedHandler handler) {
    this(baseUrl, consumerKey, consumerSecret, store, handler,
        DefaultTwitterOAuth1HttpRequestAuthorizer.INSTANCE, DEFAULT_TWITTER_REQUEST_TOKEN_URL,
        DEFAULT_TWITTER_AUTHENTICATE_URL, DEFAULT_TWITTER_ACCESS_TOKEN_URL);
  }

  /* default */ TwitterOAuth1HttpFilter(String baseUrl, String consumerKey, String consumerSecret,
      TokenStore store, AuthenticatedHandler handler, TwitterOAuth1HttpRequestAuthorizer authorizer,
      String twitterRequestTokenUrl, String twitterAuthenticateUrl, String twitterAccessTokenUrl) {
    this.baseUrl = baseUrl;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.store = store;
    this.handler = handler;
    this.authorizer = authorizer;
    this.twitterRequestTokenUrl = twitterRequestTokenUrl;
    this.twitterAuthenticateUrl = twitterAuthenticateUrl;
    this.twitterAccessTokenUrl = twitterAccessTokenUrl;
  }

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    if (req.getRequestURI().equals(format("%s/%s/%s", baseUrl, BASE_PATH, AUTHENTICATE))) {
      ModelHttpServlets.fromModelResponse(res, authenticate(req));
    } else if (req.getRequestURI().equals(format("%s/%s/%s", baseUrl, BASE_PATH, CALLBACK))) {
      ModelHttpServlets.fromModelResponse(res, callback(req));
    } else {
      chain.doFilter(req, res);
    }
  }

  protected ModelHttpResponse authenticate(HttpServletRequest req) throws IOException {
    ModelHttpRequest request = ModelHttpServlets.toModelRequest(req);

    if (!request.getMethod().equals(ModelHttpMethods.GET)) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.METHOD_NOT_ALLOWED,
          List.of(ModelHttpHeader.of(ModelHttpHeaderNames.ALLOW, ModelHttpMethods.GET)),
          ModelHttpEntity.of(ModelHttpMediaTypes.TEXT_PLAIN, "Method not allowed"));
    }

    ModelHttpRequest unsignedRequest =
        ModelHttpRequest.of(ModelHttpVersions.HTTP_1_1, ModelHttpMethods.POST,
            ModelHttpUrl.fromString(getTwitterAuthenticateUrl() + "?" + ModelHttpQueryString.of(
                ModelHttpQueryString.Parameter.of(OAuth.OAUTH_CALLBACK_NAME, getCallbackUrl()))),
            Optional.empty());

    ModelHttpRequest signedRequest =
        authorizer.authorize(unsignedRequest, getConsumerKey(), getConsumerSecret());

    HttpResponse<String> response;
    try {
      response = newHttpClient().send(ModelHttpClients.fromRequest(signedRequest),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException();
    }
    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.INTERNAL_ERROR, ModelHttpEntity
          .of(ModelHttpMediaTypes.TEXT_PLAIN, "Downstream authentication request failed"));
    }

    ModelHttpFormData form = ModelHttpFormData.fromString(response.body());

    String oauthToken = form.findFirstEntryByName(OAuth.OAUTH_TOKEN_NAME)
        .flatMap(ModelHttpFormData.Entry::getValue).orElse(null);
    if (oauthToken == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.INTERNAL_ERROR, ModelHttpEntity
          .of(ModelHttpMediaTypes.TEXT_PLAIN, "Response missing " + OAuth.OAUTH_TOKEN_NAME));
    }

    String oauthTokenSecret = form.findFirstEntryByName(OAuth.OAUTH_TOKEN_SECRET_NAME)
        .flatMap(ModelHttpFormData.Entry::getValue).orElse(null);
    if (oauthTokenSecret == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.INTERNAL_ERROR, ModelHttpEntity
          .of(ModelHttpMediaTypes.TEXT_PLAIN, "Response missing " + OAuth.OAUTH_TOKEN_SECRET_NAME));
    }

    getStore().putTokenSecret(oauthToken, oauthTokenSecret);

    return ModelHttpResponse.of(ModelHttpStatusCodes.TEMPORARY_REDIRECT,
        List.of(ModelHttpHeader.of(ModelHttpHeaderNames.LOCATION, getTwitterAuthenticateUrl())),
        Optional.empty());
  }

  protected ModelHttpResponse callback(HttpServletRequest req) throws IOException {
    ModelHttpRequest request = ModelHttpServlets.toModelRequest(req);

    if (!request.getMethod().equals(ModelHttpMethods.GET)) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.METHOD_NOT_ALLOWED,
          List.of(ModelHttpHeader.of(ModelHttpHeaderNames.ALLOW, ModelHttpMethods.GET)),
          ModelHttpEntity.of(ModelHttpMediaTypes.TEXT_PLAIN, "Method not allowed"));
    }

    String oauthToken = request.getUrl().getQueryString()
        .flatMap(q -> q.findFirstParameterByName(OAuth.OAUTH_TOKEN_NAME))
        .flatMap(ModelHttpQueryString.Parameter::getValue).orElse(null);
    if (oauthToken == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.BAD_REQUEST, ModelHttpEntity.of(
          ModelHttpMediaTypes.TEXT_PLAIN, "Missing required parameter " + OAuth.OAUTH_TOKEN_NAME));
    }

    String oauthVerifier = request.getUrl().getQueryString()
        .flatMap(q -> q.findFirstParameterByName(OAuth.OAUTH_VERIFIER_NAME))
        .flatMap(ModelHttpQueryString.Parameter::getValue).orElse(null);
    if (oauthVerifier == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.BAD_REQUEST,
          ModelHttpEntity.of(ModelHttpMediaTypes.TEXT_PLAIN,
              "Missing required parameter " + OAuth.OAUTH_VERIFIER_NAME));
    }

    String oauthTokenSecret = getStore().getTokenSecret(oauthToken).orElse(null);
    if (oauthTokenSecret == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.NOT_FOUND,
          ModelHttpEntity.of(ModelHttpMediaTypes.TEXT_PLAIN, "Not Found"));
    }

    ModelHttpRequest unsignedRequest =
        ModelHttpRequest
            .of(ModelHttpVersions.HTTP_1_1, ModelHttpMethods.POST,
                ModelHttpUrl.fromString(getTwitterAccessTokenUrl() + "?" + ModelHttpQueryString.of(
                    ModelHttpQueryString.Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
                    ModelHttpQueryString.Parameter.of(OAuth.OAUTH_VERIFIER_NAME, oauthVerifier))),
                Optional.empty());

    ModelHttpRequest signedRequest = getAuthorizer().authorize(unsignedRequest, getConsumerKey(),
        getConsumerSecret(), oauthToken, oauthTokenSecret);

    HttpResponse<String> response;
    try {
      response = newHttpClient().send(ModelHttpClients.fromRequest(signedRequest),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException();
    }
    if (response.statusCode() != HttpURLConnection.HTTP_OK) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.INTERNAL_ERROR, ModelHttpEntity
          .of(ModelHttpMediaTypes.TEXT_PLAIN, "Downstream authentication request failed"));
    }

    ModelHttpFormData form = ModelHttpFormData.fromString(response.body());

    String accessToken = form.findFirstEntryByName(OAuth.OAUTH_TOKEN_NAME)
        .flatMap(ModelHttpFormData.Entry::getValue).orElse(null);
    if (accessToken == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.INTERNAL_ERROR, ModelHttpEntity
          .of(ModelHttpMediaTypes.TEXT_PLAIN, "Response missing " + OAuth.OAUTH_TOKEN_NAME));
    }

    String accessTokenSecret = form.findFirstEntryByName(OAuth.OAUTH_TOKEN_SECRET_NAME)
        .flatMap(ModelHttpFormData.Entry::getValue).orElse(null);
    if (accessTokenSecret == null) {
      return ModelHttpResponse.of(ModelHttpStatusCodes.INTERNAL_ERROR, ModelHttpEntity
          .of(ModelHttpMediaTypes.TEXT_PLAIN, "Response missing " + OAuth.OAUTH_TOKEN_SECRET_NAME));
    }

    return getHandler().authenticated(accessToken, accessTokenSecret);
  }

  /**
   * Returns the local URL for the authenticate endpoint
   */
  public String getAuthenticateUrl() {
    return String.format("%s/%s/%s", getBaseUrl(), BASE_PATH, AUTHENTICATE);
  }

  /**
   * Returns the local URL for the callback endpoint
   */
  public String getCallbackUrl() {
    return String.format("%s/%s/%s", getBaseUrl(), BASE_PATH, CALLBACK);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * @return the store
   */
  public TokenStore getStore() {
    return store;
  }

  /**
   * @return the handler
   */
  public AuthenticatedHandler getHandler() {
    return handler;
  }

  /**
   * @return the consumerKey
   */
  public String getConsumerKey() {
    return consumerKey;
  }

  /**
   * @return the consumerSecret
   */
  public String getConsumerSecret() {
    return consumerSecret;
  }

  /**
   * @return the authorizer
   */
  public TwitterOAuth1HttpRequestAuthorizer getAuthorizer() {
    return authorizer;
  }

  /**
   * @return the twitterRequestTokenUrl
   */
  public String getTwitterRequestTokenUrl() {
    return twitterRequestTokenUrl;
  }

  /**
   * @return the twitterAuthenticateUrl
   */
  public String getTwitterAuthenticateUrl() {
    return twitterAuthenticateUrl;
  }

  /**
   * @return the twitterAccessTokenUrl
   */
  public String getTwitterAccessTokenUrl() {
    return twitterAccessTokenUrl;
  }

  /**
   * test hook
   * 
   * @return
   */
  protected HttpClient newHttpClient() {
    return HttpClient.newHttpClient();
  }
}
