package de.rwth.dbis.acis.bazaar.service.twitter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.NoSuchElementException;

import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.LinkedTwitterAccount;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
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

    // Keep this in-memory
    private LinkedTwitterAccount linkedTwitterAccount;

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
    public void publishTweet(DALFacade dalFacade, String text) throws BazaarException {
        TwitterClient twitterClient = createAuthenticatedTwitterClient(dalFacade);
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

    public void handleAuthCallback(DALFacade dalFacade, String redirectUri, String code) throws Exception {
        TwitterClient twitterClient = createTwitterClientForAuthentication();

        // BearerToken bearerToken = twitterClient.getOAuth2AccessToken(clientId, clientSecret, code, CODE_CHALLENGE, redirectUri);
        // workaround because above method has NO SUPPORT for clientSecret
        BearerToken bearerToken = TwitterClientSecretSupport.getOAuth2AccessTokenWithSecret(twitterClient,
                clientId, clientSecret, code, CODE_CHALLENGE, redirectUri);

        logger.info("Twitter accessToken: " + bearerToken.getAccessToken());
        logger.info("Twitter refreshToken: " + bearerToken.getRefreshToken());

        // store in-memory
        OffsetDateTime now = OffsetDateTime.now();
        LinkedTwitterAccount linkedTwitterAccount = LinkedTwitterAccount.builder()
                .linkedByUserId(424242) // TODO Use from OAuth challenge
                .creationDate(now)
                .lastUpdatedDate(now)
                .accessToken(bearerToken.getAccessToken())
                .refreshToken(bearerToken.getRefreshToken())
                .expirationDate(now.plus(bearerToken.getExpiresIn(), ChronoUnit.SECONDS))
                .build();

        // store in database (overrides previous linked account)
        dalFacade.replaceLinkedTwitterAccount(linkedTwitterAccount);
        this.linkedTwitterAccount = linkedTwitterAccount;
    }

    /**
     * Create a {@link TwitterClient} to be used for the authentication process (get access token etc.)
     *
     * @return
     */
    private TwitterClient createTwitterClientForAuthentication() {
        return new TwitterClient(TwitterCredentials.builder()
                // TODO DO NOT HARDCODE!
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
    private TwitterClient createAuthenticatedTwitterClient(DALFacade dalFacade) throws BazaarException {
        if (linkedTwitterAccount == null) {
            linkedTwitterAccount = dalFacade.getLinkedTwitterAccount()
                    .orElseThrow(() -> new NoSuchElementException("No linked Twitter account"));
        }

        /*
         * TODO Check expired and refresh if necessary.
         */
        return new TwitterClient(TwitterCredentials.builder()
                // TODO DO NOT HARDCODE!
                .apiKey("gMPXcP40Nf2Fm2pqOb8Nd4MNR")
                        .apiSecretKey("pdC8TwRxiw50DglEvFIt7ryIkW56Bq4y0UfLEHp5wtdHYzZhQ2")
                .accessToken(linkedTwitterAccount.getAccessToken())
                .build());
    }
}
