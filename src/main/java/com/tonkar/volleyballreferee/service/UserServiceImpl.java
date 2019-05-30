package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.repository.FriendRequestRepository;
import com.tonkar.volleyballreferee.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Override
    public User getUser(String userId) throws NotFoundException {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userId)));
    }

    private User getUserByPseudo(String pseudo) throws NotFoundException {
        return userRepository
                .findByPseudo(pseudo)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", pseudo)));
    }

    @Override
    public void createUser(User user) throws ConflictException {
        if (userRepository.existsById(user.getId())) {
            throw new ConflictException(String.format("Found an existing user with id %s", user.getId()));
        } else if (userRepository.existsByPseudo(user.getPseudo())) {
            throw new ConflictException(String.format("Found an existing user with pseudo %s", user.getPseudo()));
        }  else {
            user.setFriends(new ArrayList<>());
            userRepository.save(user);
            log.info(String.format("Created user with id %s and pseudo %s", user.getId(), user.getPseudo()));
        }
    }

    @Override
    public void deleteUser(User user) {
        userRepository.findByFriend(user.getId()).forEach(u -> {
            u.getFriends().removeIf(friend -> friend.getId().equals(user.getId()));
            userRepository.save(user);
        });
        userRepository.deleteById(user.getId());
        log.info(String.format("Deleted user with id %s", user.getId()));
    }

    @Override
    public Count getNumberOfFriendRequestsReceivedBy(User user) {
        return new Count(friendRequestRepository.countByReceiverId(user.getId()));
    }

    @Override
    public List<FriendRequest> listFriendRequestsSentBy(User user) {
        return friendRequestRepository.findBySenderId(user.getId());
    }

    @Override
    public List<FriendRequest> listFriendRequestsReceivedBy(User user) {
        return friendRequestRepository.findByReceiverId(user.getId());
    }

    @Override
    public FriendsAndRequests listFriendsAndRequests(User user) {
        FriendsAndRequests friendsAndRequests = new FriendsAndRequests();

        friendsAndRequests.setFriends(user.getFriends());
        friendsAndRequests.setReceivedFriendRequests(friendRequestRepository.findByReceiverId(user.getId()));
        friendsAndRequests.setSentFriendRequests(friendRequestRepository.findBySenderId(user.getId()));

        return friendsAndRequests;
    }

    @Override
    public void sendFriendRequest(User user, String receiverPseudo) throws ConflictException, NotFoundException {
        User receiverUser = getUserByPseudo(receiverPseudo);

        if (user.getId().equals(receiverUser.getId())) {
            throw new ConflictException(String.format("%s cannot be friend with himself", user.getId()));
        } else if (userRepository.areFriends(user.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("%s and %s are already friends", user.getId(), receiverUser.getId()));
        } else if (friendRequestRepository.existsBySenderIdAndReceiverId(user.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("Found an existing friend request from %s to %s", user.getId(), receiverUser.getId()));
        } else {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setId(UUID.randomUUID());
            friendRequest.setSenderId(user.getId());
            friendRequest.setSenderPseudo(user.getPseudo());
            friendRequest.setReceiverId(receiverUser.getId());
            friendRequest.setReceiverPseudo(receiverUser.getPseudo());
            friendRequestRepository.save(friendRequest);
        }
    }

    @Override
    public void acceptFriendRequest(User user, UUID friendRequestId) throws ConflictException, NotFoundException {
        FriendRequest friendRequest = friendRequestRepository
                .findByIdAndReceiverId(friendRequestId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find friend request %s with receiver %s", friendRequestId, user.getId())));

        User senderUser = getUser(friendRequest.getSenderId());
        User receiverUser = getUser(friendRequest.getReceiverId());

        if (userRepository.areFriends(senderUser.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("%s and %s are already friends", senderUser.getId(), receiverUser.getId()));
        } else {
            senderUser.getFriends().add(new User.Friend(receiverUser.getId(), receiverUser.getPseudo()));
            receiverUser.getFriends().add(new User.Friend(senderUser.getId(), senderUser.getPseudo()));

            userRepository.save(senderUser);
            userRepository.save(receiverUser);
            log.info(String.format("%s and %s are now friends", senderUser.getId(), receiverUser.getId()));
        }

        friendRequestRepository.delete(friendRequest);
    }

    @Override
    public void rejectFriendRequest(User user, UUID friendRequestId) {
        friendRequestRepository.deleteByIdAndReceiverId(friendRequestId, user.getId());
    }

    @Override
    public void removeFriend(User user, String friendId) throws NotFoundException {
        if (userRepository.areFriends(user.getId(), friendId)) {
            User friendUser = getUser(friendId);

            user.getFriends().removeIf(friend -> friend.getId().equals(friendUser.getId()));
            friendUser.getFriends().removeIf(friend -> friend.getId().equals(user.getId()));

            userRepository.save(user);
            userRepository.save(friendUser);
            log.info(String.format("%s and %s are no longer friends", user.getId(), friendUser.getId()));
        } else {
            throw new NotFoundException(String.format("%s and %s are not friends", user.getId(), friendId));
        }
    }
}
