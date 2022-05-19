# DROPWIZARD SOCIAL SIGN ON EXAMPLE WEBAPP

This module contains an [SSCCE](http://sscce.org/) example webapp that supports social sign on using the Twitter OAuth 1.0a flow. The following examples demonstrate the features provided by the dropwizard-social-sign-on-twitter-oauth-1 bundle and framework code in the example.

## Running the webapp

First, clone the repository. Next, run the following command in the repository root directory:

    $ mvn clean compile install

Next, change to the `dropwizard-social-sign-on-example-webapp` directory:

    $ cd dropwizard-social-sign-on-example-webapp

Finally, run the webapp using:

    $ TWITTER_OAUTH1_CONSUMER_KEY=$TwitterAppConsumerKey TWITTER_OAUTH1_CONSUMER_SECRET=$TwitterAppConsumerSecret java -jar target/dropwizard-social-sign-on-example-webapp.jar server config.yml

You will need to replace `$TwitterAppConsumerKey` and `$TwitterAppConsumerSecret` with your Twitter app's consumer key and consumer secret, respectively. [This answer on StackOverflow](https://stackoverflow.com/a/6875024/2103602) has a good explanation of how to get these values. Note that your app may need elevated permissions to access the Twitter 1.0a auth flow.

## Example webapp calls

Opening the following URL in your browser:

    http://localhost:8080/oauth/twitter/1/authenticate

will kick off an OAuth flow. If it is successful, then you will be redirected to a URL like the following:

    http://localhost:8080/v1/me?token=123456789-OPzHT0ReNUPCLjselrkjcljKSeljkrljkcLKJSEl

that will show some basic details of your Twitter account:

    {
      "id": 119637086,
      "screenName": "sigpwned",
      "name": "Andy Boothe 👨‍💻"
    }

Because this is an example webapp, the access token and access token secret are also printed to the log in a message that looks like this:

    INFO  [2022-05-19 19:01:48,156] com.sigpwned.dropwizard.auth.social.example.webapp.store.access.DefaultAccessTokenStore: Storing Twitter OAuth 1.0a access token TwitterAccessToken [userId=119637086, accessToken=123456789-OPzHT0ReNUPCLjselrkjcljKSeljkrljkcLKJSEl, accessTokenSecret=zkELKjclksje9328CLkjselkj4809sck38Cslkerjc88b]

In this way, you can use this example app as a way to collect access tokens for local development purposes in a pinch. Of course, you should *never* print secrets to the log in a production application.

## FAQ

### The request token call of the flow is failing. What's going on?

The most common cause of this issue is using an unapproved callback URL. [The Twitter documentation](https://developer.twitter.com/en/docs/apps/callback-urls) has a good description of how to approve callback URLs.