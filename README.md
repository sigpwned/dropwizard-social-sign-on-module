# DROPWIZARD SOCIAL SIGN ON MODULE [![tests](https://github.com/sigpwned/dropwizard-social-sign-on-module/actions/workflows/tests.yml/badge.svg)](https://github.com/sigpwned/dropwizard-social-sign-on-module/actions/workflows/tests.yml) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=sigpwned_dropwizard-social-sign-on-module&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=sigpwned_dropwizard-social-sign-on-module) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=sigpwned_dropwizard-social-sign-on-module&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=sigpwned_dropwizard-social-sign-on-module) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=sigpwned_dropwizard-social-sign-on-module&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=sigpwned_dropwizard-social-sign-on-module) 

dropwizard-social-sign-on-module adds social sign on [OAuth](https://oauth.net/) flows for the most popular social networks to Dropwizard 3+.

## Goals

* Provide OAuth authentication flows to Dropwizard
* Support the most popular social networks

## Non-Goals

* Support web frameworks other than Dropwizard
* Support all social networks

## What is OAuth

OAuth is an open protocol to allow secure authorization in a simple and standard method from web, mobile and desktop applications.

## What is an OAuth Flow?

An OAuth flow is a web-based authentication process by which a third party can authenticate and receive permission from a user to retrieve credentials that allow the third party to perform actions on behalf of the user. For example, an app can use OAuth to retrieve an access token from Twitter that allows the app to send tweets on behalf of the user.

## How do I add social sign on to my app?

Individual OAuth flows are packaged as standalone Dropwizard bundles. To add social sign on to your application, you need to add the corresponding bundle to your application. For example, this code adds the Twitter OAuth 1.0a three-legged OAuth flow to an application:

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

You'll also need to make sure your configuration returns some basic OAuth information associated with your developer app on the Twitter platform, i.e., the consumer key and consumer secret.

Other bundles are likely to have different, but similar, requirements.

Detailed information about each bundle can be found in their respective modules of this project.

## What does the bundle add to my webapp?

Each bundle adds the following features to your webapp "out of the box":

* All the web resources to implement the corresponding OAuth flow. These are packaged as a servlet `HttptFilter`, so they will not interfere with the rest of the web application. Note that in order for the OAuth flows to work, your Dropwizard `applicationContextPath` must be `/`, but your `rootPath` can be anything.
* Provides hooks for collecting new OAuth credentials as they are generated

## Where can I find an example?

You can find a [SSCCE](http://sscce.org/) Dropwizard webapp in this repository in [the dropwizard-social-sign-on-example-webapp module](https://github.com/sigpwned/dropwizard-social-sign-on-module/tree/main/dropwizard-social-sign-on-example-webapp).