package net.morher.house.miele.consumer.auth;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.morher.house.miele.config.MieleAdapterConfiguration.MieleConfig;
import net.morher.house.miele.consumer.auth.OauthCache.CacheFile;

public class AuthenticatingMieleTokenManagerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .registerModule(new JavaTimeModule());
    private static final String EMAIL = "email@example.com";
    private static final String PASSWORD = "passw0rd";
    private static final String LOCATION = "no-NO";
    private static final String CLIENT_ID = "ClientId123";
    private static final String AUTH_CODE = "AuthCode123";
    private static final Instant TOKEN_VALID_UNTIL = Instant.parse("2999-01-01T00:00:00Z");

    private AuthenticationCodeConsumer authCodeConsumer = mock(AuthenticationCodeConsumer.class);
    private OauthTokenConsumer oauthTokenConsumer = mock(OauthTokenConsumer.class);

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private String tokenCacheFilename;

    @Test
    public void testNoCacheFile() {
        String accessToken = manager()
                .getAccessToken();

        assertThat(accessToken, is(equalTo("AccessToken123")));
        verify(authCodeConsumer).getAuthCode(eq(EMAIL), eq(PASSWORD), eq(LOCATION));
        verify(oauthTokenConsumer).fetchToken(eq(AUTH_CODE));
        verifyNoMoreInteractions(authCodeConsumer, oauthTokenConsumer);
    }

    @Test
    public void testNoCachedToken() {
        createTokenCacheFile(null);

        String accessToken = manager()
                .getAccessToken();

        assertThat(accessToken, is(equalTo("AccessToken123")));
        verify(authCodeConsumer).getAuthCode(eq(EMAIL), eq(PASSWORD), eq(LOCATION));
        verify(oauthTokenConsumer).fetchToken(eq(AUTH_CODE));
        verifyNoMoreInteractions(authCodeConsumer, oauthTokenConsumer);
    }

    @Test
    public void testCachedToken() {
        createTokenCacheFile(tokenValidFor(1, DAYS));

        String accessToken = manager()
                .getAccessToken();

        assertThat(accessToken, is(equalTo("CachedAccessToken123")));
        verifyNoMoreInteractions(authCodeConsumer, oauthTokenConsumer);
    }

    @Test
    public void testCachedTokenAboutToExpire() {
        OauthToken oldToken = tokenValidFor(5, MINUTES);
        createTokenCacheFile(oldToken);

        String accessToken = manager()
                .getAccessToken();

        assertThat(accessToken, is(equalTo("RefreshedAccessToken123")));

        verify(oauthTokenConsumer).refreshToken(eq(oldToken));
        verifyNoMoreInteractions(authCodeConsumer, oauthTokenConsumer);
    }

    @Before
    public void setupMocks() {
        doReturn(AUTH_CODE)
                .when(authCodeConsumer)
                .getAuthCode(eq(EMAIL), eq(PASSWORD), eq(LOCATION));

        doReturn(new OauthToken("AccessToken123", "RefreshToken123", TOKEN_VALID_UNTIL))
                .when(oauthTokenConsumer)
                .fetchToken(eq(AUTH_CODE));

        doReturn(new OauthToken("RefreshedAccessToken123", "RefreshedRefreshToken123", TOKEN_VALID_UNTIL))
                .when(oauthTokenConsumer)
                .refreshToken(Mockito.any(OauthToken.class));
    }

    @Test
    public void testStoreCachedToken() throws Exception {
        createTokenCacheFile(null);

        String accessToken = manager()
                .getAccessToken();

        assertThat(accessToken, is(equalTo("AccessToken123")));

        CacheFile cache = OBJECT_MAPPER.readValue(new File(tokenCacheFilename), CacheFile.class);

        assertThat(cache, is(not(nullValue())));
        assertThat(cache.getTokenCache(), is(not(nullValue())));
        assertThat(cache.getTokenCache().get(EMAIL), is(equalTo(new OauthToken("AccessToken123", "RefreshToken123", TOKEN_VALID_UNTIL))));

        verify(authCodeConsumer).getAuthCode(eq(EMAIL), eq(PASSWORD), eq(LOCATION));
        verify(oauthTokenConsumer).fetchToken(eq(AUTH_CODE));
        verifyNoMoreInteractions(authCodeConsumer, oauthTokenConsumer);
    }

    private OauthToken tokenValidFor(long validDuration, TemporalUnit unit) {
        return new OauthToken("CachedAccessToken123", "CachedRefreshToken123", Instant.now().plus(validDuration, unit));
    }

    private void createTokenCacheFile(OauthToken token) {
        try {
            File tokenCacheFile = tmp.newFile();
            tokenCacheFilename = tokenCacheFile.getPath();
            CacheFile cache = new CacheFile();
            if (token != null) {
                cache.getTokenCache()
                        .put(EMAIL, token);
            }
            OBJECT_MAPPER.writeValue(tokenCacheFile, cache);

        } catch (IOException e) {
            throw new AssertionError("Failed to create token cace file", e);
        }
    }

    private AuthenticatingMieleTokenManager manager() {
        MieleConfig config = new MieleConfig();
        config.setEmail(EMAIL);
        config.setPassword(PASSWORD);
        config.setLocation(LOCATION);
        config.setClientId(CLIENT_ID);

        if (tokenCacheFilename == null) {
            tokenCacheFilename = tmp.getRoot().getPath() + "/token-cache.yaml";
        }
        config.setTokenCacheFile(tokenCacheFilename);

        return new AuthenticatingMieleTokenManager(config, authCodeConsumer, oauthTokenConsumer);
    }
}
