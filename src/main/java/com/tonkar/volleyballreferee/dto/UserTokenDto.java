package com.tonkar.volleyballreferee.dto;

public record UserTokenDto(String token, long tokenExpiry, UserSummaryDto user) {}
