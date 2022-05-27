package com.tonkar.volleyballreferee.dto;

public record UserToken(
        String token,
        long tokenExpiry,
        UserSummary user) {
}
