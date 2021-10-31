package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.Statistics;
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
        UserToken userToken = createUser();

        // WHEN
        ResponseEntity<Statistics> getStatisticsResponse = restTemplate.exchange("/statistics", HttpMethod.GET, emptyPayloadWithAuth(userToken.getToken()), Statistics.class);

        // THEN
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());
    }

    @Test
    public void test_statistics_public_get() {
        // WHEN
        ResponseEntity<Statistics> getStatisticsResponse = restTemplate.exchange("/public/statistics", HttpMethod.GET, emptyPayloadWithoutAuth(), Statistics.class);

        // THEN
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());
    }
}
