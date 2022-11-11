package net.morher.house.miele.consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import net.morher.house.miele.consumer.auth.MieleTokenManager;
import net.morher.house.miele.domain.MieleDeviceIdentity;
import net.morher.house.miele.domain.MieleDeviceInfo;
import net.morher.house.miele.domain.MieleDeviceState;
import net.morher.house.miele.domain.MieleDeviceStatus;
import net.morher.house.miele.domain.MieleDeviceType;

public class MieleConsumerImplTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule();

    @Test
    public void getDevices() throws Exception {
        byte[] responseBytes = getClass().getClassLoader().getResourceAsStream("sample-response.json").readAllBytes();

        stubFor(get("/devices")
                .willReturn(ok()
                        .withBody(responseBytes)));

        Map<String, MieleDeviceInfo> devices = consumer()
                .getDevices();

        assertThat(devices, is(not(nullValue())));
        assertThat(devices.size(), is(1));

        MieleDeviceInfo device = devices.get("000000000001");
        assertThat(device, is(not(nullValue())));
        assertThat(device.getId(), is(equalTo("000000000001")));

        MieleDeviceIdentity deviceIdent = device.getIdent();
        assertThat(deviceIdent, is(not(nullValue())));
        assertThat(deviceIdent.getDeviceType(), is(MieleDeviceType.DISHWASHER));

        MieleDeviceState deviceState = device.getState();
        assertThat(deviceState, is(not(nullValue())));
        assertThat(deviceState.getStatus(), is(MieleDeviceStatus.PROGRAMMED_WAITING_TO_START));
        assertThat(deviceState.getDelayedStart(), is(equalTo(Duration.ofHours(2).plusMinutes(12))));
        assertThat(deviceState.getRemainingTime(), is(equalTo(Duration.ofHours(3).plusMinutes(55))));
        assertThat(deviceState.getProgramPhase(), is(equalTo("")));
    }

    private MieleConsumerImpl consumer() {
        return new MieleConsumerImpl("http://localhost:" + wireMock.port(), mock(MieleTokenManager.class));
    }
}
