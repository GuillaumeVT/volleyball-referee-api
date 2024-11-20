package com.tonkar.volleyballreferee.service;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;

@Service
public class AuthService {

    @Getter
    private final PasswordEncoder passwordEncoder;
    private final Algorithm       signingKeyAlgorithm;
    private final String          issuer;
    private final Duration        tokenValidity;

    public AuthService(@Value("${vbr.jwt.key}") String jwtKey,
                       @Value("${vbr.jwt.issuer}") String issuer,
                       @Value("${vbr.jwt.token-validity-days}") int tokenValidityDays) {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.signingKeyAlgorithm = Algorithm.HMAC256(jwtKey.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.tokenValidity = Duration.ofDays(tokenValidityDays);
    }

    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(signingKeyAlgorithm).withIssuer(issuer).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    public UserTokenDto generateToken(User user) {
        ZonedDateTime iat = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime exp = iat.plus(tokenValidity);

        String token = JWT
                .create()
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withIssuedAt(iat.toInstant())
                .withExpiresAt(exp.toInstant())
                .sign(signingKeyAlgorithm);

        return new UserTokenDto(token, Date.from(exp.toInstant()).getTime(),
                                new UserSummaryDto(user.getId(), user.getPseudo(), user.isAdmin()));
    }

    public void validatePassword(String password) {
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Password must contain at least %d characters", 8));
        }

        int numberOfDigits = 0;
        int numberOfUppercaseCharacters = 0;
        int numberOfSpecialCharacters = 0;
        int numberORepeatedCharacters = 0;
        int maxNumberORepeatedCharacters = 0;
        char previousCharacter = ' ';

        for (int index = 0; index < password.length(); index++) {
            char character = password.charAt(index);

            if (Character.isDigit(character)) {
                numberOfDigits++;
            } else if (Character.isUpperCase(character)) {
                numberOfUppercaseCharacters++;
            } else if (!Character.isLetter(character) && !Character.isWhitespace(character)) {
                numberOfSpecialCharacters++;
            }

            if (previousCharacter == character) {
                numberORepeatedCharacters++;
            } else {
                numberORepeatedCharacters = 1;
            }

            maxNumberORepeatedCharacters = Math.max(maxNumberORepeatedCharacters, numberORepeatedCharacters);
            previousCharacter = character;
        }

        if (numberOfDigits < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Password must contain at least %d digits", 1));
        }

        if (numberOfUppercaseCharacters < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              String.format("Password must contain at least %d uppercase characters", 1));
        }

        if (numberOfSpecialCharacters < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              String.format("Password must contain at least %d special characters", 1));
        }

        if (maxNumberORepeatedCharacters > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              String.format("Password must contain at most %d repeating characters", 3));
        }
    }
}
