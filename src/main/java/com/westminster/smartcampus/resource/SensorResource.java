package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = dataStore.getAllSensors();
        if (type == null || type.trim().isEmpty()) {
            return all;
        }

        String wanted = type.trim();
        List<Sensor> filtered = new ArrayList<>();
        for (Sensor s : all) {
            if (s != null && s.getType() != null && s.getType().equalsIgnoreCase(wanted)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Sensor id is required"))
                    .build();
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Sensor type is required"))
                    .build();
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Sensor status is required"))
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Sensor roomId is required"))
                    .build();
        }

        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            // Part 5 will introduce a dedicated 422 exception + mapper.
            return Response.status(422)
                    .entity(new ErrorMessage("Referenced roomId does not exist"))
                    .build();
        }

        boolean alreadyExists = dataStore.getSensor(sensor.getId()) != null;
        dataStore.upsertSensor(sensor);

        if (alreadyExists) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorMessage("Sensor with id already exists"))
                    .build();
        }

        // Link sensor to the room
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
            dataStore.upsertRoom(room);
        }

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
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
