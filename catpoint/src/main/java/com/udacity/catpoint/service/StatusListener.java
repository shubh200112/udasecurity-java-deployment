package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;

public interface StatusListener {

    void notify(AlarmStatus status);

    void catDetected(boolean catDetected);

    void sensorStatusChanged();
}
