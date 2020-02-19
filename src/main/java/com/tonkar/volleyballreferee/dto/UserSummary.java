package com.tonkar.volleyballreferee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class UserSummary {

    @NotBlank
    private String id;
    @NotBlank
    private String pseudo;
    @Email
    private String email;

}
