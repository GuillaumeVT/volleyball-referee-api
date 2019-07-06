package com.tonkar.volleyballreferee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class EmailCredentials {

    @NotBlank
    @Email
    private String userEmail;
    @NotBlank
    private String userPassword;

}
