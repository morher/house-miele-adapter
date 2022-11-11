package net.morher.house.miele.consumer.api;

import java.time.Duration;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import net.morher.house.miele.domain.MieleDeviceIdentity;
import net.morher.house.miele.domain.MieleDeviceInfo;
import net.morher.house.miele.domain.MieleDeviceState;
import net.morher.house.miele.domain.MieleDeviceStatus;
import net.morher.house.miele.domain.MieleDeviceType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MieleDeviceInfoTO {
    private IdentTO ident = new IdentTO();
    private StateTO state = new StateTO();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdentTO {
        @JsonProperty("deviceIdentLabel")
        private IdentLabelTO label = new IdentLabelTO();
        private LocalizedValueTO type = new LocalizedValueTO();

        public MieleDeviceIdentity toMieleDeviceIdent() {
            return new MieleDeviceIdentity(deviceType(type.getValueRaw()));
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdentLabelTO {
        private String fabNumber;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StateTO {
        @JsonProperty("ProgramID")
        private LocalizedValueTO programId;
        private LocalizedValueTO status;
        private LocalizedValueTO programType;
        private LocalizedValueTO programPhase;
        private int[] remainingTime;
        private int[] startTime;

        public MieleDeviceState toMieleDeviceState() {
            return new MieleDeviceState(
                    MieleDeviceStatus.findStatus(rawValue(status)),
                    localizedValue(programPhase),
                    toDuration(remainingTime),
                    toDuration(startTime));
        }

        private static LocalTime toStartTime(int[] startTime) {
            if (startTime == null || startTime.length != 2) {
                return null;
            }
            return LocalTime.of(startTime[0], startTime[1]);
        }

    }

    @Data
    public static class LocalizedValueTO {
        @JsonProperty("key_localized")
        private String keyLocalized;

        @JsonProperty("value_raw")
        private Integer valueRaw;

        @JsonProperty("value_localized")
        private String valueLocalized;
    }

    private static Duration toDuration(int[] timeArr) {
        if (timeArr == null || timeArr.length != 2) {
            return null;
        }
        return Duration
                .ofHours(timeArr[0])
                .plusMinutes(timeArr[1]);
    }

    private static Integer rawValue(LocalizedValueTO value) {
        return value != null
                ? value.getValueRaw()
                : null;
    }

    private static String localizedValue(LocalizedValueTO value) {
        return value != null
                ? value.getValueLocalized()
                : null;
    }

    public MieleDeviceInfo toMieleDevice() {
        return new MieleDeviceInfo(
                ident.getLabel().getFabNumber(),
                ident.toMieleDeviceIdent(),
                state.toMieleDeviceState());
    }

    private static MieleDeviceType deviceType(Integer valueRaw) {
        switch (valueRaw) {
        case 1:
            return MieleDeviceType.WASHING_MACHINE;
        case 2:
            return MieleDeviceType.TUMBLE_DRYER;
        case 7:
            return MieleDeviceType.DISHWASHER;
        case 12:
            return MieleDeviceType.OVEN;
        case 13:
            return MieleDeviceType.OVEN_MICROWAVE;
        case 14:
            return MieleDeviceType.HOB_HIGHLIGHT;
        case 15:
            return MieleDeviceType.STEAM_OVEN;
        case 16:
            return MieleDeviceType.MICROWAVE;
        case 17:
            return MieleDeviceType.COFFEE_SYSTEM;
        case 18:
            return MieleDeviceType.HOOD;
        case 19:
            return MieleDeviceType.FRIDGE;
        case 20:
            return MieleDeviceType.FREEZER;
        case 21:
            return MieleDeviceType.FRIDGE_FREEZER_COMBINATION;
        case 23:
            return MieleDeviceType.VACUUM_CLEANER;
        case 24:
            return MieleDeviceType.WASHER_DRYER;
        case 25:
            return MieleDeviceType.DISH_WARMER;
        case 27:
            return MieleDeviceType.HOB_INDUCTION;
        case 31:
            return MieleDeviceType.STEAM_OVEN_COMBINATION;
        case 32:
            return MieleDeviceType.WINE_CABINET;
        case 33:
            return MieleDeviceType.WINE_CONDITIONING_UNIT;
        case 34:
            return MieleDeviceType.WINE_STORAGE_CONDITIONING_UNIT;
        case 45:
            return MieleDeviceType.STEAM_OVEN_MICROWAVE_COMBINATION;
        case 48:
            return MieleDeviceType.VACUUM_DRAWER;
        case 67:
            return MieleDeviceType.DIALOGOVEN;
        case 68:
            return MieleDeviceType.WINE_CABINET_FREEZER_COMBINATION;
        }
        return MieleDeviceType.UNKNOWN;
    }
}
