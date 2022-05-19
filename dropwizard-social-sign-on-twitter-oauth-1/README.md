# DROPWIZARD SOCIAL SIGN ON TWITTER OAUTH 1

This module contains a Dropwizard bundle that implements the Twitter OAuth 1.0a three-legged authentication flow.

## How do I add social sign on to my app?

To add the Twitter OAuth 1.0a three-legged OAuth flow to an application:

    public class ExampleWebapp extends Application<ExampleConfiguration> {
      public static void main(String[] args) throws Exception {
        new ExampleWebapp().run(args);
      }

      @Override
      public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        // ADD THIS METHOD TO YOUR WEBAPP
        bootstrap.addBundle(new TwitterOAuth1Bundle<ExampleConfiguration>() {
          @Override
          protected TwitterOAuth1TokenStore getTokenStore(ExampleConfiguration configuration) {
            // You need to implement this yourself. It is used to store temporary tokens used during
            // the OAuth flow. A webapp with one webserver can use a simple in-memory store.
            return new MyTwitterOAuth1TokenStore(configuration);
          }

          @Override
          protected TwitterOAuth1AuthenticatedHandler getAuthenticatedHandler(
              ExampleConfiguration configuration) {
            // You need to implement this yourself, too. It is called when new access tokens have
            // been received from Twitter. Typically, users store the new access tokens, set a
            // pcookie, and redirect the user back to the webapp.
            return new MyTwitterOAuth1AuthenticatedHandler(configuration);
          }

          /**
           * This gives the base URL used to generate OAuth callback URLs. Don't forget to register your
           * callback URLs on the networks you're authenticating to! You can use HTTP or HTTPS links
           * here, but obviously HTTPS is preferred. Obviously, you should use your actual domain here!
           */
          @Override
          protected String getBaseUrl(ExampleConfiguration configuration) {
            return "https://www.example.com";
          }
        });
      }

      // The rest of your webapp
    }

You'll also need to make sure your configuration returns some basic OAuth information associated with your developer app on the Twitter platform, i.e., the consumer key and consumer secret, using the `TwitterOAuth1BundleConfiguration` interface. See [the example webapp](https://github.com/sigpwned/dropwizard-social-sign-on-module/tree/main/dropwizard-social-sign-on-example-webapp) for an example.

## FAQ

### What are my OAuth callback URLs?

You will need to register your OAuth callback URLs before the application will work. The callback URLs to register depends on the base URL your application provides to the `TwitterOAuth1Bundle`:

    bootstrap.addBundle(new TwitterOAuth1Bundle<ExampleConfiguration>() {
    
      // Other methods here...
    
      @Override
      protected String getBaseUrl(ExampleConfiguration configuration) {
        // BASE URL GOES HERE
        return "https://www.example.com";
      }
    });

The value you rturn must be an absolute URL that does not end with a slash. In general, if your application is hosted on `www.example.com`, then you should just return `https://www.example.com`. In this case, the callback URL to register would be:

    https://www.example.com/oauth/twitter/1/callback

If you want to use an additional URL prefix, then you can add it to the end of the base URL. For example, if you used `https://www.example.com/prefix/goes/here` as the base URL, then your callback URL would be `https://www.example.com/prefix/goes/here/oauth/twitter/1/callback`.

### The request token call of the flow is failing. What's going on?

The most common cause of this issue is using an unapproved callback URL. [The Twitter documentation](https://developer.twitter.com/en/docs/apps/callback-urls) has a good description of how to approve callback URLs.