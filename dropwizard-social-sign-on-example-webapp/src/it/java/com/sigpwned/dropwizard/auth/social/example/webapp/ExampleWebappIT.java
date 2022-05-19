package com.sigpwned.dropwizard.auth.social.example.webapp;

import static java.util.Collections.unmodifiableMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.NotAuthorizedException;
import org.junit.ClassRule;
import org.junit.Test;
import io.dropwizard.testing.junit.DropwizardAppRule;

@SuppressWarnings("deprecation")
public class ExampleWebappIT {
  @ClassRule
  public static final DropwizardAppRule<ExampleConfiguration> RULE =
      new DropwizardAppRule<>(ExampleWebapp.class, "config.yml");

  @Test
  public void smokeTest() {
    assertThat(true, is(true));
  }

  @Test(expected = NotAuthorizedException.class)
  public void shouldFailIfNoCredentialsSuccessfully() throws Exception {
    httpGet("/v1/me");
  }

  @Test(expected = NotAuthorizedException.class)
  public void shouldFailIfInvalidCredentialsSuccessfully() throws Exception {
    httpGet("/v1/me?token=foobar");
  }

  private static class HttpResponse {
    public static HttpResponse of(String body, Map<String, String> headers) {
      return new HttpResponse(body, headers);
    }

    private final String body;
    private final Map<String, String> headers;

    public HttpResponse(String body, Map<String, String> headers) {
      this.body = body;
      this.headers = unmodifiableMap(headers);
    }

    /**
     * @return the body
     */
    public String getBody() {
      return body;
    }

    /**
     * @return the headers
     */
    public Map<String, String> getHeaders() {
      return headers;
    }

    @Override
    public int hashCode() {
      return Objects.hash(body, headers);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      HttpResponse other = (HttpResponse) obj;
      return Objects.equals(body, other.body) && Objects.equals(headers, other.headers);
    }

    @Override
    public String toString() {
      final int maxLen = 10;
      return "HttpResponse [body=" + body + ", headers="
          + (headers != null ? toString(headers.entrySet(), maxLen) : null) + "]";
    }

    private String toString(Collection<?> collection, int maxLen) {
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      int i = 0;
      for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
        if (i > 0)
          builder.append(", ");
        builder.append(iterator.next());
      }
      builder.append("]");
      return builder.toString();
    }
  }

  private HttpResponse httpGet(String path) throws IOException {
    URI uri = URI.create(String.format("http://localhost:%d%s", RULE.getLocalPort(), path));

    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();

    java.net.http.HttpResponse<String> response;
    try {
      response = client.send(HttpRequest.newBuilder().GET().uri(uri).build(),
          BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException();
    }

    if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
      throw new NotAuthorizedException("challenge");
    if (response.statusCode() != HttpURLConnection.HTTP_OK)
      throw new IOException("request failed: " + response.statusCode());

    String responseBody = response.body();

    Map<String, String> headers = new HashMap<>();
    for (Map.Entry<String, List<String>> e : response.headers().map().entrySet()) {
      if (e.getKey() != null) {
        headers.put(e.getKey().toLowerCase(), e.getValue().get(0));
      }
    }

    return HttpResponse.of(responseBody, headers);
  }
}
