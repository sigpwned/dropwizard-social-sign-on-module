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
package com.sigpwned.dropwizard.auth.social.twitter.oauth1;

import java.io.IOException;
import java.util.Optional;

/**
 * Stores and retrieves OAuth tokens secrets during the flow process. These are part of the OAuth
 * flow and are different from access tokens themselves. A standalone webserver could use an
 * in-memory store, but a horizontally-scaled webserver would need to store these in some shared
 * data store. Records should be stored for at least an hour, but do not need to be stored forever.
 */
public interface TwitterOAuth1TokenStore {
  public void putTwitterOAuth1TokenSecret(String token, String tokenSecret) throws IOException;

  public Optional<String> getTwitterOAuth1TokenSecret(String token) throws IOException;
}
