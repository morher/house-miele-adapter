package net.morher.house.miele.consumer.auth;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class OauthToken {

    private String accessToken;
    private String refreshToken;
    private Instant expires;

    public boolean isExpired(Instant atInstant) {
        return !atInstant.isBefore(expires);
    }
}
