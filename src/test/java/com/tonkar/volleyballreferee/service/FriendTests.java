package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FriendTests extends VbrMockedTests {

    private final FriendService friendService;

    public FriendTests(@Autowired FriendService friendService) {
        super();
        this.friendService = friendService;
    }

    @Test
    void test_friends_and_requests() {
        // GIVEN
        UserToken userToken = sandbox.createUser();
        UserToken userToken2 = sandbox.createUser();
        UserToken userToken3 = sandbox.createUser();
        UserToken userToken4 = sandbox.createUser();
        sandbox.addFriend(sandbox.getUser(userToken.user().id()), sandbox.getUser(userToken2.user().id()));
        friendService.sendFriendRequest(sandbox.getUser(userToken.user().id()), userToken3.user().pseudo());
        friendService.sendFriendRequest(sandbox.getUser(userToken4.user().id()), userToken.user().pseudo());

        // WHEN
        FriendsAndRequests friendsAndRequests = friendService.listFriendsAndRequests(sandbox.getUser(userToken.user().id()));

        // THEN
        assertEquals(1, friendsAndRequests.friends().size());
        assertEquals(userToken2.user().pseudo(), friendsAndRequests.friends().getFirst().getPseudo());
        assertEquals(1, friendsAndRequests.sentFriendRequests().size());
        assertEquals(userToken3.user().pseudo(), friendsAndRequests.sentFriendRequests().getFirst().getReceiverPseudo());
        assertEquals(userToken.user().pseudo(), friendsAndRequests.sentFriendRequests().getFirst().getSenderPseudo());
        assertEquals(1, friendsAndRequests.receivedFriendRequests().size());
        assertEquals(userToken.user().pseudo(), friendsAndRequests.receivedFriendRequests().getFirst().getReceiverPseudo());
        assertEquals(userToken4.user().pseudo(), friendsAndRequests.receivedFriendRequests().getFirst().getSenderPseudo());
    }
}
