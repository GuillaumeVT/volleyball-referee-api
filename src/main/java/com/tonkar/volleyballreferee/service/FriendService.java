package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;

import java.util.List;
import java.util.UUID;

public interface FriendService {

    Count getNumberOfFriendRequestsReceivedBy(User user);

    List<FriendRequest> listFriendRequestsSentBy(User user);

    List<FriendRequest> listFriendRequestsReceivedBy(User user);

    FriendsAndRequests listFriendsAndRequests(User user);

    UUID sendFriendRequest(User user, String receiverPseudo);

    void acceptFriendRequest(User user, UUID friendRequestId);

    void rejectFriendRequest(User user, UUID friendRequestId);

    void removeFriend(User user, String friendId);
}
