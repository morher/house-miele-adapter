package net.morher.house.miele.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import net.morher.house.api.entity.Device;
import net.morher.house.api.entity.DeviceId;
import net.morher.house.api.entity.DeviceManager;
import net.morher.house.api.schedule.DelayedTrigger;
import net.morher.house.api.schedule.HouseScheduler;
import net.morher.house.miele.config.MieleAdapterConfiguration.MieleConfig;
import net.morher.house.miele.config.MieleAdapterConfiguration.MieleDeviceConfig;
import net.morher.house.miele.consumer.MieleConsumer;
import net.morher.house.miele.domain.MieleDeviceInfo;

@Slf4j
public class MieleController {
    private final HouseScheduler scheduler = HouseScheduler.get();
    private final DelayedTrigger checkStatusTrigger = scheduler.delayedTrigger("Update Miele devices", this::updateDevices);
    private final MieleConsumer consumer;
    private final DeviceManager deviceManager;
    private final Map<String, MieleDevice> devices = new HashMap<>();
    private Duration updateInterval = Duration.ofSeconds(30);

    public MieleController(MieleConsumer consumer, DeviceManager deviceManager) {
        this.consumer = consumer;
        this.deviceManager = deviceManager;

        scheduler.execute(this::listAvailableDevices);
    }

    public void configure(MieleConfig config) {
        for (Entry<String, MieleDeviceConfig> entry : config.getDevices().entrySet()) {
            configureDevice(entry.getKey(), entry.getValue());
        }
        checkStatusTrigger.runNow();
    }

    private void configureDevice(String mieleDeviceId, MieleDeviceConfig deviceConfig) {
        DeviceId deviceId = deviceConfig.getDevice().toDeviceId();
        Device device = deviceManager.device(deviceId);

        MieleDevice mieleDevice = new MieleDevice(mieleDeviceId, device);
        devices.put(mieleDeviceId, mieleDevice);
    }

    private void listAvailableDevices() {
        Map<String, MieleDeviceInfo> devices = consumer.getDevices();
        StringBuilder sb = new StringBuilder();
        sb.append("Available devices:");
        for (MieleDeviceInfo device : devices.values()) {
            sb.append("\n").append(" - ").append(device.getId()).append(": ").append(device.getIdent().getDeviceType());
        }
        log.info("{}", sb.toString());
    }

    private void updateDevices() {
        try {
            scheduleNextUpdate();
            for (MieleDeviceInfo deviceInfo : consumer.getDevices().values()) {
                String id = deviceInfo.getId();
                MieleDevice device = devices.get(id);
                if (device != null) {
                    device.updateDevice(deviceInfo);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update devices", e);
            // TODO: Longer interval on failure...
            scheduleNextUpdate();
        }
    }

    private void scheduleNextUpdate() {
        checkStatusTrigger.runAfter(updateInterval);
    }
}
