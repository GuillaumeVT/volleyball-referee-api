package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class StatisticsTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/statistics", HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void testManageStatistics() {
        createUser1();

        ResponseEntity<Statistics> getStatisticsResponse = restTemplate.exchange("/statistics", HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Statistics.class);
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());

        getStatisticsResponse = restTemplate.exchange("/public/statistics", HttpMethod.GET, emptyPayloadWithoutAuth(), Statistics.class);
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());
    }

}
