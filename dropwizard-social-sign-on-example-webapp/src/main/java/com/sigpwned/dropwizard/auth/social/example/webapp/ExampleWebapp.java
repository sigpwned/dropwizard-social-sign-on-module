/*-
 * =================================LICENSE_START==================================
 * dropwizard-jose-jwt-example-webapp
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
import java.util.Optional;
import javax.ws.rs.core.Cookie;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import com.sigpwned.dropwizard.auth.social.TwitterOAuth1Bundle;
import com.sigpwned.dropwizard.auth.social.example.webapp.auth.ExampleAuthenticator;
import com.sigpwned.dropwizard.auth.social.example.webapp.auth.ExampleAuthorizer;
import com.sigpwned.dropwizard.auth.social.example.webapp.health.AccessTokenStoreHealthCheck;
import com.sigpwned.dropwizard.auth.social.example.webapp.health.OAuthTokenStoreHealthCheck;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.Account;
import com.sigpwned.dropwizard.auth.social.example.webapp.resource.MeResource;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1AuthenticatedHandler;
import com.sigpwned.httpmodel.ModelHttpHeaders;
import com.sigpwned.httpmodel.ModelHttpResponse;
import com.sigpwned.httpmodel.util.ModelHttpHeaderNames;
import com.sigpwned.httpmodel.util.ModelHttpStatusCodes;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

/**
 * This web application uses JWTs as a stateless session ID for a user-facing SPA. (If we use our
 * imaginations.)
 */
public class ExampleWebapp extends Application<ExampleConfiguration> {
  public static void main(String[] args) throws Exception {
    new ExampleWebapp().run(args);
  }

  @Override
  public String getName() {
    return "ExampleWebapp";
  }

  @Override
  public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(true)));

    // We register the Twitter OAuth 1.0a bundle here in the initialization function. The bundle is
    // abstract and requires us to implement two methods to create the bundle's token store and and
    // authenticated handler.
    bootstrap.addBundle(new TwitterOAuth1Bundle<ExampleConfiguration>() {
      /**
       * Create our token store by pulling configuration data out of our configuration object.
       */
      @Override
      protected OAuthTokenStore getTokenStore(ExampleConfiguration configuration) {
        return configuration.getSocialAuth().getOAuthTokenStore().build();
      }

      /**
       * Create our authenticated handler. Note that we store our new access token (since we won't
       * get to see it again) and then we just redirect the user to another page and set a cookie.
       * (Note that the existing TwitterOAuth1TokenStore is for temporary token secrets used during
       * the OAuth flow and not for proper access tokens.) Also, in a real application, you'd set a
       * cookie with a proper session token or JWT instead of just using the access token, for
       * example, but for this demo, this will do just fine. We don't share the token secret, so
       * it's not totally broken.
       */
      @Override
      protected TwitterOAuth1AuthenticatedHandler getAuthenticatedHandler(
          ExampleConfiguration configuration) {
        final AccessTokenStore store = configuration.getAccessTokenStore().build();
        return new TwitterOAuth1AuthenticatedHandler() {
          @Override
          public ModelHttpResponse twitterOAuth1Authenticated(String accessToken,
              String accessTokenSecret) throws IOException {
            store.putTwitterOAuth1AccessToken(accessToken, accessTokenSecret);
            return ModelHttpResponse.of(ModelHttpStatusCodes.TEMPORARY_REDIRECT,
                ModelHttpHeaders.of(
                    ModelHttpHeaders.Header.of(ModelHttpHeaderNames.LOCATION, "/me"),
                    ModelHttpHeaders.Header.of(ModelHttpHeaderNames.SET_COOKIE,
                        new Cookie("token", accessToken).toString())),
                Optional.empty());
          }
        };
      }
    });
  }

  @Override
  public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
    // We'll want these for health checks
    OAuthTokenStore oauthTokenStore = configuration.getSocialAuth().getOAuthTokenStore().build();
    AccessTokenStore accessTokenStore = configuration.getAccessTokenStore().build();

    // Our OAuthTokenStore instance has been registered many times under other types, but register
    // it under the umbrella OAuthTokenStore type now. Our AccessTokenStore has never been
    // registered, so register it here now.
    environment.jersey().register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(oauthTokenStore).to(OAuthTokenStore.class);
        bind(accessTokenStore).to(AccessTokenStore.class);
      }
    });

    // Set up our authentication. We treat access tokens as credentials.
    environment.jersey()
        .register(new AuthDynamicFeature(AccessTokenAuthFilter.<Account>builder()
            .setAuthenticator(new ExampleAuthenticator(accessTokenStore))
            .setAuthorizer(new ExampleAuthorizer()).buildAuthFilter()));

    // Our application's resources are simple: you can ask who you are. The OAuth resources are
    // automatically registered by the bundle.
    environment.jersey().register(MeResource.class);

    // Make sure our account store is healthy
    environment.healthChecks().register(AccessTokenStoreHealthCheck.NAME,
        new AccessTokenStoreHealthCheck(accessTokenStore));
    environment.healthChecks().register(OAuthTokenStoreHealthCheck.NAME,
        new OAuthTokenStoreHealthCheck(oauthTokenStore));
  }
}
