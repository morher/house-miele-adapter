package net.morher.house.miele.consumer.auth;

import java.time.Duration;
import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import net.morher.house.miele.config.MieleAdapterConfiguration.MieleConfig;

@Slf4j
public class AuthenticatingMieleTokenManager implements MieleTokenManager {
    private final AuthenticationCodeConsumer codeConsumer;
    private final OauthTokenConsumer tokenConsumer;
    private final OauthCache tokenCache;
    private final Duration refreshDuration;
    private final String username;
    private final String password;
    private final String location;

    public AuthenticatingMieleTokenManager(
            MieleConfig config,
            AuthenticationCodeConsumer codeConsumer,
            OauthTokenConsumer tokenConsumer) {
        this.codeConsumer = codeConsumer;
        this.tokenConsumer = tokenConsumer;

        tokenCache = new OauthCache(config.getTokenCacheFile());
        refreshDuration = Duration.ofMinutes(10l);
        this.username = config.getEmail();
        this.password = config.getPassword();
        this.location = config.getLocation();
    }

    @Override
    public String getAccessToken() {
        log.debug("Get token for {}.", username);
        OauthToken token = tokenCache.getToken(username);
        Instant now = Instant.now();
        if (token == null) {
            log.debug("No cached token, request new.");
            token = fetchToken();

        } else if (!token.getExpires().isAfter(now)) {
            log.debug("Cached token has expired, request new.");
            token = fetchToken();

        } else if (!token.getExpires().minus(refreshDuration).isAfter(now)) {
            log.debug("Token is about to expire. Refresh token.");
            token = refreshToken(token);

        }
        log.debug("Token for {} valid until {}.", username, token.getExpires());
        return token.getAccessToken();
    }

    private OauthToken fetchToken() {
        String code = codeConsumer.getAuthCode(username, password, location);
        log.trace("Auth code: {}", code);
        OauthToken token = tokenConsumer.fetchToken(code);
        tokenCache.storeToken(username, token);
        return token;
    }

    private OauthToken refreshToken(OauthToken token) {
        log.trace("Refresh token: {}", token.getRefreshToken());
        return tokenConsumer.refreshToken(token);
    }
}
