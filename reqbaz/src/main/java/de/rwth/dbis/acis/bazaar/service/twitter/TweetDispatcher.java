package de.rwth.dbis.acis.bazaar.service.twitter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.NoSuchElementException;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.LinkedTwitterAccount;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import i5.las2peer.logging.L2pLogger;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.others.BearerToken;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.signature.Scope;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import org.apache.commons.lang3.StringUtils;

public class TweetDispatcher {

    private final L2pLogger logger = L2pLogger.getInstance(TweetDispatcher.class.getName());

    private final BazaarService bazaarService;
    private final String apiKey;
    private final String apiKeySecret;
    private final String clientId;
    private final String clientSecret;

    // Keep this in-memory
    private LinkedTwitterAccount linkedTwitterAccount;

    public TweetDispatcher(BazaarService bazaarService, String apiKey, String apiKeySecret, String clientId, String clientSecret) {
        this.bazaarService = bazaarService;
        this.apiKey = apiKey;
        this.apiKeySecret = apiKeySecret;
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        logger.info("Twitter apiKey: " + apiKey);
        logger.info("Twitter apiKeySecret: " + apiKeySecret);
        logger.info("Twitter clientId: " + clientId);
        logger.info("Twitter clientSecret: " + clientSecret);
        if (StringUtils.isAnyBlank(apiKey, apiKeySecret, clientId, clientSecret)) {
            logger.warning("Tweeting functionality cannot be used without Twitter API credentials!");
        }
    }

    /**
     * Publishes a plain text Tweet.
     *
     * @param text the text of the Tweet
     */
    public void publishTweet(String text) throws Exception {
        DALFacade dalFacade = bazaarService.getDBConnection();
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

    public void handleAuthCallback(String redirectUri, String code) throws Exception {
        DALFacade dalFacade = bazaarService.getDBConnection();
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
                .expirationDate(calcTokenExpirationDate(bearerToken))
                .build();

        // store in database (overrides previous linked account)
        dalFacade.replaceLinkedTwitterAccount(linkedTwitterAccount);
        this.linkedTwitterAccount = linkedTwitterAccount;
    }

    private OffsetDateTime calcTokenExpirationDate(BearerToken bearerToken) {
        return OffsetDateTime.now().plus(bearerToken.getExpiresIn(), ChronoUnit.SECONDS);
    }

    /**
     * Create a {@link TwitterClient} to be used for the authentication process (get access token etc.)
     *
     * @return
     */
    private TwitterClient createTwitterClientForAuthentication() {
        ensureCredentialsConfigured();

        return new TwitterClient(TwitterCredentials.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiKeySecret)
                .build());
    }

    /**
     * Returns whether the instance has a linked Twitter account to post tweets with.
     *
     * @return
     * @throws BazaarException
     */
    public boolean hasLinkedTwitterAccount() throws Exception {
        DALFacade dalFacade = bazaarService.getDBConnection();
        return dalFacade.getLinkedTwitterAccount().isPresent();
    }

    /**
     * Create {@link TwitterClient} which can make authenticated requests on behalf of the
     * ReqBaz Twitter account.
     *
     * @return
     */
    private TwitterClient createAuthenticatedTwitterClient(DALFacade dalFacade) throws Exception {
        ensureCredentialsConfigured();

        if (linkedTwitterAccount == null) {
            linkedTwitterAccount = dalFacade.getLinkedTwitterAccount()
                    .orElseThrow(() -> new NoSuchElementException("No linked Twitter account"));
        }

        if (linkedTwitterAccount.isTokenExpired()) {
            logger.info("Twitter access token is expired. Refreshing...");
            refreshAccessToken(dalFacade);
        }

        return new TwitterClient(TwitterCredentials.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiKeySecret)
                .accessToken(linkedTwitterAccount.getAccessToken())
                .build());
    }

    private void refreshAccessToken(DALFacade dalFacade) throws Exception {
        TwitterClient twitterClient = createTwitterClientForAuthentication();

        BearerToken bearerToken = TwitterClientSecretSupport.refreshOAuth2AccessTokenWithSecret(twitterClient,
                clientId, clientSecret, linkedTwitterAccount.getRefreshToken());

        linkedTwitterAccount.updateToken(bearerToken.getAccessToken(), bearerToken.getRefreshToken(),
                calcTokenExpirationDate(bearerToken));

        dalFacade.updateLinkedTwitterAccount(linkedTwitterAccount);
        logger.info("Twitter access token refreshed successfully");
    }

    private void ensureCredentialsConfigured() {
        if (StringUtils.isAnyBlank(apiKey, apiKeySecret, clientId, clientSecret)) {
            throw new IllegalStateException("Missing Twitter API credentials");
        }
    }
}
