package com.tonkar.volleyballreferee.controller;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.AdminService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Secured("ADMIN")
    @GetMapping(value = "/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<User>> listUsers(@RequestParam(value = "filter", required = false) String filter,
                                                @RequestParam("page") @Min(0) int page,
                                                @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(adminService.listUsers(filter, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @Secured("ADMIN")
    @GetMapping(value = "/admin/users/{userId}/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionPurchase> getUserSubscription(@PathVariable("userId") String userId) {
        return new ResponseEntity<>(adminService.getUserSubscription(userId), HttpStatus.OK);
    }

    @Secured("ADMIN")
    @PostMapping(value = "/admin/users/{userId}/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> refreshUserSubscription(@PathVariable("userId") String userId) {
        adminService.refreshUserSubscription(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Secured("ADMIN")
    @PostMapping(value = "/admin/users/{userId}/subscription/{purchaseToken}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserSubscription(@PathVariable("userId") String userId,
                                                       @PathVariable("purchaseToken") @NotBlank String purchaseToken) {
        adminService.updateUserSubscription(userId, purchaseToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
