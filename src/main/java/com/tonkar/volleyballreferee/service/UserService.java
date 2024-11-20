package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final AuthService      authService;
    private final UserDao          userDao;
    private final GameDao          gameDao;
    private final FriendRequestDao friendRequestDao;

    public User getUser(UUID userId) {
        return userDao
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", userId)));
    }

    public UserTokenDto signInUser(String userPseudo, String userPassword) {
        userPassword = userPassword.trim();

        User user = userDao
                .findByPseudo(userPseudo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", userPseudo)));

        if (isLocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Access is locked for user %s", user.getId()));
        }

        if (authService.getPasswordEncoder().matches(userPassword, user.getPassword())) {
            userSignedIn(user);
            return authService.generateToken(user);
        } else {
            addFailedAuthentication(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid password for user %s", userPseudo));
        }
    }

    public UserTokenDto updateUserPassword(User user, UserPasswordUpdateDto userPasswordUpdate) {
        if (!authService.getPasswordEncoder().matches(userPasswordUpdate.currentPassword(), user.getPassword())) {
            addFailedAuthentication(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid password for user %s", user.getId()));
        }

        String newPassword = userPasswordUpdate.newPassword().trim();
        updateUserPassword(user, newPassword);

        return signInUser(user.getPseudo(), newPassword);
    }

    public UserSummaryDto updateUserPseudo(User user, String newPseudo) {
        boolean pseudoExists = userDao.existsByPseudo(newPseudo);

        if (pseudoExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Found an existing user with pseudo %s", newPseudo));
        } else {
            if (!userDao.updateUserPseudo(user.getId(), newPseudo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                  String.format("Unable to set pseudo %s for user %s", newPseudo, user.getId()));
            }
            userDao.updateFriendPseudo(user.getId(), newPseudo);
            gameDao.updateUserPseudo(user.getId(), newPseudo);
            friendRequestDao.updateSenderPseudo(user.getId(), newPseudo);
            friendRequestDao.updateReceiverPseudo(user.getId(), newPseudo);
        }

        user = userDao
                .findByPseudo(newPseudo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", newPseudo)));

        return new UserSummaryDto(user.getId(), user.getPseudo(), user.isAdmin());
    }

    private boolean isLocked(User user) {
        return user.getFailedAuthentication().getAttempts() >= 5 && LocalDateTime
                .now(ZoneOffset.UTC)
                .isBefore(LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getFailedAuthentication().getResetsAt()), ZoneOffset.UTC));
    }

    private void userSignedIn(User user) {
        userDao.updateUserSignedIn(user.getId(), Instant.now().toEpochMilli());
    }

    private void addFailedAuthentication(User user) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime willResetAt = now.plusMinutes(20L);
        int maxFailedAttempts = 5;

        User.FailedAuthentication failedAuthentication = user.getFailedAuthentication();
        if (now.isAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getFailedAuthentication().getResetsAt()), ZoneOffset.UTC))) {
            // Reset attempts
            failedAuthentication = new User.FailedAuthentication(0, now.toInstant(ZoneOffset.UTC).toEpochMilli());
        }

        failedAuthentication.setAttempts(user.getFailedAuthentication().getAttempts() == maxFailedAttempts ? maxFailedAttempts : user
                .getFailedAuthentication()
                .getAttempts() + 1);
        failedAuthentication.setResetsAt(willResetAt.toInstant(ZoneOffset.UTC).toEpochMilli());
        userDao.addFailedAuthentication(user.getId(), failedAuthentication);
    }

    public void updateUserPassword(User user, String password) {
        if (authService.getPasswordEncoder().matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Provided password is the same as the previous");
        }

        authService.validatePassword(password);

        if (!userDao.updateUserPassword(user.getId(), authService.getPasswordEncoder().encode(password))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Unable to set new password for user %s", user.getId()));
        }
    }
}
