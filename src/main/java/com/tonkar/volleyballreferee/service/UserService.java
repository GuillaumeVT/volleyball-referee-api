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

    void deleteUser(String userId);

    Count getNumberOfFriendRequestsReceivedBy(String receiverId);

    List<FriendRequest> listFriendRequestsSentBy(String senderId);

    List<FriendRequest> listFriendRequestsReceivedBy(String receiverId);

    FriendsAndRequests listFriendsAndRequests(User user);

    void sendFriendRequest(String senderId, String receiverPseudo) throws ConflictException, NotFoundException;

    void acceptFriendRequest(String userId, UUID friendRequestId) throws ConflictException, NotFoundException;

    void rejectFriendRequest(String userId, UUID friendRequestId);

    void removeFriend(String userId, String friendId) throws NotFoundException;
}
