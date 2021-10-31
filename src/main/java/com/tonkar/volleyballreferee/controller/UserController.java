package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.dto.UserPasswordUpdate;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.FriendService;
import com.tonkar.volleyballreferee.service.SubscriptionService;
import com.tonkar.volleyballreferee.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final UserService         userService;
    private final SubscriptionService subscriptionService;
    private final FriendService       friendService;

    @PostMapping(value = "/users/{purchaseToken}", produces = {"application/json"})
    public ResponseEntity<Void> refreshSubscriptionPurchaseToken(@AuthenticationPrincipal User user, @PathVariable("purchaseToken") @NotBlank String purchaseToken) {
        if (user.isSubscription()) {
            subscriptionService.refreshSubscriptionPurchaseToken(purchaseToken);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping(value = "/users/password", produces = {"application/json"})
    public ResponseEntity<UserToken> updateUserPassword(@AuthenticationPrincipal User user, @Valid @NotNull @RequestBody UserPasswordUpdate userPasswordUpdate) {
        return new ResponseEntity<>(userService.updateUserPassword(user, userPasswordUpdate), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends/requested", produces = {"application/json"})
    public ResponseEntity<List<FriendRequest>> listFriendRequestsSentBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.listFriendRequestsSentBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends/received", produces = {"application/json"})
    public ResponseEntity<List<FriendRequest>> listFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.listFriendRequestsReceivedBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends/received/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.getNumberOfFriendRequestsReceivedBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends", produces = {"application/json"})
    public ResponseEntity<FriendsAndRequests> listFriendsAndRequests(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.listFriendsAndRequests(user), HttpStatus.OK);
    }

    @PostMapping(value = "/users/friends/request/{receiverPseudo}", produces = {"application/json"})
    public ResponseEntity<Void> sendFriendRequest(@AuthenticationPrincipal User user, @PathVariable("receiverPseudo") String receiverPseudo) {
        friendService.sendFriendRequest(user, receiverPseudo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/users/friends/accept/{id}", produces = {"application/json"})
    public ResponseEntity<Void> acceptFriendRequest(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        friendService.acceptFriendRequest(user, id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/users/friends/reject/{id}", produces = {"application/json"})
    public ResponseEntity<Void> rejectFriendRequest(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        friendService.rejectFriendRequest(user, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/users/friends/remove/{friendId}", produces = {"application/json"})
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal User user, @PathVariable("friendId") String friendId){
        friendService.removeFriend(user, friendId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
