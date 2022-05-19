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
package com.sigpwned.dropwizard.auth.social.example.webapp.health;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.Test;
import com.codahale.metrics.health.HealthCheck;
import com.sigpwned.dropwizard.auth.social.example.webapp.AccessTokenStore;

public class AccessTokenStoreHealthCheckTest {
  @Test
  public void shouldBeHealthyIfNoException() throws Exception {
    AccessTokenStore store = mock(AccessTokenStore.class);
    when(store.getTwitterOAuth1AccessTokenSecret(any(String.class))).thenReturn(Optional.empty());

    AccessTokenStoreHealthCheck unit = new AccessTokenStoreHealthCheck(store);

    HealthCheck.Result result = unit.check();

    assertThat(result.isHealthy(), is(true));
  }

  @Test
  public void shouldBeUnhealthyIfException() throws Exception {
    AccessTokenStore store = mock(AccessTokenStore.class);
    when(store.getTwitterOAuth1AccessTokenSecret(any(String.class)))
        .thenThrow(new RuntimeException("simulated failure"));

    AccessTokenStoreHealthCheck unit = new AccessTokenStoreHealthCheck(store);

    HealthCheck.Result result = unit.check();

    assertThat(result.isHealthy(), is(false));
  }
}
