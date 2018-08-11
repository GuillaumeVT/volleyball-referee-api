package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.security.User;

import java.util.Optional;

public interface UserAuthenticationService {

    Optional<User> getUser(User.AuthenticationProvider authProvider, String idToken);

}
