package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.*;

import java.util.List;

public record FriendsAndRequests(List<User.Friend> friends, List<FriendRequest> receivedFriendRequests,
                                 List<FriendRequest> sentFriendRequests) {}
