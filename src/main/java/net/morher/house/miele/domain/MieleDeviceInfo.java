package net.morher.house.miele.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MieleDeviceInfo {
    private final String id;
    private final MieleDeviceIdentity ident;
    private final MieleDeviceState state;
}
