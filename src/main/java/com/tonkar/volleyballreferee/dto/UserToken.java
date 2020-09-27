package com.tonkar.volleyballreferee.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserToken {
    private String      token;
    private long        tokenExpiry;
    private UserSummary user;
}
