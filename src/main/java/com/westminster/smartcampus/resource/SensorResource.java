package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.exception.BadRequestException;
import com.westminster.smartcampus.exception.ConflictException;
import com.westminster.smartcampus.exception.UnprocessableEntityException;
import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            throw new BadRequestException("Sensor id is required");
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            throw new BadRequestException("Sensor type is required");
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            throw new BadRequestException("Sensor status is required");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new BadRequestException("Sensor roomId is required");
        }

        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new UnprocessableEntityException("Referenced roomId does not exist");
        }

        boolean alreadyExists = dataStore.getSensor(sensor.getId()) != null;
        if (alreadyExists) {
            throw new ConflictException("Sensor with id already exists");
        }

        dataStore.upsertSensor(sensor);

        // Link sensor to the room
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
            dataStore.upsertRoom(room);
        }

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
                .build();
    }
}
