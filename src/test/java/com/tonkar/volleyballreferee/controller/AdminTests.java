package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

class AdminTests extends VbrMockedTests {

    private final UserService userService;

    private final UserDao userDao;

    public AdminTests(@Autowired UserService userService, @Autowired UserDao userDao) {
        super();
        this.userService = userService;
        this.userDao = userDao;
    }

    @Test
    void test_admin_unauthorized() {
        final var invalidToken = "invalid";

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/admin/users").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .get()
                .uri(String.format("/admin/users/%s/subscription", UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri(String.format("/admin/users/%s/subscription", UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();

        webTestClient
                .post()
                .uri(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"))
                .header(HttpHeaders.AUTHORIZATION, bearer(invalidToken))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_admin_forbidden() {
        final var userToken = sandbox.createUser();

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/admin/users").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isForbidden();

        webTestClient
                .get()
                .uri(String.format("/admin/users/%s/subscription", UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isForbidden();

        webTestClient
                .post()
                .uri(String.format("/admin/users/%s/subscription", UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isForbidden();

        webTestClient
                .post()
                .uri(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void test_admin() {
        final var userToken = sandbox.createUser();

        final var user = userService.getUser(userToken.user().id());
        user.setAdmin(true);
        userDao.save(user);

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/admin/users").queryParam("page", 0).queryParam("size", 20).build())
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .get()
                .uri(String.format("/admin/users/%s/subscription", UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();

        webTestClient
                .post()
                .uri(String.format("/admin/users/%s/subscription", UUID.randomUUID()))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();

        webTestClient
                .post()
                .uri(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"))
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
