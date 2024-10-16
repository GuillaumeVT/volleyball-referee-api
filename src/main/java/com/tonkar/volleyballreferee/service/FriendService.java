package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.*;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class FriendService {

    private final UserDao          userDao;
    private final FriendRequestDao friendRequestDao;

    public Count getNumberOfFriendRequestsReceivedBy(User user) {
        return new Count(friendRequestDao.countByReceiverId(user.getId()));
    }

    public List<FriendRequest> listFriendRequestsSentBy(User user) {
        return friendRequestDao.findBySenderId(user.getId());
    }

    public List<FriendRequest> listFriendRequestsReceivedBy(User user) {
        return friendRequestDao.findByReceiverId(user.getId());
    }

    public FriendsAndRequests listFriendsAndRequests(User user) {
        return new FriendsAndRequests(user.getFriends(), friendRequestDao.findByReceiverId(user.getId()),
                                      friendRequestDao.findBySenderId(user.getId()));
    }

    public UUID sendFriendRequest(User user, String receiverPseudo) {
        User receiverUser = getUserByPseudo(receiverPseudo);

        if (user.getId().equals(receiverUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("%s cannot be friend with himself", user.getId()));
        } else if (userDao.areFriends(user.getId(), receiverUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("%s and %s are already friends", user.getId(), receiverUser.getId()));
        } else if (friendRequestDao.existsBySenderIdAndReceiverId(user.getId(), receiverUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("Found an existing friend request from %s to %s", user.getId(),
                                                            receiverUser.getId()));
        } else {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setId(UUID.randomUUID());
            friendRequest.setSenderId(user.getId());
            friendRequest.setSenderPseudo(user.getPseudo());
            friendRequest.setReceiverId(receiverUser.getId());
            friendRequest.setReceiverPseudo(receiverUser.getPseudo());
            friendRequestDao.save(friendRequest);

            return friendRequest.getId();
        }
    }

    private User getUser(UUID userId) {
        return userDao
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", userId)));
    }

    public void acceptFriendRequest(User user, UUID friendRequestId) {
        FriendRequest friendRequest = friendRequestDao
                .findByIdAndReceiverId(friendRequestId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               String.format("Could not find friend request %s with receiver %s",
                                                                             friendRequestId, user.getId())));

        User senderUser = getUser(friendRequest.getSenderId());
        User receiverUser = getUser(friendRequest.getReceiverId());

        if (userDao.areFriends(friendRequest.getSenderId(), friendRequest.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              String.format("%s and %s are already friends", senderUser.getId(), receiverUser.getId()));
        } else {
            userDao.addFriend(senderUser.getId(), new User.Friend(receiverUser.getId(), receiverUser.getPseudo()));
            userDao.addFriend(receiverUser.getId(), new User.Friend(senderUser.getId(), senderUser.getPseudo()));
            log.info("{} and {} are now friends", senderUser.getId(), receiverUser.getId());
        }

        friendRequestDao.deleteById(friendRequest);
    }

    public void rejectFriendRequest(User user, UUID friendRequestId) {
        friendRequestDao.deleteByIdAndReceiverId(friendRequestId, user.getId());
    }

    public void removeFriend(User user, UUID friendId) {
        if (userDao.areFriends(user.getId(), friendId)) {
            userDao.removeFriend(user.getId(), friendId);
            userDao.removeFriend(friendId, user.getId());
            log.info("{} and {} are no longer friends", user.getId(), friendId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("%s and %s are not friends", user.getId(), friendId));
        }
    }

    private User getUserByPseudo(String pseudo) {
        return userDao
                .findByPseudo(pseudo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", pseudo)));
    }
}
