package com.tonkar.volleyballreferee;

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
        ResponseEntity<Statistics> getStatisticsResponse = restTemplate.exchange(urlOf("/api/v3/statistics"), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidAuth), Statistics.class);
        assertEquals(HttpStatus.UNAUTHORIZED, getStatisticsResponse.getStatusCode());
    }

    @Test
    public void testManageStatistics() {
        ResponseEntity<Statistics> getStatisticsResponse = restTemplate.exchange(urlOf("/api/v3/statistics"), HttpMethod.GET, emptyPayloadWithAuth(testUser1Auth), Statistics.class);
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());

        getStatisticsResponse = restTemplate.exchange(urlOf("/api/v3/public/statistics"), HttpMethod.GET, emptyPayloadWithoutAuth(), Statistics.class);
        assertEquals(HttpStatus.OK, getStatisticsResponse.getStatusCode());
    }

}
