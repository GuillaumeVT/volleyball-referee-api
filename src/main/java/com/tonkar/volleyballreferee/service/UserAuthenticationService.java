package com.tonkar.volleyballreferee.service;

import java.util.Optional;

public interface UserAuthenticationService {

    Optional<String> getUserId(AuthenticationProvider authProvider, String idToken);

}
