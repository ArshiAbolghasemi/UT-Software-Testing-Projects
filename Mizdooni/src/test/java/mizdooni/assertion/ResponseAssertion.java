package mizdooni.assertion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.HttpStatus;

import mizdooni.response.Response;

public class ResponseAssertion {

    public static void checkResponse(Response response, HttpStatus status, boolean success, Object data, String error,
        String message) {
        assertEquals(status, response.getStatus());
        assertEquals(success, response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
    }
}

