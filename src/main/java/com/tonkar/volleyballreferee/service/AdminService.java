package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class AdminService {

    private final AuthService authService;
    private final UserService userService;
    private final UserDao     userDao;
    private final String      adminPseudo;
    private final String      adminPassword;

    public AdminService(AuthService authService,
                        UserService userService,
                        UserDao userDao,
                        @Value("${vbr.admin.pseudo}") String adminPseudo,
                        @Value("${vbr.admin.password}") String adminPassword) {
        this.authService = authService;
        this.userService = userService;
        this.userDao = userDao;
        this.adminPseudo = adminPseudo;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public void initAdmin() {
        if (StringUtils.isNotBlank(adminPseudo) && StringUtils.isNotBlank(adminPassword) && !userDao.existsByPseudo(adminPseudo)) {
            createUser(new NewUserDto(adminPseudo, adminPassword), true);
        }
    }

    public Page<UserSummaryDto> listUsers(String filter, Pageable pageable) {
        return userDao.listUsers(filter, pageable);
    }

    public UserSummaryDto createUser(NewUserDto newUser) {
        return createUser(newUser, false);
    }

    private UserSummaryDto createUser(NewUserDto newUser, boolean isAdmin) {
        String password = newUser.password().trim();
        authService.validatePassword(password);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPseudo(newUser.pseudo());

        boolean pseudoExists = userDao.existsByPseudo(user.getPseudo());

        if (pseudoExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Found an existing user with pseudo %s", user.getPseudo()));
        }

        user.setPassword(authService.getPasswordEncoder().encode(password));
        user.setFailedAuthentication(new User.FailedAuthentication(0, Instant.now().toEpochMilli()));
        user.setEnabled(true);
        user.setAdmin(isAdmin);
        user.setFriends(new ArrayList<>());
        user.setCreatedAt(Instant.now().toEpochMilli());
        userDao.save(user);
        log.info("Created user with id {} and pseudo {}}", user.getId(), user.getPseudo());
        return new UserSummaryDto(user.getId(), user.getPseudo(), user.isAdmin());
    }

    public void deleteUser(UUID userId) {
        User user = userService.getUser(userId);

        if (user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not delete admin user %s".formatted(userId));
        }

        user.getFriends().forEach(friend -> userDao.removeFriend(friend.getId(), user.getId()));
        userDao.delete(user);
    }

    public void updateUserPassword(UUID userId, String newPassword) {
        User user = userService.getUser(userId);
        newPassword = newPassword.trim();

        userService.updateUserPassword(user, newPassword);
    }
}
