package net.morher.house.miele.domain;

public enum MieleDeviceStatus {
    OFF(1, "Off"),
    ON(2, "On"),
    PROGRAMMED(3, "Programmed"),
    PROGRAMMED_WAITING_TO_START(4, "Programmed, waiting to start"),
    RUNNING(5, "Running"),
    PAUSE(6, "Pause"),
    END_PROGRAMMED(7, "End programmed"),
    FAILURE(8, "FAILURE"),
    PROGRAMME_INTERRUPTED(9, "Programme interrupted"),
    IDLE(10, "Idle"),
    RINSE_HOLD(11, "Rinse hold"),
    SERVICE(12, "Service"),
    SUPERFREEZING(13, "Superfreesing"),
    SUPERCOOLING(14, "Supercooling"),
    SUPERHEATING(15, "Superheating"),
    SUPERCOOLING_SUPERFREEZING(146, "Superfreeszing and Supercooling"),
    NOT_CONNECTED(255, "Not connected"),
    UNKNOWN(-1, "Uknown");

    private final int code;
    private final String name;

    private MieleDeviceStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MieleDeviceStatus findStatus(Integer statusCode) {
        if (statusCode != null) {
            for (MieleDeviceStatus status : MieleDeviceStatus.values()) {
                if (status.getCode() == statusCode) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }
}
