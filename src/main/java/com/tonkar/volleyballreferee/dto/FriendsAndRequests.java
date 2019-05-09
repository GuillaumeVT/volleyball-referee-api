package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class FriendsAndRequests {

    private List<User.Friend>   friends;
    private List<FriendRequest> receivedFriendRequests;
    private List<FriendRequest> sentFriendRequests;

    public FriendsAndRequests() {
        this.friends = new ArrayList<>();
        this.receivedFriendRequests = new ArrayList<>();
        this.sentFriendRequests = new ArrayList<>();
    }
}
