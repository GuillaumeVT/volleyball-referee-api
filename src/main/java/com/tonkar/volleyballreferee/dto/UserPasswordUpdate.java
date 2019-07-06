package com.tonkar.volleyballreferee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class UserPasswordUpdate {

    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;

}
