package net.morher.house.miele.consumer.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OauthTokenConsumerImpl implements OauthTokenConsumer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String endpointUrl;
    private final String clientId;
    private final String clientSecret;

    public OauthTokenConsumerImpl(String endpointUrl, String clientId, String clientSecret) {
        this.endpointUrl = endpointUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public OauthToken fetchToken(String code) {
        log.info("Fetching token");
        try {
            Instant now = Instant.now();

            String body = "client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&grant_type=authorization_code"
                    + "&code=" + code
                    + "&redirect_uri=http://localhost/"
                    + "&state=token";

            HttpRequest request = HttpRequest
                    .newBuilder(new URI(endpointUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient
                    .newBuilder()
                    .build();

            HttpResponse<String> response = client
                    .send(request, BodyHandlers.ofString());

            return OBJECT_MAPPER
                    .readValue(response.body(), TokenTO.class)
                    .toOauthToken(now);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch token", e);
        }
    }

    @Override
    public OauthToken refreshToken(OauthToken oldToken) {
        log.info("Refreshing token");
        try {
            Instant now = Instant.now();

            String body = "client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&grant_type=refresh_token"
                    + "&refresh_token=" + oldToken.getRefreshToken();

            HttpRequest request = HttpRequest
                    .newBuilder(new URI(endpointUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient
                    .newBuilder()
                    .build();

            HttpResponse<String> response = client
                    .send(request, BodyHandlers.ofString());

            return OBJECT_MAPPER
                    .readValue(response.body(), TokenTO.class)
                    .toOauthToken(now);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch token", e);
        }
    }

    @Data
    private static class TokenTO {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private long expiresInSeconds;

        public OauthToken toOauthToken(Instant fetchedTime) {
            return new OauthToken(accessToken, refreshToken, fetchedTime.plusSeconds(expiresInSeconds));
        }
    }
}
