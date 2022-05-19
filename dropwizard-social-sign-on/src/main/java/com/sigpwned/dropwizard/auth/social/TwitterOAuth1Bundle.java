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
package com.sigpwned.dropwizard.auth.social;

import java.util.EnumSet;
import java.util.Optional;
import javax.servlet.DispatcherType;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1AuthenticatedHandler;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1BundleConfiguration;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1Configuration;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1HttpFilter;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1TokenStore;
import com.sigpwned.dropwizard.auth.social.util.SocialAuth;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;

public abstract class TwitterOAuth1Bundle<C extends TwitterOAuth1BundleConfiguration>
    implements ConfiguredBundle<C> {

  public static final String FILTER_NAME = "SocialAuthTwitterOAuth1";

  @Override
  public void run(C configuration, Environment environment) throws Exception {
    final TwitterOAuth1Configuration toa1 = configuration.getTwitterOAuth1Configuration();

    final String consumerKey = toa1.getConsumerKey();

    final String consumerSecret = toa1.getConsumerSecret();

    final String baseUrl =
        Optional.ofNullable(getBaseUrl(configuration)).orElse(SocialAuth.DEFAULT_BASE_URL);

    final TwitterOAuth1TokenStore tokenStore = getTokenStore(configuration);

    final TwitterOAuth1AuthenticatedHandler authenticatedHandler =
        getAuthenticatedHandler(configuration);

    // Register everything for dependency injection. We create our filter instance below, but this
    // is a good best practice in case these things are needed elsewhere.
    environment.jersey().register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(baseUrl).to(String.class).named(SocialAuth.BASE_URL_NAMED);
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
  }

  /**
   * By default, we just serve off the root path. May be empty. If not empty, then must start with a
   * forward slash, and not end with a forward slash. Examples: "/example", "/path/to/social/auth".
   */
  protected String getBaseUrl(C configuration) {
    return SocialAuth.DEFAULT_BASE_URL;
  }

  protected abstract TwitterOAuth1TokenStore getTokenStore(C configuration);

  protected abstract TwitterOAuth1AuthenticatedHandler getAuthenticatedHandler(C configuration);
}
