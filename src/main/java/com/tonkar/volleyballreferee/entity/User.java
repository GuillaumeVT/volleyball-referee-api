package com.tonkar.volleyballreferee.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor @Getter @Setter
@Document(collection="users")
public class User {

    @Id
    @NotBlank
    private String       id;
    @NotBlank
    private String       pseudo;
    @NotNull
    private List<Friend> friends;

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class Friend {

        @NotBlank
        private String id;
        @NotBlank
        private String pseudo;

    }

}
