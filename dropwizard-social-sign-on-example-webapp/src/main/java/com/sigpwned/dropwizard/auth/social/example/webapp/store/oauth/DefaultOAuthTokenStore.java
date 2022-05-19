package com.sigpwned.dropwizard.auth.social.example.webapp.store.oauth;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.sigpwned.dropwizard.auth.social.example.webapp.OAuthTokenStore;

public class DefaultOAuthTokenStore implements OAuthTokenStore {
  /**
   * This is an in-memory store, so we don't need this, but it shows how to pass configuraiton data
   * through the app.
   */
  @SuppressWarnings("unused")
  private final String username;

  /**
   * This is an in-memory store, so we don't need this, but it shows how to pass configuraiton data
   * through the app.
   */
  @SuppressWarnings("unused")
  private final String password;

  private final Map<String, String> tokens;

  public DefaultOAuthTokenStore(String username, String password) {
    this.username = username;
    this.password = password;
    this.tokens = new ConcurrentHashMap<>();
  }

  @Override
  public void putTwitterOAuth1TokenSecret(String token, String tokenSecret) throws IOException {
    tokens.put(token, tokenSecret);
  }

  @Override
  public Optional<String> getTwitterOAuth1TokenSecret(String token) throws IOException {
    return Optional.ofNullable(tokens.get(token));
  }
}
