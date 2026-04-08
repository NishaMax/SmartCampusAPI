package com.westminster.smartcampus.resource;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public Collection<Room> getAllRooms() {
        return dataStore.getAllRooms();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Room id is required"))
                    .build();
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Room name is required"))
                    .build();
        }
        if (room.getCapacity() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Room capacity must be >= 0"))
                    .build();
        }

        // Ensure sensorIds isn't null (so JSON serialization stays consistent)
        if (room.getSensorIds() == null) {
            room.setSensorIds(null);
        }

        boolean alreadyExists = dataStore.getRoom(room.getId()) != null;
        dataStore.upsertRoom(room);

        if (alreadyExists) {
            // If room exists, treat as conflict (we'll refine behaviour later if needed)
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorMessage("Room with id already exists"))
                    .build();
        }

        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room)
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
