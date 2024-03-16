package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminTests extends VbrMockedTests {

    private final AdminService adminService;

    public AdminTests(@Autowired AdminService adminService) {
        super();
        this.adminService = adminService;
    }

    @Test
    void test_admin_listUsers() {
        // GIVEN
        sandbox.createUser();

        // WHEN
        Page<User> page = adminService.listUsers(null, PageRequest.of(0, 50));

        // THEN
        assertEquals(1, page.getContent().size());
    }

    @Test
    void test_admin_listUsers_filter_empty() {
        // GIVEN
        sandbox.createUser();

        // WHEN
        Page<User> page = adminService.listUsers("xxxxxx", PageRequest.of(0, 50));

        // THEN
        assertEquals(0, page.getContent().size());
    }

    @Test
    void test_admin_listUsers_filter() {
        // GIVEN
        String email = faker.internet().safeEmailAddress();
        sandbox.createUser(email);

        // WHEN
        Page<User> page = adminService.listUsers(email, PageRequest.of(0, 50));

        // THEN
        assertEquals(1, page.getContent().size());
    }
}
