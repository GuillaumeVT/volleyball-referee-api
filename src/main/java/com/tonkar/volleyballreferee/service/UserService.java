package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User getUser(String userId) throws NotFoundException;

    void createUser(User user) throws ConflictException;

    void deleteUser(User user);

    Count getNumberOfFriendRequestsReceivedBy(User user);

    List<FriendRequest> listFriendRequestsSentBy(User user);

    List<FriendRequest> listFriendRequestsReceivedBy(User user);

    FriendsAndRequests listFriendsAndRequests(User user);

    void sendFriendRequest(User user, String receiverPseudo) throws ConflictException, NotFoundException;

    void acceptFriendRequest(User user, UUID friendRequestId) throws ConflictException, NotFoundException;

    void rejectFriendRequest(User user, UUID friendRequestId);

    void removeFriend(User user, String friendId) throws NotFoundException;
}
