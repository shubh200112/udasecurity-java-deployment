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

    private final SecurityRepository securityRepository;
    private final FakeImageService imageService;

    private final Set<StatusListener> statusListeners = new HashSet<>();

    private ArmingStatus armingStatus = ArmingStatus.DISARMED;
    private AlarmStatus alarmStatus = AlarmStatus.NO_ALARM;

    public SecurityService(SecurityRepository securityRepository,
                           FakeImageService imageService) {

        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    public void setArmingStatus(ArmingStatus armingStatus) {

        this.armingStatus = armingStatus;

        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        if (armingStatus == ArmingStatus.ARMED_HOME ||
                armingStatus == ArmingStatus.ARMED_AWAY) {

            securityRepository.getSensors()
                    .forEach(sensor -> sensor.setActive(false));
        }

        notifySensorStatusChanged();
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {

    if (active) {

        if (alarmStatus == AlarmStatus.PENDING_ALARM) {

            setAlarmStatus(AlarmStatus.ALARM);

        } else if (!sensor.getActive()) {

            sensor.setActive(true);

            if (alarmStatus == AlarmStatus.NO_ALARM
                    && armingStatus != ArmingStatus.DISARMED) {

                setAlarmStatus(AlarmStatus.PENDING_ALARM);
            }
        }

    } else {

        sensor.setActive(false);

        if (allSensorsInactive()) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    notifySensorStatusChanged();
}

    private boolean allSensorsInactive() {

        return securityRepository.getSensors()
                .stream()
                .noneMatch(Sensor::getActive);
    }

    public void processImage(BufferedImage image) {

        boolean catDetected =
                imageService.imageContainsCat(image, 50.0f);

        if (catDetected &&
                armingStatus == ArmingStatus.ARMED_HOME) {

            setAlarmStatus(AlarmStatus.ALARM);
        }

        notifyCatDetected(catDetected);
    }

    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
        notifyAlarmStatus();
    }

    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
        notifySensorStatusChanged();
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
        notifySensorStatusChanged();
    }

    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    private void notifyAlarmStatus() {

        statusListeners.forEach(
                listener -> listener.notify(alarmStatus)
        );
    }

    private void notifyCatDetected(boolean catDetected) {

        statusListeners.forEach(
                listener -> listener.catDetected(catDetected)
        );
    }

    private void notifySensorStatusChanged() {

        statusListeners.forEach(
                StatusListener::sensorStatusChanged
        );
    }
}