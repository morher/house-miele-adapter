package net.morher.house.miele.consumer;

import java.util.Map;

import net.morher.house.miele.domain.MieleDeviceInfo;

public interface MieleConsumer {
    Map<String, MieleDeviceInfo> getDevices();
}
