package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.FriendRequestDao;
import com.tonkar.volleyballreferee.dto.FriendsAndRequestsDto;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FriendTests extends VbrServiceTests {

    private final FriendService    friendService;
    private final FriendRequestDao friendRequestDao;
    private final UserService      userService;

    public FriendTests(@Autowired FriendService friendService,
                       @Autowired FriendRequestDao friendRequestDao,
                       @Autowired UserService userService) {
        super();
        this.friendService = friendService;
        this.friendRequestDao = friendRequestDao;
        this.userService = userService;
    }

    @Test
    void test_friends_and_requests() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        var user3 = sandbox.createAndGetUser();
        var user4 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);
        friendService.sendFriendRequest(user1, user3.getPseudo());
        friendService.sendFriendRequest(user4, user1.getPseudo());

        // WHEN
        FriendsAndRequestsDto friendsAndRequests = friendService.listFriendsAndRequests(user1);

        // THEN
        assertEquals(1, friendsAndRequests.friends().size());
        assertEquals(user2.getPseudo(), friendsAndRequests.friends().getFirst().getPseudo());
        assertEquals(1, friendsAndRequests.sentFriendRequests().size());
        assertEquals(user3.getPseudo(), friendsAndRequests.sentFriendRequests().getFirst().getReceiverPseudo());
        assertEquals(user1.getPseudo(), friendsAndRequests.sentFriendRequests().getFirst().getSenderPseudo());
        assertEquals(1, friendsAndRequests.receivedFriendRequests().size());
        assertEquals(user1.getPseudo(), friendsAndRequests.receivedFriendRequests().getFirst().getReceiverPseudo());
        assertEquals(user4.getPseudo(), friendsAndRequests.receivedFriendRequests().getFirst().getSenderPseudo());
    }

    @Test
    void test_friends_request() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();

        // WHEN
        UUID friendRequestId = friendService.sendFriendRequest(user1, user2.getPseudo());

        // THEN
        Assertions.assertNotNull(friendRequestId);
        Assertions.assertTrue(friendRequestDao.existsBySenderIdAndReceiverId(user1.getId(), user2.getId()));
    }

    @Test
    void test_friends_request_conflict() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> friendService.sendFriendRequest(user1, user1.getPseudo()));
    }

    @Test
    void test_friends_request_conflict2() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> friendService.sendFriendRequest(user1, user2.getPseudo()));
    }

    @Test
    void test_friends_request_received_count() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        var count = friendService.getNumberOfFriendRequestsReceivedBy(user1);

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(0L, count.count());
    }

    @Test
    void test_friends_request_received_count2() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        var count = friendService.getNumberOfFriendRequestsReceivedBy(user2);

        // THEN
        Assertions.assertNotNull(count);
        Assertions.assertEquals(1L, count.count());
    }

    @Test
    void test_friends_request_received_list() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        List<FriendRequest> friendRequests = friendService.listFriendRequestsReceivedBy(user1);

        // THEN
        Assertions.assertNotNull(friendRequests);
        Assertions.assertTrue(friendRequests.isEmpty());
    }

    @Test
    void test_friends_request_received_list2() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        List<FriendRequest> friendRequests = friendService.listFriendRequestsReceivedBy(user2);

        // THEN
        Assertions.assertNotNull(friendRequests);
        Assertions.assertEquals(1, friendRequests.size());
        Assertions.assertEquals(user1.getId(), friendRequests.getFirst().getSenderId());
        Assertions.assertEquals(user1.getPseudo(), friendRequests.getFirst().getSenderPseudo());
        Assertions.assertEquals(user2.getId(), friendRequests.getFirst().getReceiverId());
        Assertions.assertEquals(user2.getPseudo(), friendRequests.getFirst().getReceiverPseudo());
    }

    @Test
    void test_friends_request_sent_list() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        List<FriendRequest> friendRequests = friendService.listFriendRequestsSentBy(user2);

        // THEN
        Assertions.assertNotNull(friendRequests);
        Assertions.assertTrue(friendRequests.isEmpty());
    }

    @Test
    void test_friends_request_sent_list2() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        List<FriendRequest> friendRequests = friendService.listFriendRequestsSentBy(user1);

        // THEN
        Assertions.assertNotNull(friendRequests);
        Assertions.assertEquals(1, friendRequests.size());
        Assertions.assertEquals(user1.getId(), friendRequests.getFirst().getSenderId());
        Assertions.assertEquals(user1.getPseudo(), friendRequests.getFirst().getSenderPseudo());
        Assertions.assertEquals(user2.getId(), friendRequests.getFirst().getReceiverId());
        Assertions.assertEquals(user2.getPseudo(), friendRequests.getFirst().getReceiverPseudo());
    }

    @Test
    void test_friends_reject() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        var friendRequestId = friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        friendService.rejectFriendRequest(user2, friendRequestId);

        // THEN
        Assertions.assertTrue(friendService.listFriendRequestsSentBy(user1).isEmpty());
        Assertions.assertTrue(friendService.listFriendRequestsReceivedBy(user2).isEmpty());
    }

    @Test
    void test_friends_accept() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        var friendRequestId = friendService.sendFriendRequest(user1, user2.getPseudo());

        // WHEN
        friendService.acceptFriendRequest(user2, friendRequestId);

        // THEN
        Assertions.assertTrue(friendService.listFriendRequestsSentBy(user1).isEmpty());
        Assertions.assertTrue(friendService.listFriendRequestsReceivedBy(user2).isEmpty());
        Assertions.assertEquals(1, userService.getUser(user1.getId()).getFriends().size());
        Assertions.assertEquals(1, userService.getUser(user2.getId()).getFriends().size());
        Assertions.assertEquals(user2.getId(), userService.getUser(user1.getId()).getFriends().getFirst().getId());
        Assertions.assertEquals(user1.getId(), userService.getUser(user2.getId()).getFriends().getFirst().getId());
    }

    @Test
    void test_friends_remove() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();
        sandbox.addFriend(user1, user2);

        // WHEN
        friendService.removeFriend(user2, user1.getId());

        // THEN
        Assertions.assertTrue(userService.getUser(user1.getId()).getFriends().isEmpty());
        Assertions.assertTrue(userService.getUser(user2.getId()).getFriends().isEmpty());
    }

    @Test
    void test_friends_remove_notFound() {
        // GIVEN
        var user1 = sandbox.createAndGetUser();
        var user2 = sandbox.createAndGetUser();

        // WHEN / THEN
        Assertions.assertThrows(ResponseStatusException.class, () -> friendService.removeFriend(user2, user1.getId()));
    }
}
