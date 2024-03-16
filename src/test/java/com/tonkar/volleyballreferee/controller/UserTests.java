package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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

        // WHEN / THEN
        webTestClient
                .get()
                .uri("/public/users/%s".formatted(user.getPurchaseToken()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserSummary.class)
                .value(userSummary -> assertEquals(user.getId(), userSummary.id()));
    }

    @Test
    void test_users_get_byPurchaseToken_invalid() {
        // WHEN / THEN
        webTestClient.get().uri("/public/users/%s".formatted(invalidPurchaseToken)).exchange().expectStatus().isForbidden();
    }

    @Test
    void test_users_create() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user).exchange().expectStatus().isCreated();
    }

    @Test
    void test_users_create_invalidEmail() {
        // GIVEN
        User user = sandbox.generateUser("invalidemail.com");

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user).exchange().expectStatus().isBadRequest();
    }

    @Test
    void test_users_create_invalidPurchaseToken() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        user.setPurchaseToken(invalidPurchaseToken);

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user).exchange().expectStatus().isForbidden();
    }

    @Test
    void test_users_create_invalidPassword() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        user.setPassword("12345");

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user).exchange().expectStatus().isBadRequest();
    }

    @Test
    void test_users_create_conflict() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_users_create_conflict2() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        User user2 = sandbox.generateUser(user.getEmail());

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user2).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_users_create_conflict3() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        User user2 = sandbox.generateUser(faker.internet().safeEmailAddress());
        user2.setPseudo(user.getPseudo());

        // WHEN / THEN
        webTestClient.post().uri("/public/users").bodyValue(user2).exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_users_signIn() {
        // GIVEN
        NewUser user = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        String password = user.password();
        userService.createUser(user);

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/public/users/token")
                .bodyValue(new EmailCredentials(user.email(), password))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserToken.class)
                .value(userToken -> assertNotNull(userToken.token()));
    }

    @Test
    void test_users_signIn_unauthorized() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        String invalidPassword = "invalidPassword";

        // WHEN / THEN
        webTestClient
                .post()
                .uri("/public/users/token")
                .bodyValue(new EmailCredentials(user.getEmail(), invalidPassword))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_users_updatePassword() {
        // GIVEN
        NewUser user = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        String currentPassword = user.password();
        String newPassword = "NewPassword5678-";
        UserToken userToken = userService.createUser(user);

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/users/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(new UserPasswordUpdate(currentPassword, newPassword))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserToken.class)
                .value(userToken1 -> assertNotNull(userToken1.token()));
    }

    @Test
    void test_users_updatePassword_invalidPassword() {
        // GIVEN
        NewUser user = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        String currentPassword = user.password();
        String newInvalidPassword = "newInvalidPassword";
        UserToken userToken = userService.createUser(user);

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/users/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(new UserPasswordUpdate(currentPassword, newInvalidPassword))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void test_users_updatePassword_unauthorized() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        String wrongCurrentPassword = "wrongCurrentPassword";
        String newPassword = "NewPassword5678-";

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/users/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(new UserPasswordUpdate(wrongCurrentPassword, newPassword))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void test_users_recoverPassword() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());

        // WHEN / THEN
        webTestClient.post().uri("/public/users/password/recover/%s".formatted(user.getEmail())).exchange().expectStatus().isOk();
    }

    @Test
    void test_users_recoverPassword_unknownEmail() {
        // GIVEN
        String unknownEmail = faker.internet().safeEmailAddress();

        // WHEN / THEN
        webTestClient.post().uri("/public/users/password/recover/%s".formatted(unknownEmail)).exchange().expectStatus().isOk();
    }

    @Test
    void test_users_recoverPassword_follow() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        UUID passwordResetId = userService.initiatePasswordReset(user.getEmail()).orElseThrow();

        // WHEN / THEN
        webTestClient
                .get()
                .uri(String.format("/public/users/password/follow/%s", passwordResetId))
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .value("location", location -> assertTrue(location.contains(passwordResetId.toString())));
    }

    @Test
    void test_users_recoverPassword_follow_notFound() {
        // GIVEN
        UUID unknownPasswordResetId = UUID.randomUUID();

        // WHEN / THEN
        webTestClient
                .get()
                .uri(String.format("/public/users/password/follow/%s", unknownPasswordResetId))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_users_recoverPassword_reset() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        UUID passwordResetId = userService.initiatePasswordReset(user.getEmail()).orElseThrow();
        String newPassword = "NewPassword5678-";

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/public/users/password/reset/%s", passwordResetId))
                .bodyValue(new UserPassword(newPassword))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserToken.class)
                .value(userToken1 -> assertNotNull(userToken1.token()));
    }

    @Test
    void test_users_recoverPassword_reset_invalidPassword() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        User user = sandbox.getUser(userToken.user().id());
        UUID passwordResetId = userService.initiatePasswordReset(user.getEmail()).orElseThrow();
        String newInvalidPassword = "newInvalidPassword";

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/public/users/password/reset/%s", passwordResetId))
                .bodyValue(new UserPassword(newInvalidPassword))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void test_users_recoverPassword_reset_notFound() {
        // GIVEN
        UUID unknownPasswordResetId = UUID.randomUUID();
        String newPassword = "NewPassword5678-";

        // WHEN / THEN
        webTestClient
                .post()
                .uri(String.format("/public/users/password/reset/%s", unknownPasswordResetId))
                .bodyValue(new UserPassword(newPassword))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void test_users_updatePseudo() {
        // GIVEN
        NewUser user = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        UserToken userToken = userService.createUser(user);
        String newPseudo = faker.name().firstName();

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/users/pseudo")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(new UserPseudo(newPseudo))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(UserSummary.class)
                .value(userSummary -> assertEquals(newPseudo, userSummary.pseudo()));
    }

    @Test
    void test_users_updatePseudo_conflict() {
        // GIVEN
        NewUser user = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        UserToken userToken = userService.createUser(user);
        String newPseudo = user.pseudo();

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/users/pseudo")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(new UserPseudo(newPseudo))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void test_users_updatePseudo_invalid() {
        // GIVEN
        NewUser user = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        UserToken userToken = userService.createUser(user);
        String newPseudo = "ab";

        // WHEN / THEN
        webTestClient
                .patch()
                .uri("/users/pseudo")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .bodyValue(new UserPseudo(newPseudo))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void test_users_delete() {
        // GIVEN
        UserToken userToken = sandbox.createUser();

        // WHEN / THEN
        webTestClient
                .delete()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken.token()))
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
