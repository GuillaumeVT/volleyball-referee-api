package com.tonkar.volleyballreferee.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor @Getter @Setter
@Document(collection="users")
public class User implements UserDetails {

    @Id
    @NotBlank
    private String       id;
    @NotBlank
    private String       pseudo;
    @NotNull
    private List<Friend> friends;

    public static String VBR_USER_ID = "01022018@vbr";

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return getPseudo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Optional<Friend> getFriend(String friendId) {
        return friends
                .stream()
                .filter(f -> f.getId().equals(friendId))
                .findFirst();
    }

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class Friend {

        @NotBlank
        private String id;
        @NotBlank
        private String pseudo;

    }

}
