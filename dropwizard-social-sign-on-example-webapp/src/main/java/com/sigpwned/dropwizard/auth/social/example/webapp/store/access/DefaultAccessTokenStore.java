package com.sigpwned.dropwizard.auth.social.example.webapp.store.access;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sigpwned.dropwizard.auth.social.example.webapp.AccessTokenStore;

public class DefaultAccessTokenStore implements AccessTokenStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAccessTokenStore.class);

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

  public DefaultAccessTokenStore(String username, String password) {
    this.username = username;
    this.password = password;
    this.tokens = new ConcurrentHashMap<>();
  }

  @Override
  public void putTwitterOAuth1AccessToken(String accessToken, String accessTokenSecret) {
    if (LOGGER.isInfoEnabled())
      LOGGER.info("Storing Twitter OAuth 1.0a access token {} / {}", accessToken,
          accessTokenSecret);
    tokens.put(accessToken, accessTokenSecret);
  }

  @Override
  public Optional<String> getTwitterOAuth1AccessTokenSecret(String accessToken) {
    return Optional.ofNullable(tokens.get(accessToken));
  }
}
