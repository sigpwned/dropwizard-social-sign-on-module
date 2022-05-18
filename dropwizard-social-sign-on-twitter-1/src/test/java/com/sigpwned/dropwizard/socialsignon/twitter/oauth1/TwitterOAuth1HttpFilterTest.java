/*-
 * =================================LICENSE_START==================================
 * oauth4j-server
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

import static java.util.Collections.emptyEnumeration;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sigpwned.dropwizard.socialsignon.core.util.OAuth;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.authorizer.DefaultTwitterOAuth1HttpRequestAuthorizer;
import com.sigpwned.dropwizard.socialsignon.twitter.oauth1.util.Parameter;
import com.sigpwned.httpmodel.ModelHttpEntity;
import com.sigpwned.httpmodel.ModelHttpQueryString;
import com.sigpwned.httpmodel.ModelHttpResponse;
import com.sigpwned.httpmodel.util.ModelHttpHeaderNames;
import com.sigpwned.httpmodel.util.ModelHttpStatusCodes;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class TwitterOAuth1HttpFilterTest {
  public MockWebServer server;

  @Before
  public void setupDefaultLinkUnwinderTest() {
    server = new MockWebServer();
  }

  @After
  public void cleanupDefaultLinkUnwinderTest() throws IOException {
    try {
      server.shutdown();
    } catch (Exception e) {
      // Ignore me
    }
  }

  @Test
  public void shouldSucceedIfEverythingGoesRight() throws Exception {
    final String oauthToken = "foo";
    final String oauthTokenSecret = "bar";
    final String oauthTokenVerifier = "verifier";
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, oauthTokenSecret))));

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, token),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, tokenSecret))));

    server.start();

    final TokenStore store = mock(TokenStore.class);
    when(store.getTokenSecret(oauthToken)).thenReturn(Optional.of(oauthTokenSecret));

    final int status = HttpURLConnection.HTTP_OK;
    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);
    when(handler.authenticated(token, tokenSecret))
        .thenReturn(ModelHttpResponse.of(status, ModelHttpEntity.ofPlainText("success")));

    final HttpUrl twitterRequestTokenUrl = server.url(TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.DEFAULT_TWITTER_REQUEST_TOKEN_URL);
    final HttpUrl twitterAuthenticateUrl = server.url(TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.DEFAULT_TWITTER_AUTHENTICATE_URL);
    final HttpUrl twitterAccessTokenUrl = server.url(TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.DEFAULT_TWITTER_ACCESS_TOKEN_URL);

    TwitterOAuth1HttpFilter unit = new TwitterOAuth1HttpFilter(
        "http://localhost:" + server.getPort(), consumerKey, consumerSecret, store, handler,
        DefaultTwitterOAuth1HttpRequestAuthorizer.INSTANCE, twitterRequestTokenUrl.toString(),
        twitterAuthenticateUrl.toString(), twitterAccessTokenUrl.toString());

    final HttpServletRequest req1 = mock(HttpServletRequest.class);
    when(req1.getScheme()).thenReturn("http");
    when(req1.getProtocol()).thenReturn("HTTP/1.1");
    when(req1.getMethod()).thenReturn("GET");
    when(req1.getServerName()).thenReturn("localhost");
    when(req1.getServerPort()).thenReturn(server.getPort());
    when(req1.getRequestURI())
        .thenReturn("/oauth/twitter/1/" + TwitterOAuth1HttpFilter.AUTHENTICATE);
    when(req1.getHeaderNames()).thenReturn(emptyEnumeration());

    ModelHttpResponse response1 = unit.authenticate(req1);

    verify(store).putTokenSecret(oauthToken, oauthTokenSecret);

    assertThat(response1.getStatusCode(), is(ModelHttpStatusCodes.TEMPORARY_REDIRECT));
    assertThat(response1.getHeaders().stream()
        .filter(h -> h.getName().equals(ModelHttpHeaderNames.LOCATION)).map(h -> h.getValue())
        .findFirst().orElseThrow(AssertionError::new), is(twitterAuthenticateUrl.toString()));

    RecordedRequest request1 = server.takeRequest();
    assertThat(request1.getRequestUrl().queryParameter(OAuth.OAUTH_CALLBACK_NAME),
        is(unit.getCallbackUrl()));

    final HttpServletRequest req2 = mock(HttpServletRequest.class);
    when(req2.getScheme()).thenReturn("http");
    when(req2.getProtocol()).thenReturn("HTTP/1.1");
    when(req2.getMethod()).thenReturn("GET");
    when(req2.getServerName()).thenReturn("localhost");
    when(req2.getServerPort()).thenReturn(server.getPort());
    when(req2.getRequestURI()).thenReturn("/oauth/twitter/1/" + TwitterOAuth1HttpFilter.CALLBACK);
    when(req2.getQueryString()).thenReturn(ModelHttpQueryString
        .of(ModelHttpQueryString.Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
            ModelHttpQueryString.Parameter.of(OAuth.OAUTH_VERIFIER_NAME, oauthTokenVerifier))
        .toString());
    when(req2.getHeaderNames()).thenReturn(emptyEnumeration());

    ModelHttpResponse response2 = unit.callback(req2);

    assertThat(response2.getStatusCode(), is(status));
  }

  @Test
  public void should404IfUnrecognized() throws Exception {
    final String oauthToken = "foo";
    final String oauthTokenSecret = "bar";
    final String oauthTokenVerifier = "verifier";
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
    final String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
    final String tokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, oauthToken),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, oauthTokenSecret))));

    server.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(String.format("%s&%s", Parameter.of(OAuth.OAUTH_TOKEN_NAME, token),
            Parameter.of(OAuth.OAUTH_TOKEN_SECRET_NAME, tokenSecret))));

    server.start();

    final TokenStore store = mock(TokenStore.class);
    when(store.getTokenSecret(oauthToken)).thenReturn(Optional.of(oauthTokenSecret));

    final int status = HttpURLConnection.HTTP_OK;
    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);
    when(handler.authenticated(token, tokenSecret))
        .thenReturn(ModelHttpResponse.of(status, ModelHttpEntity.ofPlainText("success")));

    final HttpUrl twitterRequestTokenUrl = server.url(TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.DEFAULT_TWITTER_REQUEST_TOKEN_URL);
    final HttpUrl twitterAuthenticateUrl = server.url(TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.DEFAULT_TWITTER_AUTHENTICATE_URL);
    final HttpUrl twitterAccessTokenUrl = server.url(TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.DEFAULT_TWITTER_ACCESS_TOKEN_URL);

    TwitterOAuth1HttpFilter unit = new TwitterOAuth1HttpFilter(
        "http://localhost:" + server.getPort(), consumerKey, consumerSecret, store, handler,
        DefaultTwitterOAuth1HttpRequestAuthorizer.INSTANCE, twitterRequestTokenUrl.toString(),
        twitterAuthenticateUrl.toString(), twitterAccessTokenUrl.toString());

    final HttpServletRequest req1 = mock(HttpServletRequest.class);
    when(req1.getScheme()).thenReturn("http");
    when(req1.getProtocol()).thenReturn("HTTP/1.1");
    when(req1.getMethod()).thenReturn("GET");
    when(req1.getServerName()).thenReturn("localhost");
    when(req1.getServerPort()).thenReturn(server.getPort());
    when(req1.getRequestURI())
        .thenReturn("/oauth/twitter/1/" + TwitterOAuth1HttpFilter.AUTHENTICATE);
    when(req1.getHeaderNames()).thenReturn(emptyEnumeration());

    ModelHttpResponse response1 = unit.authenticate(req1);

    verify(store).putTokenSecret(oauthToken, oauthTokenSecret);

    assertThat(response1.getStatusCode(), is(ModelHttpStatusCodes.TEMPORARY_REDIRECT));
    assertThat(response1.getHeaders().stream()
        .filter(h -> h.getName().equals(ModelHttpHeaderNames.LOCATION)).map(h -> h.getValue())
        .findFirst().orElseThrow(AssertionError::new), is(twitterAuthenticateUrl.toString()));

    RecordedRequest request1 = server.takeRequest();
    assertThat(request1.getRequestUrl().queryParameter(OAuth.OAUTH_CALLBACK_NAME),
        is(unit.getCallbackUrl()));

    final HttpServletRequest req2 = mock(HttpServletRequest.class);
    when(req2.getScheme()).thenReturn("http");
    when(req2.getProtocol()).thenReturn("HTTP/1.1");
    when(req2.getMethod()).thenReturn("GET");
    when(req2.getServerName()).thenReturn("localhost");
    when(req2.getServerPort()).thenReturn(server.getPort());
    when(req2.getRequestURI()).thenReturn("/oauth/twitter/1/" + TwitterOAuth1HttpFilter.CALLBACK);
    when(req2.getQueryString()).thenReturn(ModelHttpQueryString
        .of(ModelHttpQueryString.Parameter.of(OAuth.OAUTH_TOKEN_NAME, "trash"),
            ModelHttpQueryString.Parameter.of(OAuth.OAUTH_VERIFIER_NAME, oauthTokenVerifier))
        .toString());
    when(req2.getHeaderNames()).thenReturn(emptyEnumeration());

    ModelHttpResponse response2 = unit.callback(req2);

    assertThat(response2.getStatusCode(), is(ModelHttpStatusCodes.NOT_FOUND));
  }

  @Test
  public void shouldCallChainForUnhandledUrl() throws Exception {
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";

    final TokenStore store = mock(TokenStore.class);

    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);

    final String baseUrl = "";

    @SuppressWarnings("serial")
    TwitterOAuth1HttpFilter unit =
        new TwitterOAuth1HttpFilter(baseUrl, consumerKey, consumerSecret, store, handler) {

          @Override
          protected ModelHttpResponse authenticate(HttpServletRequest req) throws IOException {
            throw new UnsupportedOperationException();
          }

          @Override
          protected ModelHttpResponse callback(HttpServletRequest req) throws IOException {
            throw new UnsupportedOperationException();
          }
        };

    final HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/other");

    final HttpServletResponse res = mock(HttpServletResponse.class);
    when(res.getOutputStream()).thenReturn(new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
        // NOP
      }

      @Override
      public void write(int b) throws IOException {
        // NOP
      }
    });

    final FilterChain chain = mock(FilterChain.class);

    unit.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  public void shouldCallAuthenticateForAuthenticateUrl() throws Exception {
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";

    final TokenStore store = mock(TokenStore.class);

    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);

    final AtomicBoolean called = new AtomicBoolean(false);

    final String baseUrl = "";

    @SuppressWarnings("serial")
    TwitterOAuth1HttpFilter unit =
        new TwitterOAuth1HttpFilter(baseUrl, consumerKey, consumerSecret, store, handler) {

          @Override
          protected ModelHttpResponse authenticate(HttpServletRequest req) throws IOException {
            called.set(true);
            return ModelHttpResponse.of(ModelHttpStatusCodes.OK,
                ModelHttpEntity.ofPlainText("hello"));
          }

          @Override
          protected ModelHttpResponse callback(HttpServletRequest req) throws IOException {
            throw new UnsupportedOperationException();
          }
        };

    final HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn(baseUrl + "/" + TwitterOAuth1HttpFilter.BASE_PATH + "/"
        + TwitterOAuth1HttpFilter.AUTHENTICATE);

    final HttpServletResponse res = mock(HttpServletResponse.class);
    when(res.getOutputStream()).thenReturn(new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
        // NOP
      }

      @Override
      public void write(int b) throws IOException {
        // NOP
      }
    });

    final FilterChain chain = mock(FilterChain.class);

    unit.doFilter(req, res, chain);

    assertThat(called.get(), is(true));
  }

  @Test
  public void shouldCallAuthenticateForCallbackUrl() throws Exception {
    final String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
    final String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";

    final TokenStore store = mock(TokenStore.class);

    final AuthenticatedHandler handler = mock(AuthenticatedHandler.class);

    final AtomicBoolean called = new AtomicBoolean(false);

    final String baseUrl = "";

    @SuppressWarnings("serial")
    TwitterOAuth1HttpFilter unit =
        new TwitterOAuth1HttpFilter(baseUrl, consumerKey, consumerSecret, store, handler) {

          @Override
          protected ModelHttpResponse authenticate(HttpServletRequest req) throws IOException {
            throw new UnsupportedOperationException();
          }

          @Override
          protected ModelHttpResponse callback(HttpServletRequest req) throws IOException {
            called.set(true);
            return ModelHttpResponse.of(ModelHttpStatusCodes.OK,
                ModelHttpEntity.ofPlainText("hello"));
          }
        };

    final HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn(
        baseUrl + "/" + TwitterOAuth1HttpFilter.BASE_PATH + "/" + TwitterOAuth1HttpFilter.CALLBACK);

    final HttpServletResponse res = mock(HttpServletResponse.class);
    when(res.getOutputStream()).thenReturn(new ServletOutputStream() {
      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
        // NOP
      }

      @Override
      public void write(int b) throws IOException {
        // NOP
      }
    });

    final FilterChain chain = mock(FilterChain.class);

    unit.doFilter(req, res, chain);

    assertThat(called.get(), is(true));
  }
}
