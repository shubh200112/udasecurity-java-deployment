package com.udacity.catpoint.service;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.imageservice.FakeImageService;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private FakeImageService imageService;

    @Mock
    private StatusListener statusListener;

    @InjectMocks
    private SecurityService securityService;

    private Set<Sensor> sensors;

    @BeforeEach
    void setUp() {
        sensors = new HashSet<>();
    }

    // Requirement 1 & 2 — armed + sensor activated → PENDING_ALARM
        @ParameterizedTest
        @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
        void ifAlarmIsArmed_andSensorActivated_thenStateIsPendingAlarm(ArmingStatus armingStatus) {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(armingStatus);
        securityService.changeSensorActivationStatus(sensor, true);

        assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
        }

    // Requirement 3 — PENDING + sensor activated again → ALARM
    @Test
    void ifAlarmIsPendingAndSensorActivatedAgain_thenStateIsAlarm() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // Requirement 4 (activation case) — ALARM active, activating sensor → no change
    @Test
    void ifAlarmIsActive_thenActivatingSensorDoesNotChangeAlarmState() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.setAlarmStatus(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // Requirement 4 (deactivation case) — ALARM active, deactivating sensor → alarm stays ALARM
    @Test
    void ifAlarmIsActive_thenDeactivatingSensorDoesNotClearAlarm() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensor.setActive(true);
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setAlarmStatus(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    // Requirement 5 — PENDING + all sensors inactive → NO_ALARM
    @Test
    void ifPendingAlarmAndAllSensorsInactive_thenReturnToNoAlarmState() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensor.setActive(true);
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 6 — disarmed → NO_ALARM
    @Test
    void ifSystemIsDisarmed_thenAlarmStateIsNoAlarm() {
        securityService.setAlarmStatus(AlarmStatus.ALARM);
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Requirement 7 — armed → all sensors deactivated
    @Test
    void ifSystemIsArmed_thenSensorsBecomeInactive() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);
        sensor.setActive(true);
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        assertEquals(false, sensor.getActive());
    }

    // Requirement 8 — deactivating already-inactive sensor → no alarm state change
    @Test
    void ifSensorDeactivatedWhileAlreadyInactive_thenNoAlarmStateChange() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    // Requirement 9 — cat detected via processImage while ARMED_HOME → ALARM
    @Test
    void ifCatDetectedAndArmedHome_processImageSetsAlarm() {
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    @Test
        void ifCatDetectedAndSystemArmed_thenAlarmSetToAlarm() {
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getCatDetected()).thenReturn(true);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        }

    // Requirement 10 — no cat detected + sensors inactive → NO_ALARM
    @Test
    void ifCatNotDetectedAndSensorsInactive_thenAlarmSetToNoAlarm() {
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

        securityService.processImage(null);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // Sensor activated while disarmed → no alarm
    @Test
    void ifSensorActivatedWhileDisarmed_thenAlarmRemainsNoAlarm() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // Listener notified when alarm status changes
    @Test
    void ifAlarmStatusChanges_thenListenersNotified() {
        securityService.addStatusListener(statusListener);
        securityService.setAlarmStatus(AlarmStatus.ALARM);

        verify(statusListener).notify(AlarmStatus.ALARM);
    }

    // Listener notified when cat detected
    @Test
    void ifCatDetected_thenListenerNotifiedOfCatDetection() {
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.addStatusListener(statusListener);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));

        verify(statusListener).catDetected(true);
    }

    @Test
    void addSensor_addsSensorToRepository() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        securityService.addSensor(sensor);
        verify(securityRepository).addSensor(sensor);
    }

    @Test
    void removeSensor_removesSensorFromRepository() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        securityService.removeSensor(sensor);
        verify(securityRepository).removeSensor(sensor);
    }
}