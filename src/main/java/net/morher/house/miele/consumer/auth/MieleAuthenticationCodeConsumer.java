package net.morher.house.miele.consumer.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MieleAuthenticationCodeConsumer implements AuthenticationCodeConsumer {
    private static final Pattern CODE_PATTERN = Pattern.compile("code=([^&]*)");
    private final String endpointUrl;
    private final String clientId;

    public MieleAuthenticationCodeConsumer(String endpointUrl, String clientId) {
        this.endpointUrl = endpointUrl;
        this.clientId = clientId;
    }

    @Override
    public String getAuthCode(String username, String password, String location) {
        try {
            log.info("Fetching auth code for {} at {}", username, location);
            String body = "client_id=" + this.clientId
                    + "&response_type=code"
                    + "&redirect_uri=http://localhost/"
                    + "&email=" + username
                    + "&password=" + password
                    + "&vgInformationSelector=" + location;

            HttpRequest request = HttpRequest
                    .newBuilder(new URI(endpointUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient
                    .newBuilder()
                    .followRedirects(Redirect.NEVER)
                    .build();

            HttpResponse<Void> response = client
                    .send(request, BodyHandlers.discarding());

            if (response.statusCode() == 200) {
                // TODO: Implement confirmation for first assiciating of user and clientId.
                throw new IllegalStateException("Miele returned 200. User must confirm integration? (Not implemented)");
            }
            if (response.statusCode() != 302) {
                throw new IllegalStateException("Miele returned " + response.statusCode() + ". Should have been 302.");
            }

            String redirectUrl = response
                    .headers()
                    .firstValue("Location")
                    .get();

            Matcher matcher = CODE_PATTERN.matcher(redirectUrl);

            if (!matcher.find()) {
                log.warn("Failed to fetch auth code: No code in return url");
                throw new RuntimeException("Failed to fetch auth code: No code in return url");
            }
            return matcher.group(1);

        } catch (Exception e) {
            log.warn("Failed to fetch auth code: {}", e.toString());
            throw new RuntimeException("Failed to fetch auth code", e);
        }
    }
}
