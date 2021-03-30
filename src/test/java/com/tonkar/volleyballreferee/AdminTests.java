package com.tonkar.volleyballreferee;

import com.tonkar.volleyballreferee.dto.ErrorResponse;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class AdminTests extends VbrTests {

    @Autowired
    private AdminService adminService;

    @Test
    public void testNotAuthenticated() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/admin/users"))
                .queryParam("filter", testUserPseudo1)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf(String.format("/admin/users/%s/subscription", UUID.randomUUID())), HttpMethod.GET, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf(String.format("/admin/users/%s/subscription", UUID.randomUUID())), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token")), HttpMethod.POST, emptyPayloadWithAuth(testUserInvalidToken), ErrorResponse.class);
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    public void testForbidden() {
        createUser1();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(urlOf("/admin/users"))
                .queryParam("filter", testUserPseudo1)
                .queryParam("page", 0)
                .queryParam("size", 20);
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(uriBuilder.build(false).toUriString(), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf(String.format("/admin/users/%s/subscription", UUID.randomUUID())), HttpMethod.GET, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf(String.format("/admin/users/%s/subscription", UUID.randomUUID())), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());

        errorResponse = restTemplate.exchange(urlOf(String.format("/admin/users/%s/subscription/%s", UUID.randomUUID(), "token")), HttpMethod.POST, emptyPayloadWithAuth(testUserToken1), ErrorResponse.class);
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());
    }

    @Test
    public void testListUsers() {
        createUser1();

        Page<User> page = adminService.listUsers(null, PageRequest.of(0, 50));
        assertEquals(1, page.getContent().size());

        page = adminService.listUsers("xxxxxx", PageRequest.of(0, 50));
        assertEquals(0, page.getContent().size());

        page = adminService.listUsers("vbr.app.team@gmail.com", PageRequest.of(0, 50));
        assertEquals(1, page.getContent().size());
    }
}
