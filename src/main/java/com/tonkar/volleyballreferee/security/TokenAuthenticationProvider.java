package com.tonkar.volleyballreferee.security;

import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
final class TokenAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final Object token = authentication.getCredentials();

        User userAuthentication = Optional
                .ofNullable(token)
                .map(String::valueOf)
                .flatMap(userService::getUserFromToken)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Could not find the application with authentication token %s", token)));

        return UsernamePasswordAuthenticationToken.authenticated(userAuthentication, token, userAuthentication.getAuthorities());
    }
}