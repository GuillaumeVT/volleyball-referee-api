package com.tonkar.volleyballreferee.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tonkar.volleyballreferee.entity.UserToken;
import com.tonkar.volleyballreferee.repository.UserTokenRepository;
import com.tonkar.volleyballreferee.security.FacebookIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    @Value("${vbr.auth.facebookAppAccessToken}")
    private String facebookAppAccessToken;

    @Value("${vbr.auth.googleWebClientId}")
    private String googleWebClientId;

    @Autowired
    private UserTokenRepository userTokenRepository;

    private final RestTemplate restTemplate;

    public UserAuthenticationServiceImpl() {
        restTemplate = new RestTemplate();
    }

    @Override
    public Optional<String> getUserId(AuthenticationProvider authProvider, String idToken) {
        Optional<String> optUserId;

        Optional<UserToken> optUserToken = userTokenRepository.findByToken(idToken);

        if (optUserToken.isPresent()) {
            optUserId = Optional.of(optUserToken.get().getId());
        } else {
            switch (authProvider) {
                case FACEBOOK:
                    optUserId = getUserIdWithFacebook(idToken);
                    break;
                case GOOGLE:
                    optUserId = getUserIdWithGoogle(idToken);
                    break;
                default:
                    optUserId = Optional.empty();
                    break;
            }
        }

        return optUserId;
    }

    private String computeUserId(String user, AuthenticationProvider authProvider) {
        return String.format("%s@%s", user, authProvider.toString().toLowerCase());
    }

    private Optional<String> getUserIdWithFacebook(String idToken) {
        Optional<String> optUserId;

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
                optUserId = Optional.empty();
            } else {
                String userId = computeUserId(facebookIdToken.getPayload().getUserId(), AuthenticationProvider.FACEBOOK);
                optUserId = Optional.of(userId);
                userTokenRepository.insert(new UserToken(userId, idToken));
            }
        } else {
            optUserId = Optional.empty();
        }

        return optUserId;
    }

    private Optional<String> getUserIdWithGoogle(String idToken) {
        Optional<String> optUserId;

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory()).setAudience(Collections.singletonList(googleWebClientId)).build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                optUserId = Optional.empty();
            } else {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                String userId = computeUserId(payload.getSubject(), AuthenticationProvider.GOOGLE);
                optUserId = Optional.of(userId);
                userTokenRepository.insert(new UserToken(userId, idToken));
            }
        } catch (GeneralSecurityException | IOException e) {
            optUserId = Optional.empty();
            log.error("Exception while reading google id token", e);
        }

        return optUserId;
    }


}
