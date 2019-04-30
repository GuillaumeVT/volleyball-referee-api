package com.tonkar.volleyballreferee.security;

import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.repository.UserRepository;
import com.tonkar.volleyballreferee.service.AuthenticationProvider;
import com.tonkar.volleyballreferee.service.UserAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
final class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Object authProviderObj = authentication.getPrincipal();
        Object tokenObj = authentication.getCredentials();

        User user;

        if (authProviderObj == null || tokenObj == null) {
            log.error("Authentication provider or token are missing");
            throw new UsernameNotFoundException("Authentication provider or token are missing");
        } else {
            AuthenticationProvider authProvider = (AuthenticationProvider) authProviderObj;
            String token = String.valueOf(tokenObj);
            user = userAuthenticationService
                    .getUserId(authProvider, token)
                    .flatMap(userRepository::findById)
                    .orElseThrow(() -> new UsernameNotFoundException("Failed to authenticate or to find user"));
        }

        return user;
    }
}
