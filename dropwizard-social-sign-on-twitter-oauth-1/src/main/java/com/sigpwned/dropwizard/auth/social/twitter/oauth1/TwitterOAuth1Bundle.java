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
package com.sigpwned.dropwizard.auth.social.twitter.oauth1;

import java.util.EnumSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.configuration.TwitterOAuth1Configuration;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.health.TwitterOAuth1TokenStoreHealthCheck;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.util.TwitterOAuth1;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;
import jakarta.servlet.DispatcherType;

public abstract class TwitterOAuth1Bundle<C extends TwitterOAuth1BundleConfiguration>
    implements ConfiguredBundle<C> {

  public static final String FILTER_NAME = "SocialAuthTwitterOAuth1";

  @Override
  public void run(C configuration, Environment environment) throws Exception {
    final TwitterOAuth1Configuration toa1 = configuration.getTwitterOAuth1Configuration();

    final String consumerKey = toa1.getConsumerKey();
    if (consumerKey == null)
      throw new IllegalStateException("no consumer key");

    final String consumerSecret = toa1.getConsumerSecret();
    if (consumerSecret == null)
      throw new IllegalStateException("no consumer secret");

    final String baseUrl = getBaseUrl(configuration);
    if (baseUrl == null)
      throw new IllegalStateException("no baseUrl");

    final TwitterOAuth1TokenStore tokenStore = getTokenStore(configuration);

    final TwitterOAuth1AuthenticatedHandler authenticatedHandler =
        getAuthenticatedHandler(configuration);

    // Register everything for dependency injection. We create our filter instance below, but this
    // is a good best practice in case these things are needed elsewhere.
    environment.jersey().register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(baseUrl).to(String.class).named(TwitterOAuth1.TWITTER_OAUTH_1_BASE_URL_NAMED);
        bind(consumerKey).to(String.class).named(TwitterOAuth1.TWITTER_OAUTH1_CONSUMER_KEY_NAMED);
        bind(consumerSecret).to(String.class)
            .named(TwitterOAuth1.TWITTER_OAUTH1_CONSUMER_SECRET_NAMED);
        bind(tokenStore).to(TwitterOAuth1TokenStore.class);
        bind(authenticatedHandler).to(TwitterOAuth1AuthenticatedHandler.class);
      }
    });

    // Register our filter.
    environment.servlets()
        .addFilter(FILTER_NAME,
            new TwitterOAuth1HttpFilter(baseUrl, consumerKey, consumerSecret, tokenStore,
                authenticatedHandler))
        .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");

    // Register the health check for our oauth token store
    environment.healthChecks().register(TwitterOAuth1TokenStoreHealthCheck.NAME,
        new TwitterOAuth1TokenStoreHealthCheck(tokenStore));
  }

  /**
   * This is used to generate OAuth callback URLs. It must be a full absolute URL that does not end
   * with a slash. In general, if your domain is hosted on www.example.com, then you should just use
   * "https://www.example.com", which would map your callback URLs to, e.g.,
   * "https://www.example.com/oauth/twitter/1/callback". However, if you want to use a different
   * prefix, you can add it to the end of this value. For example,
   * "https://www.example.com/prefix/goes/here" would change the callback URL to
   * "https://www.example.com/prefix/goes/here/oauth/twitter/1/callback".
   */
  protected abstract String getBaseUrl(C configuration);

  /**
   * This is used to store temporary OAuth token credentials during the OAuth flow only. Proper
   * access token credentials never flow through this store. A webapp with a single web server could
   * use an in-memory store, but most applications should use a shared data store like a database or
   * memcache/redis. Credentials only need to be stored for at most an hour.
   */
  protected abstract TwitterOAuth1TokenStore getTokenStore(C configuration);

  /**
   * This is notified when the application receives new access token credentials. These credentials
   * are permanent user credentials that should be stored persistently and securely on a permanent
   * basis. It should also return a reasonable HTTP response to send as a response to the user,
   * which is typically a redirect with a set-cookie header.
   */
  protected abstract TwitterOAuth1AuthenticatedHandler getAuthenticatedHandler(C configuration);
}
