package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.UserPasswordUpdate;
import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User getUser(String userId);

    Optional<User> getUserFromToken(String token);

    UserSummary getUserFromPurchaseToken(String purchaseToken);

    UserToken createUser(User user);

    UserToken signInUser(String userEmail, String userPassword);

    UserToken updateUserPassword(User user, UserPasswordUpdate userPasswordUpdate);

    UUID initiatePasswordReset(String userEmail);

    String followPasswordReset(UUID passwordResetId);

    UserToken resetPassword(UUID passwordResetId, String userPassword);

    void purgeOldPasswordResets(int days);

    void deleteUser(User user);
}
