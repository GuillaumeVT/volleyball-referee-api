package com.tonkar.volleyballreferee.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tonkar.volleyballreferee.security.FacebookIdToken;
import com.tonkar.volleyballreferee.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationServiceImpl.class);

    @Value("${vbr.auth.facebookAppAccessToken}")
    private String facebookAppAccessToken;

    @Value("${vbr.auth.googleWebClientId}")
    private String googleWebClientId;

    private final RestTemplate restTemplate;

    public UserAuthenticationServiceImpl() {
        restTemplate = new RestTemplate();
    }

    @Override
    public Optional<User> getUser(User.AuthenticationProvider authProvider, String idToken) {
        Optional<User> optUser;

        switch (authProvider) {
            case FACEBOOK:
                optUser = getFacebookUser(idToken);
                break;
            case GOOGLE:
                optUser = getGoogleUser(idToken);
                break;
            default:
                optUser = Optional.empty();
                break;
        }

        return optUser;
    }

    private Optional<User> getFacebookUser(String idToken) {
        Optional<User> optUser;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v3.2/debug_token")
                .queryParam("input_token", idToken)
                .queryParam("access_token", facebookAppAccessToken);
        ResponseEntity<FacebookIdToken> response = restTemplate.getForEntity(builder.toUriString(), FacebookIdToken.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            FacebookIdToken facebookIdToken = response.getBody();

            if (facebookIdToken == null
                    || facebookIdToken.getPayload() == null
                    || !facebookIdToken.getPayload().isValid()
                    || !facebookAppAccessToken.startsWith(facebookIdToken.getPayload().getAppId())) {
                optUser = Optional.empty();
            } else {
                User user = new User(facebookIdToken.getPayload().getUserId(), User.AuthenticationProvider.FACEBOOK);
                optUser = Optional.of(user);
            }
        } else {
            optUser = Optional.empty();
        }

        return optUser;
    }

    private Optional<User> getGoogleUser(String idToken) {
        Optional<User> optUser;

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory()).setAudience(Collections.singletonList(googleWebClientId)).build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                optUser = Optional.empty();
            } else {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                User user = new User(payload.getSubject(), User.AuthenticationProvider.GOOGLE);
                optUser = Optional.of(user);
            }
        } catch (GeneralSecurityException | IOException e) {
            optUser = Optional.empty();
            LOGGER.error("Exception while reading google id token", e);
        }

        return optUser;
    }


}
