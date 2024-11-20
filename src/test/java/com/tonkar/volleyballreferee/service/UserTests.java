package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class UserTests extends VbrServiceTests {

    private final UserService userService;

    private final GameService gameService;

    public UserTests(@Autowired UserService userService, @Autowired GameService gameService) {
        super();
        this.userService = userService;
        this.gameService = gameService;
    }

    @Test
    void test_users_signIn() {
        // GIVEN
        User user = sandbox.createAndGetUser();
        String currentPassword = sandbox.validPassword();

        // WHEN
        var token = userService.signInUser(user.getPseudo(), currentPassword);

        // WHEN
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.token());
    }

    @Test
    void test_users_signIn_unauthorized() {
        // GIVEN
        User user = sandbox.createAndGetUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> userService.signInUser(user.getPseudo(), sandbox.invalidPassword()));
    }

    @Test
    void test_users_updatePassword() {
        // GIVEN
        User user = sandbox.createAndGetUser();
        String currentPassword = sandbox.validPassword();
        String newPassword = "NewPassword5678-";

        // WHEN
        var token = userService.updateUserPassword(user, new UserPasswordUpdateDto(currentPassword, newPassword));

        // WHEN
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.token());
        Assertions.assertDoesNotThrow(() -> userService.signInUser(user.getPseudo(), newPassword));
    }

    @Test
    void test_users_updatePassword_invalidPassword() {
        // GIVEN
        User user = sandbox.createAndGetUser();
        String currentPassword = sandbox.validPassword();
        String newInvalidPassword = "newInvalidPassword";

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class,
                                () -> userService.updateUserPassword(user, new UserPasswordUpdateDto(currentPassword, newInvalidPassword)));
    }

    @Test
    void test_users_updatePassword_unauthorized() {
        // GIVEN
        User user = sandbox.createAndGetUser();
        String wrongCurrentPassword = "wrongCurrentPassword";
        String newPassword = "NewPassword5678-";

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class,
                                () -> userService.updateUserPassword(user, new UserPasswordUpdateDto(wrongCurrentPassword, newPassword)));
    }

    @Test
    void test_users_updatePseudo() {
        // GIVEN
        User user = sandbox.createAndGetUser();
        User user2 = sandbox.createAndGetUser();

        sandbox.addFriend(user, user2);

        Game game = sandbox.generateBeachGame(user.getId());
        gameService.upsertGame(user, game);

        String newPseudo = faker.name().firstName();

        // WHEN
        UserSummaryDto userSummary = userService.updateUserPseudo(user, newPseudo);

        // THEN
        assertEquals(newPseudo, userSummary.pseudo());
        assertEquals(newPseudo, gameService.getGame(game.getId()).getRefereeName());
        assertTrue(userService
                           .getUser(user2.getId())
                           .getFriends()
                           .stream()
                           .anyMatch(friend -> friend.getId().equals(user.getId()) && friend.getPseudo().equals(newPseudo)));
    }

    @Test
    void test_users_updatePseudo_conflict() {
        // GIVEN
        User user = sandbox.createAndGetUser();
        String newPseudo = user.getPseudo();

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> userService.updateUserPseudo(user, newPseudo));
    }
}
