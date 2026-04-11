package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;
import com.westminster.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Sub-resource handling /sensors/{sensorId}/readings.
 */
@Path("/readings")
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(@PathParam("sensorId") String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorId() {
        return sensorId;
    }

    /**
     * GET /sensors/{sensorId}/readings
     */
    @GET
    public Response getReadingHistory() {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage("Sensor not found"))
                    .build();
        }

        List<SensorReading> history = dataStore.getReadingsForSensor(sensorId);
        return Response.ok(history).build();
    }

    /**
     * POST /sensors/{sensorId}/readings
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage("Sensor not found"))
                    .build();
        }
        if (reading == null || reading.getId() == null || reading.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Reading id is required"))
                    .build();
        }

        dataStore.addReading(sensorId, reading);

        // Side-effect: keep Sensor.currentValue consistent with latest reading.
        sensor.setCurrentValue(reading.getValue());
        dataStore.upsertSensor(sensor);

        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading)
                .build();
    }

    /**
     * Minimal error body (kept simple for now).
     */
    public static class ErrorMessage {
        private String message;

        public ErrorMessage() {
        }

        public ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
