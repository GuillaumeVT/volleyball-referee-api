package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;

import java.util.List;

public record FriendsAndRequests(
        List<User.Friend> friends,
        List<FriendRequest> receivedFriendRequests,
        List<FriendRequest> sentFriendRequests) {
}
