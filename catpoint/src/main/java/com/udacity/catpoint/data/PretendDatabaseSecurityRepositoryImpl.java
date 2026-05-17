package com.udacity.catpoint.data;

import java.util.HashSet;
import java.util.Set;

public class PretendDatabaseSecurityRepositoryImpl
        implements SecurityRepository {

    private AlarmStatus alarmStatus = AlarmStatus.NO_ALARM;

    private ArmingStatus armingStatus = ArmingStatus.DISARMED;

    private final Set<Sensor> sensors = new HashSet<>();

    private boolean catDetected = false;

    @Override
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    @Override
    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        sensors.remove(sensor);
        sensors.add(sensor);
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = armingStatus;
    }

    @Override
    public Set<Sensor> getSensors() {
        return sensors;
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }

    @Override
    public boolean getCatDetected() {
        return catDetected;
    }

    @Override
    public void setCatDetected(boolean catDetected) {
        this.catDetected = catDetected;
    }
}