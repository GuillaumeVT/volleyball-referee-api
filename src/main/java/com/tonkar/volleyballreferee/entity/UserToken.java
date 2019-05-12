package com.tonkar.volleyballreferee.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
@Document(collection="userTokens")
public class UserToken {

    @Id
    @NotBlank
    private String id;
    @NotBlank
    private String token;

}
