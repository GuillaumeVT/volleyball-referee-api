package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTests extends VbrMockedTests {

    @Test
    void test_users_updatePseudo(@Autowired UserService userService, @Autowired GameService gameService) {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        userService.createUser(user);
        Game game = sandbox.generateBeachGame(user.getId());
        gameService.createGame(user, game);
        String newPseudo = faker.name().firstName();

        // WHEN
        UserSummary userSummary = userService.updateUserPseudo(user, newPseudo);

        // THEN
        assertEquals(newPseudo, userSummary.pseudo());
        assertEquals(newPseudo, gameService.getGame(game.getId()).getRefereeName());
    }

    @Test
    void test_users_updatePseudo_conflict(@Autowired UserService userService) {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String newPseudo = user.getPseudo();

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> userService.updateUserPseudo(user, newPseudo));
    }

    @Test
    void test_users_updatePseudo_invalid(@Autowired UserService userService) {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());
        String newPseudo = "ab";

        // WHEN / THEN
        assertThrows(ResponseStatusException.class, () -> userService.updateUserPseudo(user, newPseudo));
    }
}
