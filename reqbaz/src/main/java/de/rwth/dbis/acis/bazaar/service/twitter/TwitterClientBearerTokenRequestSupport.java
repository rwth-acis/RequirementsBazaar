package de.rwth.dbis.acis.bazaar.service.twitter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import i5.las2peer.logging.L2pLogger;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.TweetParameters;
import io.github.redouane59.twitter.dto.tweet.TweetV2;
import okhttp3.*;

public class TwitterClientBearerTokenRequestSupport {

    private static final L2pLogger logger = L2pLogger.getInstance(TwitterClientBearerTokenRequestSupport.class.getName());

    private static OkHttpClient httpClient = new OkHttpClient();

    TwitterClientBearerTokenRequestSupport() {
    }

    /**
     * Posts a tweet for the user who is authenticated by the credentials configured for the given {@link TwitterClient}.
     *
     * @param twitterClient
     * @param tweetParameters
     * @return
     */
    public static TweetV2 postTweet(TwitterClient twitterClient, TweetParameters tweetParameters) {
        String url = twitterClient.getUrlHelper().getPostTweetUrl();
        return doAccessTokenAuthenticatedRequest(twitterClient, url, tweetParameters, TweetV2.class);
    }

    /**
     * Executes an authenticated request to the given URL with the given request data.<br>
     * <br>
     * The request is authenticated using the <i>access token</i> set for the {@link TwitterClient}.
     *
     * <i>NOTE</i>: This method is a workaround for doing user-specific v2 requests to the Twitter API
     * which is currently not supported by the {@link TwitterClient} library.
     *
     * @param twitterClient
     * @param url
     * @param requestData
     * @param responseType
     * @param <T>
     * @return
     */
    public static <T> T doAccessTokenAuthenticatedRequest(TwitterClient twitterClient, String url, Object requestData, Class<T> responseType) {
        String requestBodyJson;
        try {
            requestBodyJson = twitterClient.OBJECT_MAPPER.writeValueAsString(requestData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write request data as JSON", e);
        }

        String accessToken = twitterClient.getTwitterCredentials().getAccessToken();

        /*
         * We do the request by directly using OkHttpClient instead of TwitterClient because
         * support for access token based v2 APIs is not implemented properly yet!
         */
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyJson))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.severe("Twitter API request failed: " + response);
                throw new RuntimeException("Unexpected API response code: " + response.code());
            }

            return twitterClient.OBJECT_MAPPER.readValue(response.body().byteStream(), responseType);
        } catch (IOException e) {
            throw new RuntimeException("Exception during Twitter API request", e);
        }
    }
}
