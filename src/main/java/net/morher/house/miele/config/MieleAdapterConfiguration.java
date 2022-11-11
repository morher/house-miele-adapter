package net.morher.house.miele.config;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import net.morher.house.api.config.DeviceName;

@Data
public class MieleAdapterConfiguration {
    private MieleConfig miele;

    @Data
    public static class MieleConfig {
        private String clientId;
        private String clientSecret;
        private String email;
        private String password;
        private String location;

        private String apiUrl = "https://api.mcs3.miele.com/v1";
        private String authCodeUrl = "https://api.mcs3.miele.com/oauth/auth";
        private String tokenUrl = "https://api.mcs3.miele.com/thirdparty/token";
        private String tokenCacheFile = "token-cache.yaml";

        private final Map<String, MieleDeviceConfig> devices = new HashMap<>();
    }

    @Data
    public static class MieleDeviceConfig {
        private DeviceName device;
    }
}
