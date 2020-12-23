package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.dto.Statistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class StatisticsTests extends VbrTests {

    @Test
    public void testNotAuthenticated() {
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(urlOf("/statistics"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void testManageStatistics() {
        createUser1();

        ResponseEntity<Statistics> getStatisticsResponse = restTemplate.exchange(urlOf("/statistics"), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), Statistics.class);
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());

        getStatisticsResponse = restTemplate.exchange(urlOf("/public/statistics"), HttpMethod.GET, emptyPayloadWithoutAuth(), Statistics.class);
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());
    }

}
