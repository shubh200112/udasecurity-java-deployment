package com.udacity.catpoint.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @BeforeEach
    void setUp() {

        SecurityRepository repository =
                new PretendDatabaseSecurityRepositoryImpl();

        FakeImageService fakeImageService =
                new FakeImageService();

        securityService = new SecurityService(
                repository,
                fakeImageService
        );
    }

    @Test
    void alarmIsArmed() {

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        assertEquals(
                ArmingStatus.ARMED_HOME,
                securityService.getArmingStatus()
        );
    }

    @Test
    void ifArmedAndSensorOpened_thenAlarmStateIsPendingAlarm() {

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
    void ifPendingAlarmAndSensorActivatedAgain_thenAlarmStateIsAlarm() {

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
    void ifAlarmActiveAndSystemDisarmed_thenAlarmStateIsNoAlarm() {

        securityService.setAlarmStatus(AlarmStatus.ALARM);

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        assertEquals(
                AlarmStatus.NO_ALARM,
                securityService.getAlarmStatus()
        );
    }

    @Test
    void ifSensorDeactivatedWhilePendingAndNoOtherSensorsActive_thenNoAlarm() {

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
void ifAlarmIsPendingAndSensorDeactivated_thenAlarmStaysPending() {

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
void ifCatDetectedWhileArmedHome_thenAlarmTriggered() {

    securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

    securityService.setAlarmStatus(AlarmStatus.NO_ALARM);

    securityService.processImage(null);

    assertNotNull(
            securityService.getAlarmStatus()
    );
}

@Test
void ifSystemDisarmed_thenAlarmIsNoAlarm() {

    securityService.setAlarmStatus(AlarmStatus.ALARM);

    securityService.setArmingStatus(ArmingStatus.DISARMED);

    assertEquals(
            AlarmStatus.NO_ALARM,
            securityService.getAlarmStatus()
    );
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
}