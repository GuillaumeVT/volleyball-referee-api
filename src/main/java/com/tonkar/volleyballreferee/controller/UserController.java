package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final UserService   userService;
    private final FriendService friendService;

    @PatchMapping(value = "/users/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserTokenDto> updateUserPassword(@AuthenticationPrincipal User user,
                                                           @Valid @NotNull @RequestBody UserPasswordUpdateDto userPasswordUpdate) {
        return new ResponseEntity<>(userService.updateUserPassword(user, userPasswordUpdate), HttpStatus.OK);
    }

    @PatchMapping(value = "/users/pseudo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserSummaryDto> updateUserPseudo(@AuthenticationPrincipal User user,
                                                           @Valid @NotNull @RequestBody UserPseudoDto userPseudo) {
        return new ResponseEntity<>(userService.updateUserPseudo(user, userPseudo.userPseudo()), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends/requested", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FriendRequest>> listFriendRequestsSentBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.listFriendRequestsSentBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends/received", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FriendRequest>> listFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.listFriendRequestsReceivedBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends/received/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CountDto> getNumberOfFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.getNumberOfFriendRequestsReceivedBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/users/friends", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FriendsAndRequestsDto> listFriendsAndRequests(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(friendService.listFriendsAndRequests(user), HttpStatus.OK);
    }

    @PostMapping(value = "/users/friends/request/{receiverPseudo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> sendFriendRequest(@AuthenticationPrincipal User user,
                                                  @PathVariable("receiverPseudo") String receiverPseudo) {
        friendService.sendFriendRequest(user, receiverPseudo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/users/friends/accept/{friendRequestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> acceptFriendRequest(@AuthenticationPrincipal User user,
                                                    @PathVariable("friendRequestId") UUID friendRequestId) {
        friendService.acceptFriendRequest(user, friendRequestId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/users/friends/reject/{friendRequestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rejectFriendRequest(@AuthenticationPrincipal User user,
                                                    @PathVariable("friendRequestId") UUID friendRequestId) {
        friendService.rejectFriendRequest(user, friendRequestId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/users/friends/remove/{friendId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal User user, @PathVariable("friendId") UUID friendId) {
        friendService.removeFriend(user, friendId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
