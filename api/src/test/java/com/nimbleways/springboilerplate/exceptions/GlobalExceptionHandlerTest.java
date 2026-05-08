package com.nimbleways.springboilerplate.exceptions;

import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/orders/42/processOrder");
    }

    @Test
    void orderNotFound_mapsTo404() {
        ResponseEntity<ErrorResponse> response = handler.handleOrderNotFound(
                new OrderNotFoundException(42L), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = bodyOf(response);
        assertEquals(404, body.status());
        assertEquals("Not Found", body.error());
        assertEquals("Order not found: 42", body.message());
        assertEquals("/orders/42/processOrder", body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void typeMismatch_mapsTo400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("orderId");
        when(ex.getValue()).thenReturn("abc");

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = bodyOf(response);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("Invalid value for parameter 'orderId': abc", body.message());
    }

    @Test
    void constraintViolation_mapsTo400() {
        ConstraintViolationException ex =
                new ConstraintViolationException("orderId must be positive", Collections.emptySet());

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, bodyOf(response).status());
    }

    @Test
    void methodNotAllowed_mapsTo405() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("GET");

        ResponseEntity<ErrorResponse> response = handler.handleMethodNotAllowed(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(405, bodyOf(response).status());
    }

    @Test
    void anyUnknownException_mapsTo500_andDoesNotLeakDetails() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new NullPointerException("internal pointer issue"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = bodyOf(response);
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals("Unexpected error", body.message());
    }

    @Test
    void runtimeException_alsoMapsTo500_provingCatchAllCoversEverything() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new IllegalStateException("anything goes here"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", bodyOf(response).message());
    }

    private static ErrorResponse bodyOf(ResponseEntity<ErrorResponse> response) {
        return Objects.requireNonNull(response.getBody(), "ErrorResponse body must not be null");
    }
}
