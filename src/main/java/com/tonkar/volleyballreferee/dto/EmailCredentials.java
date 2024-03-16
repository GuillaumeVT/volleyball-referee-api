package com.tonkar.volleyballreferee.dto;

import jakarta.validation.constraints.*;

public record EmailCredentials(@NotBlank @Email String userEmail, @NotBlank String userPassword) {}
