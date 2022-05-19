package com.sigpwned.dropwizard.auth.social.example.webapp;

import java.util.Optional;

public interface AccessTokenStore {
  public void putTwitterOAuth1AccessToken(String accessToken, String accessTokenSecret);

  public Optional<String> getTwitterOAuth1AccessTokenSecret(String accessToken);
}
