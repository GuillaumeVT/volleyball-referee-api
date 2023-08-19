package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUser(@NotBlank @Size(min = 36, max = 36) String id,
                      @NotBlank String pseudo,
                      @NotBlank @Email String email,
                      @NotBlank String password,
                      @NotBlank String purchaseToken) {
}
