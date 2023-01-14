package com.tonkar.volleyballreferee.dto;

import com.tonkar.volleyballreferee.entity.GameType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
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
