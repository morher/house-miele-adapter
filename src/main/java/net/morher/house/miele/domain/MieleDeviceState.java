package net.morher.house.miele.domain;

import java.time.Duration;

import lombok.Data;

@Data
public class MieleDeviceState {
    private final MieleDeviceStatus status;
    private final String programPhase;
    private final Duration remainingTime;
    private final Duration delayedStart;
}
