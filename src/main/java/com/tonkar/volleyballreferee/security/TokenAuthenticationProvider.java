package com.tonkar.volleyballreferee.security;

import com.tonkar.volleyballreferee.service.UserAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
final class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationProvider.class);

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Object authProviderObj = authentication.getPrincipal();
        Object tokenObj = authentication.getCredentials();

        User user;

        if (authProviderObj == null || tokenObj == null) {
            LOGGER.error("Authentication provider or token are missing");
            throw new UsernameNotFoundException("Authentication provider or token are missing");
        } else {
            User.AuthenticationProvider authProvider = (User.AuthenticationProvider) authProviderObj;
            String token = String.valueOf(tokenObj);
            user = userAuthenticationService
                    .getUser(authProvider, token)
                    .orElseThrow(() -> new UsernameNotFoundException("Authentication provider or token are invalid"));
        }

        return user;
    }
}
