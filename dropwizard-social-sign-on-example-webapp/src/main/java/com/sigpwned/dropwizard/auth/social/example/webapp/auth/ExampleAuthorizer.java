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
package com.sigpwned.dropwizard.auth.social.example.webapp.auth;

import javax.ws.rs.container.ContainerRequestContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.sigpwned.dropwizard.auth.social.example.webapp.model.Account;
import io.dropwizard.auth.Authorizer;

/**
 * We use a trivially simple authorization model. A real application might include more information
 * on the account and make authorization decisions on that basis.
 */
public class ExampleAuthorizer implements Authorizer<Account> {
  @Override
  public boolean authorize(Account principal, String role,
      @Nullable ContainerRequestContext requestContext) {
    // We have no roles. Everyone can do everything.
    return true;
  }
}
