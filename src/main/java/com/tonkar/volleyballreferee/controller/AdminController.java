package com.tonkar.volleyballreferee.controller;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("isAdmin()")
    @GetMapping(value = "/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserSummaryDto>> listUsers(@RequestParam(value = "filter", required = false) String filter,
                                                          @RequestParam("page") @Min(0) int page,
                                                          @RequestParam("size") @Min(20) @Max(200) int size) {
        return new ResponseEntity<>(adminService.listUsers(filter, PageRequest.of(page, size)), HttpStatus.OK);
    }

    @PreAuthorize("isAdmin()")
    @PostMapping(value = "/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserSummaryDto> createUser(@RequestBody @Valid @NotNull NewUserDto newUser) {
        return new ResponseEntity<>(adminService.createUser(newUser), HttpStatus.CREATED);
    }

    @PreAuthorize("isAdmin()")
    @DeleteMapping(value = "/admin/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        adminService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("isAdmin()")
    @PostMapping(value = "/admin/users/{userId}/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserPassword(@PathVariable("userId") UUID userId,
                                                   @RequestBody @Valid @NotNull UserPasswordDto userPassword) {
        adminService.updateUserPassword(userId, userPassword.userPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
