package com.tonkar.volleyballreferee.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    @NotNull
    private UUID                 id;
    @NotBlank
    private String               pseudo;
    @NotBlank
    private String               password;
    private List<Friend>         friends;
    private long                 createdAt;
    private long                 lastLoginAt;
    private FailedAuthentication failedAuthentication;
    private boolean              enabled;
    private boolean              admin;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return getPseudo();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    public Optional<Friend> getFriend(UUID friendId) {
        return friends.stream().filter(f -> f.getId().equals(friendId)).findFirst();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldNameConstants
    public static class Friend {
        @NotNull
        private UUID   id;
        @NotBlank
        private String pseudo;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldNameConstants
    public static class FailedAuthentication {
        private int  attempts;
        private long resetsAt;
    }
}
