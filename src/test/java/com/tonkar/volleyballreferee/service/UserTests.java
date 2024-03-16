package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class UserTests extends VbrMockedTests {

    private final UserService userService;

    private final GameService gameService;

    public UserTests(@Autowired UserService userService, @Autowired GameService gameService) {
        super();
        this.userService = userService;
        this.gameService = gameService;
    }

    @Test
    void test_users_updatePseudo() {
        // GIVEN
        NewUser newUser = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        userService.createUser(newUser);

        NewUser newUser2 = sandbox.generateNewUser(faker.internet().safeEmailAddress());
        userService.createUser(newUser2);

        User user = userService.getUser(newUser.id());
        User user2 = userService.getUser(newUser2.id());

        sandbox.addFriend(user, user2);

        Game game = sandbox.generateBeachGame(user.getId());
        gameService.createGame(user, game);

        String newPseudo = faker.name().firstName();

        // WHEN
        UserSummary userSummary = userService.updateUserPseudo(user, newPseudo);

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
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String newPseudo = user.getPseudo();

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> userService.updateUserPseudo(user, newPseudo));
    }

    @Test
    void test_users_updatePseudo_invalid() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String newPseudo = "ab";

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> userService.updateUserPseudo(user, newPseudo));
    }
}
