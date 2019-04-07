package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.Count;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.exception.ConflictException;
import com.tonkar.volleyballreferee.exception.NotFoundException;
import com.tonkar.volleyballreferee.security.User;
import com.tonkar.volleyballreferee.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v3/users")
@CrossOrigin("*")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(value = "", produces = {"application/json"})
    public ResponseEntity<com.tonkar.volleyballreferee.entity.User> getUser(@AuthenticationPrincipal User user) {
        try {
            return new ResponseEntity<>(userService.getUser(user.getUserId()), HttpStatus.OK);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/{pseudo}", produces = {"application/json"})
    public ResponseEntity<String> createUser(@AuthenticationPrincipal User user, @PathVariable("pseudo") String pseudo) {
        try {
            userService.createUser(user.getUserId(), pseudo);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ConflictException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "", produces = {"application/json"})
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal User user) {
        userService.deleteUser(user.getUserId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/friends/requested", produces = {"application/json"})
    public ResponseEntity<List<FriendRequest>> listFriendRequestsSentBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.listFriendRequestsSentBy(user.getUserId()), HttpStatus.OK);
    }

    @GetMapping(value = "/friends/received", produces = {"application/json"})
    public ResponseEntity<List<FriendRequest>> listFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.listFriendRequestsReceivedBy(user.getUserId()), HttpStatus.OK);
    }

    @GetMapping(value = "/friends/received/count", produces = {"application/json"})
    public ResponseEntity<Count> getNumberOfFriendRequestsReceivedBy(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(userService.getNumberOfFriendRequestsReceivedBy(user.getUserId()), HttpStatus.OK);
    }

    @PostMapping(value = "/friends/request/{receiverPseudo}", produces = {"application/json"})
    public ResponseEntity<String> sendFriendRequest(@AuthenticationPrincipal User user, @PathVariable("receiverPseudo") String receiverPseudo) {
        try {
            userService.sendFriendRequest(user.getUserId(), receiverPseudo);
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
            userService.acceptFriendRequest(user.getUserId(), id);
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
        userService.rejectFriendRequest(user.getUserId(), id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/friends/remove/{friendId}", produces = {"application/json"})
    public ResponseEntity<String> removeFriend(@AuthenticationPrincipal User user, @PathVariable("friendId") String friendId) {
        try {
            userService.removeFriend(user.getUserId(), friendId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
