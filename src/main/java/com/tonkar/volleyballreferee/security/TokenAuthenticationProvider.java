package com.tonkar.volleyballreferee.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public final class TokenAuthenticationProvider implements AuthenticationProvider {

    private final AuthService authService;
    private final UserService userService;

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final Object token = authentication.getCredentials();

        final String tokenString = String.valueOf(token);
        final DecodedJWT decodedJwt;

        try {
            decodedJwt = authService.verifyToken(tokenString);
        } catch (ResponseStatusException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Unable to validate token");
        }

        final UUID userId = UUID.fromString(decodedJwt.getSubject());
        final User user;

        try {
            user = userService.getUser(userId);
        } catch (ResponseStatusException e) {
            log.error(e.getMessage());
            throw new UsernameNotFoundException("Unable to find user");
        }

        return UsernamePasswordAuthenticationToken.authenticated(user, token, user.getAuthorities());
    }
}