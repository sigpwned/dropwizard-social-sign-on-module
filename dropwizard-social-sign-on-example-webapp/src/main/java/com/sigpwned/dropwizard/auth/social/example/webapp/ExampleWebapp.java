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
import javax.ws.rs.InternalServerErrorException;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import com.sigpwned.dropwizard.auth.social.example.webapp.auth.ExampleAuthenticator;
import com.sigpwned.dropwizard.auth.social.example.webapp.auth.ExampleAuthorizer;
import com.sigpwned.dropwizard.auth.social.example.webapp.health.AccessTokenStoreHealthCheck;
import com.sigpwned.dropwizard.auth.social.example.webapp.health.SessionStoreHealthCheck;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.TwitterAccount;
import com.sigpwned.dropwizard.auth.social.example.webapp.resource.MeResource;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1AuthenticatedHandler;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1Bundle;
import com.sigpwned.httpmodel.ModelHttpHeaders;
import com.sigpwned.httpmodel.ModelHttpQueryString;
import com.sigpwned.httpmodel.ModelHttpResponse;
import com.sigpwned.httpmodel.util.ModelHttpHeaderNames;
import com.sigpwned.httpmodel.util.ModelHttpStatusCodes;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * This is an example Dropwizard webapp implementation using the social sign on module. It only
 * registers the Twitter OAuth 1.0a module, but it should be clear how to use other bundles based on
 * this sample. If you just need to generate Twitter OAuth 1.0a access tokens for your application,
 * then you can use this out of the box.
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
       * get to see it again) and then we just redirect the user to another page with our token in a
       * query parameter. (Note that the existing TwitterOAuth1TokenStore is for temporary token
       * secrets used during the OAuth flow and not for proper access tokens.) In a real
       * application, you'd probably set a cookie with a proper session token or JWT instead of just
       * using the access token, for example, but for this demo, this will do just fine. We use a
       * query parameter just because this example typically runs on localhost, and Cookie rules for
       * localhost are wonky. We don't share the token secret, so it's not totally broken.
       */
      @Override
      protected TwitterOAuth1AuthenticatedHandler getAuthenticatedHandler(
          ExampleConfiguration configuration) {
        final String consumerKey = configuration.getTwitterOAuth1Configuration().getConsumerKey();
        final String consumerSecret =
            configuration.getTwitterOAuth1Configuration().getConsumerSecret();
        final AccessTokenStore tokenStore = configuration.getAccessTokenStore().build();
        final SessionStore sessionStore = configuration.getSessionStore().build();
        return new TwitterOAuth1AuthenticatedHandler() {
          @Override
          public ModelHttpResponse twitterOAuth1Authenticated(String accessToken,
              String accessTokenSecret) throws IOException {
            // Look up our Twitter user. We want to know who just authenticated our stuff!
            User user;
            try {
              user = new TwitterFactory(new ConfigurationBuilder().setOAuthConsumerKey(consumerKey)
                  .setOAuthConsumerSecret(consumerSecret).setOAuthAccessToken(accessToken)
                  .setOAuthAccessTokenSecret(accessTokenSecret).build()).getInstance()
                      .verifyCredentials();
            } catch (TwitterException e) {
              throw new InternalServerErrorException("Failed to verify new Twitter credentials", e);
            }

            // Create a session for our user using their access token. In a real application, you
            // might use stateless JWTs or a proper session store.
            sessionStore.putSession(accessToken, TwitterAccount.fromUser(user));

            // Store our access token secret for later. We don't do anything else with it, but
            // obviously in a real application you'd want to keep these credentials stored securely!
            tokenStore.putTwitterOAuth1AccessToken(user.getId(), accessToken, accessTokenSecret);

            // Again, normally, you'd probably want to use a cookie, but for a demo, this is just
            // fine, and it avoids Cookie nonsense on localhost.
            return ModelHttpResponse.of(ModelHttpStatusCodes.TEMPORARY_REDIRECT,
                ModelHttpHeaders.of(ModelHttpHeaders.Header.of(ModelHttpHeaderNames.LOCATION,
                    "/v1/me" + "?"
                        + ModelHttpQueryString
                            .of(ModelHttpQueryString.Parameter.of("token", accessToken)))),
                Optional.empty());
          }
        };
      }

      /**
       * This gives the base URL used to generate OAuth callback URLs. Don't forget to register your
       * callback URLs on the networks you're authenticating to!
       */
      @Override
      protected String getBaseUrl(ExampleConfiguration configuration) {
        return configuration.getSocialAuth().getBaseUrl();
      }
    });
  }

  @Override
  public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
    // We'll want these for health checks
    OAuthTokenStore oauthTokenStore = configuration.getSocialAuth().getOAuthTokenStore().build();
    AccessTokenStore accessTokenStore = configuration.getAccessTokenStore().build();
    SessionStore sessionStore = configuration.getSessionStore().build();

    // Our OAuthTokenStore instance has been registered many times under other types, but register
    // it under the umbrella OAuthTokenStore type now. Our AccessTokenStore and SessionStore have
    // never been registered, so register them here now.
    environment.jersey().register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(oauthTokenStore).to(OAuthTokenStore.class);
        bind(accessTokenStore).to(AccessTokenStore.class);
        bind(sessionStore).to(SessionStore.class);
      }
    });

    // Set up our authentication. We treat access tokens as credentials.
    environment.jersey()
        .register(new AuthDynamicFeature(AccessTokenAuthFilter.<TwitterAccount>builder()
            .setRealm("Example").setAuthenticator(new ExampleAuthenticator(sessionStore))
            .setAuthorizer(new ExampleAuthorizer()).buildAuthFilter()));

    // Our application's resources are simple: you can ask who you are. The Twitter OAuth resources
    // are automatically registered by the bundle.
    environment.jersey().register(MeResource.class);

    // Make sure our account store is healthy
    environment.healthChecks().register(AccessTokenStoreHealthCheck.NAME,
        new AccessTokenStoreHealthCheck(accessTokenStore));
    environment.healthChecks().register(SessionStoreHealthCheck.NAME,
        new SessionStoreHealthCheck(sessionStore));
  }
}
