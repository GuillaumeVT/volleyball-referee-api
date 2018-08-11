package com.tonkar.volleyballreferee.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class User implements UserDetails {

    public enum AuthenticationProvider {
        FACEBOOK, GOOGLE
    }

    private String                 id;
    private AuthenticationProvider authProvider;

    public User(String id, AuthenticationProvider authProvider) {
        this.id = id;
        this.authProvider = authProvider;
    }

    public String getUserId() {
        return String.format("%s@%s", id, authProvider.toString().toLowerCase());
    }

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
        return getUserId();
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

    public static String VBR_USER_ID = "01022018@vbr";
}
