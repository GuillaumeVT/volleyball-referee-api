package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class LeagueSummary {
    @NotNull
    private UUID     id;
    @NotBlank
    private String   createdBy;
    private long     createdAt;
    private long     updatedAt;
    @NotNull
    private GameType kind;
    @NotBlank
    private String   name;
}
