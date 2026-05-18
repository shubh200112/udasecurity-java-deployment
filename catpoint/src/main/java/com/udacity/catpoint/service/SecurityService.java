package com.udacity.catpoint.service;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.imageservice.FakeImageService;

public class SecurityService {

    private final FakeImageService imageService;
    private final SecurityRepository securityRepository;

    private final Set<StatusListener> statusListeners = new HashSet<>();

    private AlarmStatus alarmStatus = AlarmStatus.NO_ALARM;
    private ArmingStatus armingStatus = ArmingStatus.DISARMED;

    public SecurityService(SecurityRepository securityRepository,
                           FakeImageService imageService) {
        this.imageService = imageService;
        this.securityRepository = securityRepository;
    }

    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = armingStatus;
        securityRepository.setArmingStatus(armingStatus); // FIX 1: sync to repository

        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        if (armingStatus == ArmingStatus.ARMED_HOME ||
                armingStatus == ArmingStatus.ARMED_AWAY) {

            securityRepository.getSensors()
                    .forEach(sensor -> changeSensorActivationStatus(sensor, false));

            if (securityRepository.getCatDetected()) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {

        if (sensor.getActive() == active) {
            return;
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if (active) {
            if (armingStatus == ArmingStatus.DISARMED) {
                return; // FIX: don't trigger alarm when disarmed
            }
            if (alarmStatus == AlarmStatus.NO_ALARM) {
                setAlarmStatus(AlarmStatus.PENDING_ALARM);
            } else if (alarmStatus == AlarmStatus.PENDING_ALARM) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
            // if alarmStatus == ALARM, do nothing

        } else {
            // FIX 3: only reset to NO_ALARM if status is PENDING, not ALARM
            if (allSensorsInactive() && alarmStatus == AlarmStatus.PENDING_ALARM) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
    }

    private boolean allSensorsInactive() {
        return securityRepository.getSensors()
                .stream()
                .noneMatch(Sensor::getActive);
    }

    public void processImage(BufferedImage image) {
        boolean catDetected = imageService.imageContainsCat(image, 50.0f);

        securityRepository.setCatDetected(catDetected);
        notifyCatDetected(catDetected); // FIX 4: actually call notifyCatDetected

        // FIX 2: use this.armingStatus (local field), not securityRepository.getArmingStatus()
        if (catDetected && armingStatus == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM); // FIX 2: use this.setAlarmStatus()

        } else if (!catDetected) {
            if (allSensorsInactive()) {
                setAlarmStatus(AlarmStatus.NO_ALARM); // FIX 2: use this.setAlarmStatus()
            }
        }
    }

    public void setAlarmStatus(AlarmStatus status) {
        this.alarmStatus = status; // keep local field in sync
        securityRepository.setAlarmStatus(status);
        notifyListeners(status);
    }

    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    private void notifyListeners(AlarmStatus status) {
        statusListeners.forEach(listener -> listener.notify(status));
    }

    private void notifyCatDetected(boolean catDetected) {
        statusListeners.forEach(listener -> listener.catDetected(catDetected));
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }
}