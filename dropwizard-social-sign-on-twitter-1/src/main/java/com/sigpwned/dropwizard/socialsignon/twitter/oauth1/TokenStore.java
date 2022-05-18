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

import java.io.IOException;
import java.util.Optional;

/**
 * Stores and retrieves OAuth tokens during the flow process. A reasonable implementation should
 * store these in a persistent data store. Records should be stored for at least an hour, but do not
 * need to be stored forever.
 */
public interface TokenStore {
  public void putTokenSecret(String token, String tokenSecret) throws IOException;

  public Optional<String> getTokenSecret(String token) throws IOException;
}
