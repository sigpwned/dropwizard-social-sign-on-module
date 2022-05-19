/*-
 * =================================LICENSE_START==================================
 * dropwizard-social-sign-on-example-webapp
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
package com.sigpwned.dropwizard.auth.social.example.webapp.model;

import java.security.Principal;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import twitter4j.User;

/**
 * This is our application user model object
 */
public class TwitterAccount implements Principal {
  public static TwitterAccount fromUser(User u) {
    return of(u.getId(), u.getScreenName(), u.getName());
  }

  @JsonCreator
  public static TwitterAccount of(@JsonProperty("id") long id,
      @JsonProperty("screenName") String screenName, @JsonProperty("name") String name) {
    return new TwitterAccount(id, screenName, name);
  }

  @JsonProperty
  private final long id;

  @JsonProperty
  private final String screenName;

  @JsonProperty
  private final String name;

  public TwitterAccount(long id, String screenName, String name) {
    this.id = id;
    this.screenName = screenName;
    this.name = name;
  }

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @return the screenName
   */
  public String getScreenName() {
    return screenName;
  }

  /**
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, screenName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TwitterAccount other = (TwitterAccount) obj;
    return id == other.id && Objects.equals(name, other.name)
        && Objects.equals(screenName, other.screenName);
  }

  @Override
  public String toString() {
    return "TwitterAccount [id=" + id + ", screenName=" + screenName + ", name=" + name + "]";
  }
}
