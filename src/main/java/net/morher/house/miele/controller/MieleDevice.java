package net.morher.house.miele.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.morher.house.api.devicetypes.GeneralDevice;
import net.morher.house.api.devicetypes.HomeApplianceDevice;
import net.morher.house.api.entity.Device;
import net.morher.house.api.entity.DeviceInfo;
import net.morher.house.api.entity.sensor.SensorEntity;
import net.morher.house.api.entity.sensor.SensorOptions;
import net.morher.house.api.entity.sensor.SensorType;
import net.morher.house.miele.domain.MieleDeviceInfo;
import net.morher.house.miele.domain.MieleDeviceStatus;

public class MieleDevice {
    private final SensorEntity<String> statusEntity;
    private final SensorEntity<String> phaseEntity;
    private final SensorEntity<Integer> remainingTimeEntity;
    private final SensorEntity<String> estimatedCompleteTimeEntity;
    private final DateTimeFormatter estimatedCompleteTimeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private LocalDateTime estimatedCompleteTime;

    public MieleDevice(String id, Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setManufacturer("Miele");

        statusEntity = device.entity(GeneralDevice.STATUS);
        statusEntity.setDeviceInfo(deviceInfo);
        statusEntity.setOptions(new SensorOptions(SensorType.NONE));

        phaseEntity = device.entity(HomeApplianceDevice.PROGRAM_PHASE);
        phaseEntity.setDeviceInfo(deviceInfo);
        phaseEntity.setOptions(new SensorOptions(SensorType.NONE));

        remainingTimeEntity = device.entity(HomeApplianceDevice.REMAINING_TIME);
        remainingTimeEntity.setDeviceInfo(deviceInfo);
        remainingTimeEntity.setOptions(new SensorOptions(SensorType.DURATION_M));

        estimatedCompleteTimeEntity = device.entity(HomeApplianceDevice.ESTIMATED_COMPLETE_TIME);
        estimatedCompleteTimeEntity.setDeviceInfo(deviceInfo);
        estimatedCompleteTimeEntity.setOptions(new SensorOptions(SensorType.NONE));
    }

    public void updateDevice(MieleDeviceInfo info) {
        statusEntity.state().publish(info.getState().getStatus().getName());
        phaseEntity.state().publish(programPhase(info));
        remainingTimeEntity.state().publish(remainingTime(info));
        estimatedCompleteTimeEntity.state().publish(estimatedCompleteTime(info));
    }

    private static String programPhase(MieleDeviceInfo info) {
        if (isDelayedStart(info.getState().getStatus())) {
            return "Delayed start";
        }

        String phase = info.getState().getProgramPhase();
        return phase != null && !phase.isBlank()
                ? phase
                : " ";
    }

    private String estimatedCompleteTime(MieleDeviceInfo info) {
        reestimateCompleteTime(info);
        return estimatedCompleteTime != null
                ? estimatedCompleteTimeFormat.format(estimatedCompleteTime)
                : " ";
    }

    private static int remainingTime(MieleDeviceInfo info) {
        Duration remainingTime = info.getState().getRemainingTime();
        if (remainingTime == null
                || (!inProgress(info.getState().getStatus())
                        && !isDelayedStart(info.getState().getStatus()))) {
            return 0;
        }
        return (int) remainingTime.toMinutes();
    }

    private void reestimateCompleteTime(MieleDeviceInfo info) {
        MieleDeviceStatus status = info.getState().getStatus();
        Duration remainingTime = info.getState().getRemainingTime();
        if (remainingTime == null) {
            estimatedCompleteTime = null;
            return;
        }

        Duration delayedStart = info.getState().getDelayedStart();
        if (isDelayedStart(status) && delayedStart != null && !delayedStart.isZero()) {
            updateCompleteTimeEstimate(LocalDateTime.now().plus(delayedStart).plus(remainingTime));
            return;
        }

        if (!inProgress(status) || remainingTime == null) {
            estimatedCompleteTime = null;
            return;
        }

        updateCompleteTimeEstimate(LocalDateTime.now().plus(remainingTime));
    }

    private void updateCompleteTimeEstimate(LocalDateTime newEstimate) {
        LocalDateTime lastEstimate = estimatedCompleteTime;
        if (lastEstimate == null
                || newEstimate.isAfter(lastEstimate)
                || newEstimate.isBefore(lastEstimate.minusMinutes(3))) {
            estimatedCompleteTime = newEstimate;
        }
    }

    private static boolean inProgress(MieleDeviceStatus status) {
        return MieleDeviceStatus.RUNNING.equals(status)
                || MieleDeviceStatus.PAUSE.equals(status);
    }

    private static boolean isDelayedStart(MieleDeviceStatus status) {
        return MieleDeviceStatus.PROGRAMMED_WAITING_TO_START.equals(status);
    }
}
