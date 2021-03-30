package com.tonkar.volleyballreferee.service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User getUser(String userId);

    Optional<User> getUserFromToken(String token);

    UserSummary getUserFromPurchaseToken(String purchaseToken);

    void refreshSubscriptionPurchaseToken(String purchaseToken);

    SubscriptionPurchase getUserSubscription(String purchaseToken);

    UserToken createUser(User user);

    UserToken signInUser(String userEmail, String userPassword);

    Count getNumberOfFriendRequestsReceivedBy(User user);

    List<FriendRequest> listFriendRequestsSentBy(User user);

    List<FriendRequest> listFriendRequestsReceivedBy(User user);

    FriendsAndRequests listFriendsAndRequests(User user);

    void sendFriendRequest(User user, String receiverPseudo);

    void acceptFriendRequest(User user, UUID friendRequestId);

    void rejectFriendRequest(User user, UUID friendRequestId);

    void removeFriend(User user, String friendId);

    UserToken updateUserPassword(User user, UserPasswordUpdate userPasswordUpdate);

    void initiatePasswordReset(String userEmail);

    String followPasswordReset(UUID passwordResetId);

    UserToken resetPassword(UUID passwordResetId, String userPassword);

    void purgeOldPasswordResets(int days);

    void deleteUser(User user);
}
