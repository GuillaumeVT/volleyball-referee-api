package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.service.AdminService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

@ContextConfiguration(classes = AdminController.class)
class AdminTests extends VbrControllerTests {

    @MockitoBean
    private AdminService adminService;

    @ParameterizedTest
    @CsvSource(value = { "userToken, FORBIDDEN", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_admin_listUsers(String token, HttpStatus responseCode) {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/admin/users").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, FORBIDDEN", "adminToken, CREATED", "invalidToken, UNAUTHORIZED" })
    void test_admin_createUser(String token, HttpStatus responseCode) {
        var newUser = new NewUserDto(faker.name().firstName(), faker.code().ean13());

        webTestClient
                .post()
                .uri("/admin/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(newUser)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, FORBIDDEN", "adminToken, NO_CONTENT", "invalidToken, UNAUTHORIZED" })
    void test_admin_deleteUser(String token, HttpStatus responseCode) {
        webTestClient
                .delete()
                .uri("/admin/users/%s".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }

    @ParameterizedTest
    @CsvSource(value = { "userToken, FORBIDDEN", "adminToken, OK", "invalidToken, UNAUTHORIZED" })
    void test_admin_updateUserPassword(String token, HttpStatus responseCode) {
        var userPassword = new UserPasswordDto(faker.code().ean13());

        webTestClient
                .post()
                .uri("/admin/users/%s/password".formatted(UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .bodyValue(userPassword)
                .exchange()
                .expectStatus()
                .isEqualTo(responseCode);
    }
}
