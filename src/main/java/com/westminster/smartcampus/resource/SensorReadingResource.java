package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.store.DataStore;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Sub-resource handling /sensors/{sensorId}/readings.
 *
 * Actual HTTP methods will be added in Day 14.
 */
@Path("/readings")
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(@PathParam("sensorId") String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorId() {
        return sensorId;
    }
}
