package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminTests extends VbrMockedTests {

    @Test
    public void test_admin_listUsers(@Autowired AdminService adminService) {
        // GIVEN
        sandbox.createUser();

        // WHEN
        Page<User> page = adminService.listUsers(null, PageRequest.of(0, 50));

        // THEN
        assertEquals(1, page.getContent().size());
    }

    @Test
    public void test_admin_listUsers_filter_empty(@Autowired AdminService adminService) {
        // GIVEN
        sandbox.createUser();

        // WHEN
        Page<User> page = adminService.listUsers("xxxxxx", PageRequest.of(0, 50));

        // THEN
        assertEquals(0, page.getContent().size());
    }

    @Test
    public void test_admin_listUsers_filter(@Autowired AdminService adminService) {
        // GIVEN
        String email = faker.internet().safeEmailAddress();
        sandbox.createUser();

        // WHEN
        Page<User> page = adminService.listUsers(email, PageRequest.of(0, 50));

        // THEN
        assertEquals(1, page.getContent().size());
    }
}
