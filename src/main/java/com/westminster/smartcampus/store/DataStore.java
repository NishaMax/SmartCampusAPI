package com.westminster.smartcampus.store;

import com.westminster.smartcampus.model.Room;
import com.westminster.smartcampus.model.Sensor;
import com.westminster.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory data store.
 *
 * Note: JAX-RS resources may be instantiated per-request depending on the runtime;
 * therefore shared data must be stored in a singleton and backed by thread-safe
 * data structures.
 */
public final class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * Reading history keyed by sensorId.
     * Each sensorId maps to a synchronized list to keep append operations safe.
     */
    private final Map<String, List<SensorReading>> readingsBySensorId = new ConcurrentHashMap<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ---- Rooms ----

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    /**
     * Creates or replaces a room by id.
     */
    public Room upsertRoom(Room room) {
        if (room == null || room.getId() == null) {
            throw new IllegalArgumentException("Room and room.id must not be null");
        }
        return rooms.put(room.getId(), room);
    }

    public Room deleteRoom(String id) {
        return rooms.remove(id);
    }

    // ---- Sensors ----

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    /**
     * Creates or replaces a sensor by id.
     */
    public Sensor upsertSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null) {
            throw new IllegalArgumentException("Sensor and sensor.id must not be null");
        }
        return sensors.put(sensor.getId(), sensor);
    }

    public Sensor deleteSensor(String id) {
        readingsBySensorId.remove(id);
        return sensors.remove(id);
    }

    // ---- Readings ----

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        if (sensorId == null) {
            throw new IllegalArgumentException("sensorId must not be null");
        }
        List<SensorReading> list = readingsBySensorId.get(sensorId);
        if (list == null) {
            return Collections.emptyList();
        }
        // Return a snapshot copy so callers can't mutate our internal list.
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    public void addReading(String sensorId, SensorReading reading) {
        if (sensorId == null) {
            throw new IllegalArgumentException("sensorId must not be null");
        }
        if (reading == null) {
            throw new IllegalArgumentException("reading must not be null");
        }

        List<SensorReading> list = readingsBySensorId.computeIfAbsent(
                sensorId,
                k -> Collections.synchronizedList(new ArrayList<>())
        );
        list.add(reading);
    }
}
