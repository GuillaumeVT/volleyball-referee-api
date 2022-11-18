package com.tonkar.volleyballreferee.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@Getter
@Setter
@FieldNameConstants
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    @NotBlank
    private String               id;
    @NotBlank
    private String               pseudo;
    @Email
    @NotBlank
    private String               email;
    @NotBlank
    private String               password;
    @NotBlank
    private String               purchaseToken;
    private boolean              subscription;
    private long                 subscriptionExpiryAt;
    private List<Friend>         friends;
    private long                 createdAt;
    private long                 lastLoginAt;
    private FailedAuthentication failedAuthentication;
    private boolean              enabled;
    private boolean              admin;

    public static String VBR_USER_ID = "01022018@vbr";

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(admin ? "ROLE_ADMIN" : "ROLE_USER"));
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return getPseudo();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return !subscription || LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() < subscriptionExpiryAt;
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
    public Optional<Friend> getFriend(String friendId) {
        return friends
                .stream()
                .filter(f -> f.getId().equals(friendId))
                .findFirst();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldNameConstants
    public static class Friend {
        @NotBlank
        private String id;
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
