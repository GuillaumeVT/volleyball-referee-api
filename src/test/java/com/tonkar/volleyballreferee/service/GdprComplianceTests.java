package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GdprComplianceTests extends VbrMockedTests {

    private final GdprComplianceService gdprComplianceService;

    public GdprComplianceTests(@Autowired GdprComplianceService gdprComplianceService) {
        super();
        this.gdprComplianceService = gdprComplianceService;
    }

    @Test
    void test_gdprCompliance_deleteUser() {
        // GIVEN
        User user = sandbox.generateUser(faker.internet().safeEmailAddress());

        // WHEN
        assertDoesNotThrow(() -> gdprComplianceService.deleteUser(user, true));

        // THEN
        assertThrows(ResponseStatusException.class, () -> sandbox.getUser(user.getId()));
    }
}
