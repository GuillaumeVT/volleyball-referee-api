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
public class RulesSummary {
    @NotNull
    private UUID     id;
    @NotBlank
    private String   createdBy;
    private long     createdAt;
    private long     updatedAt;
    @NotBlank
    private String   name;
    @NotNull
    private GameType kind;
}
