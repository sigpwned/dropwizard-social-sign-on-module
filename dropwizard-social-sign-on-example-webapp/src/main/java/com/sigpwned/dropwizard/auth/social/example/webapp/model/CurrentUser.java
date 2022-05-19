package com.sigpwned.dropwizard.auth.social.example.webapp.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrentUser {
  @JsonCreator
  public static CurrentUser of(@JsonProperty("id") String id,
      @JsonProperty("screenName") String screenName, @JsonProperty("name") String name) {
    return new CurrentUser(id, screenName, name);
  }

  private final String id;
  private final String screenName;
  private final String name;

  public CurrentUser(String id, String screenName, String name) {
    this.id = id;
    this.screenName = screenName;
    this.name = name;
  }

  /**
   * @return the id
   */
  public String getId() {
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
    CurrentUser other = (CurrentUser) obj;
    return Objects.equals(id, other.id) && Objects.equals(name, other.name)
        && Objects.equals(screenName, other.screenName);
  }

  @Override
  public String toString() {
    return "CurrentUser [id=" + id + ", screenName=" + screenName + ", name=" + name + "]";
  }
}
