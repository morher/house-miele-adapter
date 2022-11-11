package net.morher.house.miele.consumer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.morher.house.miele.consumer.api.MieleDeviceInfoTO;
import net.morher.house.miele.consumer.auth.MieleTokenManager;
import net.morher.house.miele.domain.MieleDeviceInfo;

@Slf4j
public class MieleConsumerImpl implements MieleConsumer {
    private static final TypeReference<Map<String, MieleDeviceInfoTO>> DEVICES_TYPE = new TypeReference<Map<String, MieleDeviceInfoTO>>() {
    };
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String baseUrl;
    private final MieleTokenManager tokenManager;

    public MieleConsumerImpl(String baseUrl, MieleTokenManager tokenManager) {
        this.baseUrl = baseUrl;
        this.tokenManager = tokenManager;
    }

    @Override
    public Map<String, MieleDeviceInfo> getDevices() {
        log.debug("Fetching all devices");
        try {
            HttpRequest request = HttpRequest
                    .newBuilder(new URI(baseUrl + "/devices"))
                    .GET()
                    .header("accept", "application/json; charset=utf-8")
                    .header("Authorization", "Bearer " + tokenManager.getAccessToken())
                    .build();

            HttpClient client = HttpClient
                    .newBuilder()
                    .build();

            HttpResponse<String> response = client
                    .send(request, BodyHandlers.ofString());

            Map<String, MieleDeviceInfoTO> responseBody = OBJECT_MAPPER.readValue(response.body(), DEVICES_TYPE);

            HashMap<String, MieleDeviceInfo> devices = new HashMap<>();
            for (Map.Entry<String, MieleDeviceInfoTO> entry : responseBody.entrySet()) {
                devices.put(entry.getKey(), entry.getValue().toMieleDevice());
            }
            return devices;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch devices", e);
        }
    }
}
