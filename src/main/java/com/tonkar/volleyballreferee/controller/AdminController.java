package com.tonkar.volleyballreferee.controller;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping(value = "/admin/users", produces = {"application/json"})
    public ResponseEntity<Page<User>> listUsers(@AuthenticationPrincipal User user,
                                                @RequestParam(value = "filter", required = false) String filter,
                                                @RequestParam("page") @Min(0) int page,
                                                @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(adminService.listUsers(filter, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping(value = "/admin/users/{userId}/subscription", produces = {"application/json"})
    public ResponseEntity<SubscriptionPurchase> getUserSubscription(@AuthenticationPrincipal User user,
                                                                    @PathVariable("userId") String userId) {
        return new ResponseEntity<>(adminService.getUserSubscription(userId), HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping(value = "/admin/users/{userId}/subscription", produces = {"application/json"})
    public ResponseEntity<Void> refreshUserSubscription(@AuthenticationPrincipal User user,
                                                        @PathVariable("userId") String userId) {
        adminService.refreshUserSubscription(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping(value = "/admin/users/{userId}/subscription/{purchaseToken}", produces = {"application/json"})
    public ResponseEntity<Void> updateUserSubscription(@AuthenticationPrincipal User user,
                                                       @PathVariable("userId") String userId,
                                                       @PathVariable("purchaseToken") @NotBlank String purchaseToken) {
        adminService.updateUserSubscription(userId, purchaseToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
