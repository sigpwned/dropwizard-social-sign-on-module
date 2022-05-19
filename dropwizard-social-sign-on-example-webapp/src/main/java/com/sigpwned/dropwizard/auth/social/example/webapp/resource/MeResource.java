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
package com.sigpwned.dropwizard.auth.social.example.webapp.resource;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import com.sigpwned.dropwizard.auth.social.example.webapp.linting.VisibleForTesting;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.Account;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.CurrentUser;
import com.sigpwned.dropwizard.auth.social.twitter.oauth1.TwitterOAuth1;

/**
 * A simple example endpoint that returns the current user. Note that the class is annotated with
 * {@link PermitAll}, which requires all calls to this endpoint to include credentials.
 */
@PermitAll
@Path("/me")
public class MeResource {
  @Context
  @VisibleForTesting
  SecurityContext context;

  private String consumerKey;

  private String consumerSecret;

  @Inject
  public MeResource(@Named(TwitterOAuth1.TWITTER_OAUTH1_CONSUMER_KEY_NAMED) String consumerKey,
      @Named(TwitterOAuth1.TWITTER_OAUTH1_CONSUMER_SECRET_NAMED) String consumerSecret) {
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public CurrentUser getMe() {
    Account account = (Account) context.getUserPrincipal();
    // TODO Create twitter4j client
    return CurrentUser.of(account.getAccessToken(), account.getAccessToken(),
        account.getAccessToken());
  }
}
