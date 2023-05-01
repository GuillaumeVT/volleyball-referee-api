package com.tonkar.volleyballreferee.controller;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.UserService;
import com.tonkar.volleyballreferee.util.TestPageImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
    void test_admin_forbidden() {
        final var userToken = sandbox.createUser();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/admin/users")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());
    }

    @Test
    void test_admin() {
        final var userToken = sandbox.createUser();

        final var user = userService.getUser(userToken.user().id());
        user.setAdmin(true);
        userDao.save(user);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("/admin/users")
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<TestPageImpl<User>> pageResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, pageResponse.getStatusCode());

        ResponseEntity<SubscriptionPurchase> subscriptionResponse = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.GET, emptyPayloadWithAuth(userToken.token()), SubscriptionPurchase.class);
        assertNotEquals(HttpStatus.FORBIDDEN, subscriptionResponse.getStatusCode());

        ResponseEntity<Void> response = restTemplate.exchange(String.format("/admin/users/%s/subscription", UUID.randomUUID()), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), Void.class);
        assertNotEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        response = restTemplate.exchange(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token"), HttpMethod.POST, emptyPayloadWithAuth(userToken.token()), Void.class);
        assertNotEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
