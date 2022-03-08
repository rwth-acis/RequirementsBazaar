package de.rwth.dbis.acis.bazaar.service.twitter;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.github.scribejava.core.model.Verb;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.others.BearerToken;
import io.github.redouane59.twitter.helpers.URLHelper;

public class TwitterClientSecretSupport {

    private TwitterClientSecretSupport() {
    }

    // TODO Create issue in TwitterClient library to support client secret out of the box!
    /**
     * Variant of {@link TwitterClient#getOAuth2AccessToken(String, String, String, String)}
     * that uses <i>clientId</i> and <i>clientSecret</i> for authorization.<br>
     * <br>
     * This is required for applications configured as <i>confidential client</i> in the Twitter Developer Console.
     *
     * @param twitterClient
     * @param clientId
     * @param clientSecret
     * @param code
     * @param codeVerifier
     * @param redirectUri
     * @return
     */
    public static BearerToken getOAuth2AccessTokenWithSecret(
            TwitterClient twitterClient,
            String clientId,
            String clientSecret,
            String code,
            String codeVerifier,
            String redirectUri) {

        String url = URLHelper.ACCESS_TOKEN_URL;
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        params.put("code_verifier", codeVerifier);
        params.put("grant_type", "authorization_code");

        String valueToEncode = clientId + ":" + clientSecret;
        String encodedValue = Base64.getEncoder().encodeToString(valueToEncode.getBytes());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encodedValue);

        return twitterClient.getRequestHelperV2()
                .makeRequest(Verb.POST, url, headers, params, null, false, BearerToken.class)
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Variant of {@link TwitterClient#getOAuth2RefreshToken(String, String)}
     * that uses <i>clientId</i> and <i>clientSecret</i> for authorization.<br>
     *
     * @param twitterClient
     * @param clientId
     * @param clientSecret
     * @param refreshToken
     * @return
     */
    public static BearerToken refreshOAuth2AccessTokenWithSecret(
            TwitterClient twitterClient,
            String clientId,
            String clientSecret,
            String refreshToken) {

        String url = URLHelper.ACCESS_TOKEN_URL;
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("refresh_token", refreshToken);
        params.put("grant_type", "refresh_token");

        String valueToEncode = clientId + ":" + clientSecret;
        String encodedValue = Base64.getEncoder().encodeToString(valueToEncode.getBytes());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encodedValue);

        return twitterClient.getRequestHelperV2()
                .makeRequest(Verb.POST, url, headers, params, null, false, BearerToken.class)
                .orElseThrow(NoSuchElementException::new);
    }
}
