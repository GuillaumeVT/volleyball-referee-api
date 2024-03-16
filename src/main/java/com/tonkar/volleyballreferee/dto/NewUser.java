package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.*;

public record NewUser(@NotBlank @Size(min = 36, max = 36) String id, @NotBlank String pseudo, @NotBlank @Email String email,
                      @NotBlank String password, @NotBlank String purchaseToken) {}
