package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.StatisticsGroup;
import com.tonkar.volleyballreferee.dto.UserToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticsTests extends VbrMockedTests {

    @Test
    public void test_statistics_unauthorized() {
        final var invalidToken = "invalid";

        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/statistics", HttpMethod.GET, emptyPayloadWithAuth(invalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void test_statistics_get() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN
        ResponseEntity<StatisticsGroup> statisticsResponse = restTemplate.exchange("/statistics", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), StatisticsGroup.class);

        // THEN
        assertEquals(HttpStatus.OK, statisticsResponse.getStatusCode());
    }

    @Test
    public void test_statistics_public_get() {
        // WHEN
        ResponseEntity<StatisticsGroup> statisticsResponse = restTemplate.exchange("/public/statistics", HttpMethod.GET, emptyPayloadWithoutAuth(), StatisticsGroup.class);

        // THEN
        assertEquals(HttpStatus.OK, statisticsResponse.getStatusCode());
    }
}