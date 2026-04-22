package com.westminster.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Request/response logging using java.util.logging.Logger.
 */
@Provider
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_NANOS = "smartcampus.startNanos";
    private static final Logger LOGGER = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_NANOS, System.nanoTime());
        String msg = "--> " + requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri();
        LOGGER.log(Level.INFO, msg);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        long start = 0L;
        Object prop = requestContext.getProperty(START_NANOS);
        if (prop instanceof Long) {
            start = (Long) prop;
        }
        long elapsedMs = start == 0L ? -1L : (System.nanoTime() - start) / 1_000_000L;

        String path = requestContext.getUriInfo().getRequestUri().toString();
        String msg = "<-- " + requestContext.getMethod() + " " + path + " " + responseContext.getStatus() + " (" + elapsedMs + "ms)";
        LOGGER.log(Level.INFO, msg);
    }
}
