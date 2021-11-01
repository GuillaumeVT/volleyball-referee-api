package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminTests extends VbrMockedTests {

    @Test
    public void test_admin_unauthorized() {
        final var invalidToken = "invalid";
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/admin/users")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.POST, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"), HttpMethod.POST, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void test_admin_forbidden() {
        final var userToken = sandbox.createUser();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/admin/users")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.POST, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"), HttpMethod.POST, emptyPayloadWithAuth(userToken.getToken()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());
    }
}
