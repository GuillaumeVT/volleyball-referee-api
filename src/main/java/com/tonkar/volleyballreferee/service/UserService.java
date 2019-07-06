package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.dto.UserPasswordUpdate;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User getUser(String userId) throws NotFoundException;

    UserToken createUser(User user) throws UnauthorizedException, ForbiddenException, ConflictException, NotFoundException, BadRequestException;

    UserToken signInUser(String userEmail, String userPassword) throws NotFoundException, UnauthorizedException, ForbiddenException;

    Optional<User> getUserFromToken(String token);

    Count getNumberOfFriendRequestsReceivedBy(User user);

    List<FriendRequest> listFriendRequestsSentBy(User user);

    List<FriendRequest> listFriendRequestsReceivedBy(User user);

    FriendsAndRequests listFriendsAndRequests(User user);

    void sendFriendRequest(User user, String receiverPseudo) throws ConflictException, NotFoundException;

    void acceptFriendRequest(User user, UUID friendRequestId) throws ConflictException, NotFoundException;

    void rejectFriendRequest(User user, UUID friendRequestId);

    void removeFriend(User user, String friendId) throws NotFoundException;

    UserToken updateUserPassword(User user, UserPasswordUpdate userPasswordUpdate) throws BadRequestException, ConflictException, NotFoundException, UnauthorizedException, ForbiddenException;

    void initiatePasswordReset(String userEmail) throws NotFoundException;

    String followPasswordReset(UUID passwordResetId) throws NotFoundException;

    UserToken resetPassword(UUID passwordResetId, String userPassword) throws BadRequestException, ConflictException, NotFoundException, UnauthorizedException, ForbiddenException;

    void purgeOldPasswordResets(int days);

}
