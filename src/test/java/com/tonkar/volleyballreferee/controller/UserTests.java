package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.service.FriendService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ContextConfiguration(classes = UserController.class)
class UserTests extends VbrControllerTests {

    @MockitoBean
    private FriendService friendService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_users_updateUserPassword(String token, HttpStatus responseCode) {
        var userPassword = new UserPasswordUpdateDto(faker.code().ean13(), faker.code().ean13());

        webTestClient
                .patch()
                .uri("/users/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(userPassword)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, OK", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_users_updateUserPseudo(String token, HttpStatus responseCode) {
        var userPseudo = new UserPseudoDto(faker.name().firstName());

        webTestClient
                .patch()
                .uri("/users/pseudo")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(userPseudo)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
