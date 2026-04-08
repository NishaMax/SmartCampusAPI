package com.westminster.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application entry point.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApp extends Application {
    // Intentionally empty: Jersey will auto-discover resources/providers on the classpath.
}
