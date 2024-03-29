package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserTests extends VbrMockedTests {

    private final UserService userService;

    public UserTests(@Autowired UserService userService) {
        super();
        this.userService = userService;
    }

    @Test
    void test_users_get_byPurchaseToken() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());

        // WHEN
        ResponseEntity<UserSummary> userResponse = restTemplate.exchange(String.format("/public/users/%s", user.getPurchaseToken()), HttpMethod.GET, null, UserSummary.class);

        // THEN
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
    }

    @Test
    void test_users_get_byPurchaseToken_invalid() {
        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/public/users/%s", invalidPurchaseToken), HttpMethod.GET, null, ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());
    }

    @Test
    void test_users_create() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());

        // WHEN
        ResponseEntity<User> userResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), User.class);

        // THEN
        assertEquals(HttpStatus.CREATED, userResponse.getStatusCode());
    }

    @Test
    void test_users_create_invalidEmail() {
        // GIVEN
        User user = sandbox.generateUser("invalidemail.com");

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void test_users_create_invalidPurchaseToken() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        user.setPurchaseToken(invalidPurchaseToken);

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.FORBIDDEN, errorResponse.getStatusCode());
    }

    @Test
    void test_users_create_invalidPassword() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        user.setPassword("12345");

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void test_users_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_users_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        User user2 = sandbox.generateUser(user.getEmail());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user2), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_users_create_conflict3() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        User user2 = sandbox.generateUser(faker.internet().safeEmailAddress());
        user2.setPseudo(user.getPseudo());

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users", payloadWithoutAuth(user2), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_users_signIn() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String password = user.getPassword();
        userService.createUser(user);

        // WHEN
        ResponseEntity<UserToken> userResponse = restTemplate.postForEntity("/public/users/token", payloadWithoutAuth(new EmailCredentials(user.getEmail(), password)), UserToken.class);

        // THEN
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
    }

    @Test
    void test_users_signIn_unauthorized() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        String invalidPassword = "invalidPassword";

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity("/public/users/token", payloadWithoutAuth(new EmailCredentials(user.getEmail(), invalidPassword)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    void test_users_updatePassword() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String currentPassword = user.getPassword();
        String newPassword = "NewPassword5678-";
        UserToken userToken = userService.createUser(user);

        // WHEN
        ResponseEntity<UserToken> userResponse = restTemplate.exchange("/users/password", HttpMethod.PATCH, payloadWithAuth(userToken.token(), new UserPasswordUpdate(currentPassword, newPassword)), UserToken.class);

        // THEN
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
    }

    @Test
    void test_users_updatePassword_invalidPassword() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String currentPassword = user.getPassword();
        String newInvalidPassword = "newInvalidPassword";
        UserToken userToken = userService.createUser(user);

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/users/password", HttpMethod.PATCH, payloadWithAuth(userToken.token(), new UserPasswordUpdate(currentPassword, newInvalidPassword)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void test_users_updatePassword_unauthorized() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        String wrongCurrentPassword = "wrongCurrentPassword";
        String newPassword = "NewPassword5678-";

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/users/password", HttpMethod.PATCH, payloadWithAuth(userToken.token(), new UserPasswordUpdate(wrongCurrentPassword, newPassword)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.UNAUTHORIZED, errorResponse.getStatusCode());
    }

    @Test
    void test_users_recoverPassword() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());

        // WHEN
        ResponseEntity<Void> userResponse = restTemplate.postForEntity(String.format("/public/users/password/recover/%s", user.getEmail()), emptyPayloadWithoutAuth(), Void.class);

        // THEN
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
    }

    @Test
    void test_users_recoverPassword_notFound() {
        // GIVEN
        String unknownEmail = faker.internet().safeEmailAddress();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity(String.format("/public/users/password/recover/%s", unknownEmail), emptyPayloadWithoutAuth(), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    void test_users_recoverPassword_follow() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        UUID passwordResetId = userService.initiatePasswordReset(user.getEmail());

        // WHEN
        ResponseEntity<Void> userResponse = restTemplate.exchange(String.format("/public/users/password/follow/%s", passwordResetId), HttpMethod.GET, null, Void.class);

        // THEN
        assertEquals(HttpStatus.FOUND, userResponse.getStatusCode());

        List<String> location = userResponse.getHeaders().get("Location");
        assertNotNull(location);
        assertEquals(1, location.size());

        MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(location.get(0)).build().getQueryParams();
        assertEquals(1, parameters.get("passwordResetId").size());
        assertEquals(passwordResetId, UUID.fromString(parameters.get("passwordResetId").get(0)));
    }

    @Test
    void test_users_recoverPassword_follow_notFound() {
        // GIVEN
        UUID unknownPasswordResetId = UUID.randomUUID();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(String.format("/public/users/password/follow/%s", unknownPasswordResetId), HttpMethod.GET, null, ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    void test_users_recoverPassword_reset() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        UUID passwordResetId = userService.initiatePasswordReset(user.getEmail());
        String newPassword = "NewPassword5678-";

        // WHEN
        ResponseEntity<UserToken> userResponse = restTemplate.postForEntity(String.format("/public/users/password/reset/%s", passwordResetId), payloadWithoutAuth(new UserPassword(newPassword)), UserToken.class);

        // THEN
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
    }

    @Test
    void test_users_recoverPassword_reset_invalidPassword() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        UUID passwordResetId = userService.initiatePasswordReset(user.getEmail());
        String newInvalidPassword = "newInvalidPassword";

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity(String.format("/public/users/password/reset/%s", passwordResetId), payloadWithoutAuth(new UserPassword(newInvalidPassword)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Test
    void test_users_recoverPassword_reset_notFound() {
        // GIVEN
        UUID unknownPasswordResetId = UUID.randomUUID();
        String newPassword = "NewPassword5678-";

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.postForEntity(String.format("/public/users/password/reset/%s", unknownPasswordResetId), payloadWithoutAuth(new UserPassword(newPassword)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
    }

    @Test
    void test_users_updatePseudo() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        UserToken userToken = userService.createUser(user);
        String newPseudo = faker.name().firstName();

        // WHEN
        ResponseEntity<UserSummary> userResponse = restTemplate.exchange("/users/pseudo", HttpMethod.PATCH, payloadWithAuth(userToken.token(), new UserPseudo(newPseudo)), UserSummary.class);

        // THEN
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        assertEquals(newPseudo, Objects.requireNonNull(userResponse.getBody()).pseudo());
    }

    @Test
    void test_users_updatePseudo_conflict() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        UserToken userToken = userService.createUser(user);
        String newPseudo = user.getPseudo();

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/users/pseudo", HttpMethod.PATCH, payloadWithAuth(userToken.token(), new UserPseudo(newPseudo)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
    }

    @Test
    void test_users_updatePseudo_invalid() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        UserToken userToken = userService.createUser(user);
        String newPseudo = "ab";

        // WHEN
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange("/users/pseudo", HttpMethod.PATCH, payloadWithAuth(userToken.token(), new UserPseudo(newPseudo)), ErrorResponse.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }
}
