/*-
 * =================================LICENSE_START==================================
 * dropwizard-jwt
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
package com.sigpwned.dropwizard.auth.social.example.webapp;

import java.io.IOException;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sigpwned.dropwizard.auth.social.linting.Generated;
import com.sigpwned.dropwizard.auth.social.linting.VisibleForTesting;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Uses access tokens as credentials
 */
public class AccessTokenAuthFilter<P extends Principal> extends AuthFilter<String, P> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenAuthFilter.class);

  /**
   * The default query parameter for passing in a token
   */
  public static final String DEFAULT_QUERY_PARAMETER_NAME = "token";

  /**
   * The default query parameter for passing in a token
   */
  public static final String DEFAULT_COOKIE_PARAMETER_NAME = "token";

  /**
   * The default value of the prefix, which is the leading value of the Authorization header
   */
  public static final String DEFAULT_PREFIX = "Bearer";

  public static <P extends Principal> AccessTokenAuthFilter.Builder<P> builder() {
    return new AccessTokenAuthFilter.Builder<>();
  }

  /**
   * Builder for {@link OAuthCredentialAuthFilter}.
   * <p>
   * An {@link Authenticator} must be provided during the building process.
   * </p>
   *
   * @param <P> the type of the principal
   */
  public static class Builder<P extends Principal>
      extends AuthFilterBuilder<String, P, AccessTokenAuthFilter<P>> {
    private String queryParameterName;
    private String cookieParameterName;

    private Builder() {
      setPrefix(DEFAULT_PREFIX);
      queryParameterName = DEFAULT_QUERY_PARAMETER_NAME;
      cookieParameterName = DEFAULT_COOKIE_PARAMETER_NAME;
    }

    /**
     * @param queryParameterName the queryParameterName to set
     */
    public Builder<P> setQueryParameterName(String queryParameterName) {
      this.queryParameterName = queryParameterName;
      return this;
    }

    /**
     * @param cookieParameterName the cookieParameterName to set
     */
    public Builder<P> setCookieParameterName(String cookieParameterName) {
      this.cookieParameterName = cookieParameterName;
      return this;
    }

    @Override
    public Builder<P> setRealm(String realm) {
      return (Builder<P>) super.setRealm(realm);
    }

    @Override
    public Builder<P> setPrefix(String prefix) {
      return (Builder<P>) super.setPrefix(prefix);
    }

    @Override
    public Builder<P> setAuthorizer(Authorizer<P> authorizer) {
      return (Builder<P>) super.setAuthorizer(authorizer);
    }

    @Override
    public Builder<P> setAuthenticator(Authenticator<String, P> authenticator) {
      return (Builder<P>) super.setAuthenticator(authenticator);
    }

    @Override
    public Builder<P> setUnauthorizedHandler(UnauthorizedHandler unauthorizedHandler) {
      return (Builder<P>) super.setUnauthorizedHandler(unauthorizedHandler);
    }

    @Override
    protected AccessTokenAuthFilter<P> newInstance() {
      return new AccessTokenAuthFilter<>(queryParameterName, cookieParameterName);
    }
  }

  @VisibleForTesting
  static class Authorization {
    public static Authorization fromString(String s) {
      int index = s.indexOf(' ');
      if (index == -1)
        throw new IllegalArgumentException("no method");

      String method = s.substring(0, index).strip();
      String credentials = s.substring(index + 1, s.length()).strip();

      return of(method, credentials);
    }

    public static Authorization of(String method, String credentials) {
      return new Authorization(method, credentials);
    }

    private final String method;
    private final String credentials;

    public Authorization(String method, String credentials) {
      this.method = method;
      this.credentials = credentials;
    }

    /**
     * @return the method
     */
    @Generated
    public String getMethod() {
      return method;
    }

    /**
     * @return the credentials
     */
    @Generated
    public String getCredentials() {
      return credentials;
    }

    @Override
    @Generated
    public int hashCode() {
      return Objects.hash(credentials, method);
    }

    @Override
    @Generated
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Authorization other = (Authorization) obj;
      return Objects.equals(credentials, other.credentials) && Objects.equals(method, other.method);
    }

    @Override
    public String toString() {
      return getMethod() + " " + getCredentials();
    }
  }

  /**
   * An optional query parameter to pass the token.
   * 
   * @see #DEFAULT_QUERY_PARAMETER_NAME
   */
  private final String queryParameterName;

  /**
   * An optional cookie parameter to pass the token.
   * 
   * @see #DEFAULT_COOKIE_PARAMETER_NAME
   */
  private final String cookieParameterName;

  public AccessTokenAuthFilter() {
    this(DEFAULT_QUERY_PARAMETER_NAME, DEFAULT_COOKIE_PARAMETER_NAME);
  }

  public AccessTokenAuthFilter(String queryParameterName, String cookieParameterName) {
    this.queryParameterName = queryParameterName;
    this.cookieParameterName = cookieParameterName;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String credentials;

    // Try to read a token from the query parameter first, if we have one
    credentials = Optional
        .ofNullable(requestContext.getUriInfo().getQueryParameters().getFirst(queryParameterName))
        .orElse(null);

    // Try to read a token from the cookie parameter next, if we have one
    if (credentials == null && cookieParameterName != null) {
      credentials = Optional.ofNullable(requestContext.getCookies().get(cookieParameterName))
          .map(Cookie::getValue).orElse(null);
    }

    // Try to read a token from the authentication header last, if we have one
    if (credentials == null) {
      try {
        credentials = Optional.ofNullable(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION))
            .map(Authorization::fromString).filter(a -> a.getMethod().equalsIgnoreCase(prefix))
            .map(Authorization::getCredentials).orElse(null);
      } catch (IllegalArgumentException e) {
        // No problem. This just isn't valid authentication.
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Failed to parse authorization", e);
        credentials = null;
      }
    }

    // See if the application accepts our claims, which may be null. If not, fail as unauthorized.
    if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
      throw unauthorizedHandler.buildException(prefix, realm);
    }
  }
}
