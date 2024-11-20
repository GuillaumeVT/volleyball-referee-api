package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminTests extends VbrServiceTests {

    private final AdminService adminService;

    private final UserService userService;

    public AdminTests(@Autowired AdminService adminService, @Autowired UserService userService) {
        super();
        this.adminService = adminService;
        this.userService = userService;
    }

    @Test
    void test_admin_listUsers() {
        // GIVEN
        sandbox.createUser();

        // WHEN
        Page<UserSummaryDto> page = adminService.listUsers(null, PageRequest.of(0, 50));

        // THEN
        assertEquals(1, page.getContent().size());
    }

    @Test
    void test_admin_listUsers_filter_empty() {
        // GIVEN
        sandbox.createUser();

        // WHEN
        Page<UserSummaryDto> page = adminService.listUsers("xxxxxx", PageRequest.of(0, 50));

        // THEN
        assertEquals(0, page.getContent().size());
    }

    @Test
    void test_admin_listUsers_filter() {
        // GIVEN
        var user = sandbox.createUser();

        // WHEN
        Page<UserSummaryDto> page = adminService.listUsers(user.pseudo(), PageRequest.of(0, 50));

        // THEN
        assertEquals(1, page.getContent().size());
    }

    @Test
    void test_admin_createUser() {
        // GIVEN
        var newUser = new NewUserDto(faker.name().firstName(), sandbox.validPassword());

        // WHEN
        var user = adminService.createUser(newUser);

        // THEN
        Assertions.assertNotNull(user);
        Assertions.assertEquals(newUser.pseudo(), user.pseudo());
    }

    @Test
    void test_admin_createUser_duplicatedPseudo() {
        // GIVEN
        var user = sandbox.createUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class,
                                () -> adminService.createUser(new NewUserDto(user.pseudo(), sandbox.validPassword())));
    }

    @Test
    void test_admin_createUser_invalidPassword() {
        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class,
                                () -> adminService.createUser(new NewUserDto(faker.name().firstName(), sandbox.invalidPassword())));
    }

    @Test
    void test_admin_deleteUser() {
        // GIVEN
        var user = sandbox.createUser();

        // WHEN
        adminService.deleteUser(user.id());

        // THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> userService.getUser(user.id()));
    }

    @Test
    void test_admin_updateUserPassword() {
        // GIVEN
        var user = sandbox.createUser();
        String newPassword = "ABCDEfghijk12345!";

        // WHEN // THEN
        Assertions.assertDoesNotThrow(() -> adminService.updateUserPassword(user.id(), newPassword));
    }

    @Test
    void test_admin_updateUserPassword_invalidPassword() {
        // GIVEN
        var user = sandbox.createUser();
        String newPassword = sandbox.invalidPassword();

        // WHEN // THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> adminService.updateUserPassword(user.id(), newPassword));
    }
}
