package com.tonkar.volleyballreferee.dto;

import javax.validation.constraints.NotBlank;

public record UserPassword(
        @NotBlank
        String userPassword) {
}
