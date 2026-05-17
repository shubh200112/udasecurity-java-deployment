package com.udacity.catpoint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.imageservice.FakeImageService;

public class SecurityServiceTest {

    private SecurityService securityService;
    private SecurityRepository securityRepository;
    private FakeImageService imageService;

    @BeforeEach
    void setUp() {

        securityRepository =
                new PretendDatabaseSecurityRepositoryImpl();

        imageService =
                new FakeImageService();

        securityService =
                new SecurityService(
                        securityRepository,
                        imageService
                );
    }

    @Test
    void ifAlarmIsArmedHomeAndSensorActivated_thenStateIsPendingAlarm() {

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        assertEquals(
                AlarmStatus.PENDING_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifAlarmIsArmedAwayAndSensorActivated_thenStateIsPendingAlarm() {

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        assertEquals(
                AlarmStatus.PENDING_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifAlarmIsPendingAndSensorActivatedAgain_thenStateIsAlarm() {

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        securityService.changeSensorActivationStatus(sensor, true);

        assertEquals(
                AlarmStatus.ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifPendingAlarmAndAllSensorsInactive_thenReturnToNoAlarmState() {

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        securityService.changeSensorActivationStatus(sensor, false);

        assertEquals(
                AlarmStatus.NO_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifAlarmIsActive_thenChangingSensorStateDoesNotChangeAlarmState() {

        securityService.setAlarmStatus(AlarmStatus.ALARM);

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        assertEquals(
                AlarmStatus.ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifCatDetectedWhileArmedHome_thenAlarmSetToAlarm() {

        securityRepository.setCatDetected(true);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        securityService.processImage(null);

        assertEquals(
                AlarmStatus.ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifCatNotDetectedAndSensorsInactive_thenAlarmSetToNoAlarm() {

        securityRepository.setCatDetected(false);

        securityService.processImage(null);

        assertEquals(
                AlarmStatus.NO_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifSystemIsDisarmed_thenAlarmStateIsNoAlarm() {

        securityService.setAlarmStatus(AlarmStatus.ALARM);

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        assertEquals(
                AlarmStatus.NO_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifSystemIsArmed_thenSensorsBecomeInactive() {

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        sensor.setActive(true);

        securityRepository.addSensor(sensor);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        assertEquals(false, sensor.getActive());
    }

    @Test
    void ifSensorActivatedWhileDisarmed_thenAlarmRemainsNoAlarm() {

        Sensor sensor =
                new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        assertEquals(
                AlarmStatus.NO_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void addSensor_addsSensorToRepository() {

        Sensor sensor =
                new Sensor("Door", SensorType.DOOR);

        securityService.addSensor(sensor);

        assertEquals(
                1,
                securityRepository.getSensors().size()
        );
    }

    @Test
    void removeSensor_removesSensorFromRepository() {

        Sensor sensor =
                new Sensor("Door", SensorType.DOOR);

        securityRepository.addSensor(sensor);

        securityService.removeSensor(sensor);

        assertEquals(
                0,
                securityRepository.getSensors().size()
        );
    }
}