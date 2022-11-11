package net.morher.house.miele.consumer.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class MieleAuthenticationCodeConsumerTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule(0);

    @Test(expected = RuntimeException.class)
    public void testGetAuthCodeReturned200() {
        stubFor(post("/oauth/auth")
                .willReturn(ok()
                        .withBody("<h1>Connect account</h1>")));

        getAuthCode();
    }

    @Test(expected = RuntimeException.class)
    public void testGetAuthCodeReturned404() {
        stubFor(post("/oauth/auth")
                .willReturn(notFound()
                        .withBody("<h1>404 Not Found</h1>")));

        getAuthCode();
    }

    public void testGetAuthCode() {
        stubFor(post("/oauth/auth")
                .willReturn(temporaryRedirect("http://localhost/?code=Code123&someOtherParam=ok")
                        .withBody("<h1>404 Not Found</h1>")));

        String authCode = getAuthCode();

        assertThat(authCode, is(equalTo("Code123")));
    }

    private String getAuthCode() {
        return consumer()
                .getAuthCode("test", "passw0rd", "no-NO");
    }

    private AuthenticationCodeConsumer consumer() {
        int port = wireMock.port();
        return new MieleAuthenticationCodeConsumer("http://localhost:" + port + "/oauth/auth", "ClientId123");
    }
}
