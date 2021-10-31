package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.VbrMockedTests;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.dto.UserToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FriendTests extends VbrMockedTests {

    @Test
    public void test_friends_and_requests(@Autowired FriendService friendService) {
        // GIVEN
        UserToken userToken = createUser();
        UserToken userToken2 = createUser();
        UserToken userToken3 = createUser();
        UserToken userToken4 = createUser();
        addFriend(getUser(userToken.getUser().getId()), getUser(userToken2.getUser().getId()));
        friendService.sendFriendRequest(getUser(userToken.getUser().getId()), userToken3.getUser().getPseudo());
        friendService.sendFriendRequest(getUser(userToken4.getUser().getId()), userToken.getUser().getPseudo());

        // WHEN
        FriendsAndRequests friendsAndRequests = friendService.listFriendsAndRequests(getUser(userToken.getUser().getId()));

        // THEN
        assertEquals(1, friendsAndRequests.getFriends().size());
        assertEquals(userToken2.getUser().getPseudo(), friendsAndRequests.getFriends().get(0).getPseudo());
        assertEquals(1, friendsAndRequests.getSentFriendRequests().size());
        assertEquals(userToken3.getUser().getPseudo(), friendsAndRequests.getSentFriendRequests().get(0).getReceiverPseudo());
        assertEquals(userToken.getUser().getPseudo(), friendsAndRequests.getSentFriendRequests().get(0).getSenderPseudo());
        assertEquals(1, friendsAndRequests.getReceivedFriendRequests().size());
        assertEquals(userToken.getUser().getPseudo(), friendsAndRequests.getReceivedFriendRequests().get(0).getReceiverPseudo());
        assertEquals(userToken4.getUser().getPseudo(), friendsAndRequests.getReceivedFriendRequests().get(0).getSenderPseudo());
    }
}
