package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.dto.FriendsAndRequests;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.dto.UserPasswordUpdate;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.*;
import com.tonkar.volleyballreferee.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3.1/users")
@CrossOrigin("*")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/password", produces = {"application/json"})
    public ResponseEntity<UserToken> updateUserPassword(@AuthenticationPrincipal User user, @Valid @RequestBody UserPasswordUpdate userPasswordUpdate) {
        try {
            return new ResponseEntity<>(userService.updateUserPassword(user, userPasswordUpdate), HttpStatus.OK);
        } catch (UnauthorizedException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (ForbiddenException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (BadRequestException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/friends/requested", produces = {"application/json"})
    public ResponseEntity<List<FriendRequest>> listFriendRequestsSentBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.listFriendRequestsSentBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/friends/received", produces = {"application/json"})
    public ResponseEntity<List<FriendRequest>> listFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.listFriendRequestsReceivedBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/friends/received/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.getNumberOfFriendRequestsReceivedBy(user), HttpStatus.OK);
    }

    @GetMapping(value = "/friends", produces = {"application/json"})
    public ResponseEntity<FriendsAndRequests> listFriendsAndRequests(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.listFriendsAndRequests(user), HttpStatus.OK);
    }

    @PostMapping(value = "/friends/request/{receiverPseudo}", produces = {"application/json"})
    public ResponseEntity<String> sendFriendRequest(@AuthenticationPrincipal User user, @PathVariable("receiverPseudo") String receiverPseudo) {
        try {
            userService.sendFriendRequest(user, receiverPseudo);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/friends/accept/{id}", produces = {"application/json"})
    public ResponseEntity<String> acceptFriendRequest(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        try {
            userService.acceptFriendRequest(user, id);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/friends/reject/{id}", produces = {"application/json"})
    public ResponseEntity<String> rejectFriendRequest(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        userService.rejectFriendRequest(user, id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/friends/remove/{friendId}", produces = {"application/json"})
    public ResponseEntity<String> removeFriend(@AuthenticationPrincipal User user, @PathVariable("friendId") String friendId) {
        try {
            userService.removeFriend(user, friendId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
