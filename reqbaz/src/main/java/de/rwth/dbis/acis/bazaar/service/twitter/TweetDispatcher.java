package de.rwth.dbis.acis.bazaar.service.twitter;

import java.util.Arrays;

import i5.las2peer.logging.L2pLogger;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.others.BearerToken;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.signature.Scope;
import io.github.redouane59.twitter.signature.TwitterCredentials;

public class TweetDispatcher {

    private final L2pLogger logger = L2pLogger.getInstance(TweetDispatcher.class.getName());

    private final String clientId;
    private final String clientSecret;

    // Keep this in-memory for now
    // Next step: -> store token in database
    private BearerToken tempBearerToken;

    public TweetDispatcher(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        logger.info("Twitter clientId: " + clientId);
        logger.info("Twitter clientSecret: " + clientSecret);
    }

    /**
     * Publishes a plain text Tweet.
     *
     * @param text the text of the Tweet
     */
    public void publishTweet(String text) {
        TwitterClient twitterClient = createAuthenticatedTwitterClient();
        logger.info("Publishing Tweet: " + text);

        final TweetParameters tweetParameters = TweetParameters.builder()
                .text(text)
                .build();

        TwitterClientBearerTokenRequestSupport.postTweet(twitterClient, tweetParameters);
    }

    // TODO Should this be more dynamic (e.g., some encoding of user who started auth process) ?
    private static final String CODE_CHALLENGE = "req-baz-challenge";

    public String getAuthorizationUrl(String redirectUri) {
        TwitterClient twitterClient = createTwitterClientForAuthentication();

        return twitterClient.getRequestHelperV2().getAuthorizeUrl(clientId, redirectUri,
                "state",
                CODE_CHALLENGE,
                "plain",
                Arrays.asList(Scope.TWEET_READ, Scope.TWEET_WRITE, Scope.USERS_READ, Scope.OFFLINE_ACCESS));
    }

    public void handleAuthCallback(String redirectUri, String code) {
        TwitterClient twitterClient = createTwitterClientForAuthentication();

        // BearerToken bearerToken = twitterClient.getOAuth2AccessToken(clientId, clientSecret, code, CODE_CHALLENGE, redirectUri);
        // workaround because above method has NO SUPPORT for clientSecret
        BearerToken bearerToken = TwitterClientSecretSupport.getOAuth2AccessTokenWithSecret(twitterClient,
                clientId, clientSecret, code, CODE_CHALLENGE, redirectUri);

        logger.info("Twitter accessToken: " + bearerToken.getAccessToken());
        logger.info("Twitter refreshToken: " + bearerToken.getRefreshToken());

        // store in-memory
        this.tempBearerToken = bearerToken;
        // TODO Store in database
    }

    /**
     * Create a {@link TwitterClient} to be used for the authentication process (get access token etc.)
     *
     * @return
     */
    private TwitterClient createTwitterClientForAuthentication() {
        return new TwitterClient(TwitterCredentials.builder()
                .apiKey("gMPXcP40Nf2Fm2pqOb8Nd4MNR")
                .apiSecretKey("pdC8TwRxiw50DglEvFIt7ryIkW56Bq4y0UfLEHp5wtdHYzZhQ2")
                .build());
    }

    /**
     * Create {@link TwitterClient} which can make authenticated requests on behalf of the
     * ReqBaz Twitter account.
     *
     * @return
     */
    private TwitterClient createAuthenticatedTwitterClient() {
        /*
         * TODO Check expired and refresh if necessary.
         */
        return new TwitterClient(TwitterCredentials.builder()
                .apiKey("gMPXcP40Nf2Fm2pqOb8Nd4MNR")
                        .apiSecretKey("pdC8TwRxiw50DglEvFIt7ryIkW56Bq4y0UfLEHp5wtdHYzZhQ2")
                .accessToken(tempBearerToken.getAccessToken())
                .build());
    }
}
