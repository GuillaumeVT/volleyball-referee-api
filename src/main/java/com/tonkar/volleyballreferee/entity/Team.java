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
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "teams")
public class Team {

    @Id
    @NotNull
    private UUID         id;
    @NotBlank
    private String       createdBy;
    private long         createdAt;
    private long         updatedAt;
    @NotBlank
    private String       name;
    @NotNull
    private GameType     kind;
    @NotNull
    private GenderType   gender;
    @NotBlank
    private String       color;
    @NotBlank
    private String       liberoColor;
    @NotNull
    private List<Player> players;
    @NotNull
    private List<Player> liberos;
    private int          captain;
    private String       coach;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Player {

        private int    num;
        private String name;

    }
}



