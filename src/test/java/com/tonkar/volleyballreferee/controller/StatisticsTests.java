package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.service.StatisticsService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ContextConfiguration(classes = StatisticsController.class)
class StatisticsTests extends VbrControllerTests {

    @MockitoBean
    private StatisticsService statisticsService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_statistics_getUserStatistics(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri("/statistics")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
