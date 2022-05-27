package com.tonkar.volleyballreferee.service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.dao.PasswordResetDao;
import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.dto.UserPasswordUpdate;
import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.PasswordReset;
import com.tonkar.volleyballreferee.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SubscriptionService subscriptionService;
    private final EmailService        emailService;
    private final UserDao             userDao;
    private final PasswordResetDao    passwordResetDao;
    private final PasswordEncoder     passwordEncoder = new BCryptPasswordEncoder(12);

    @Value("${vbr.web.domain}")
    private String webDomain;

    private Key signingKey;

    @Autowired
    public void initJwtSigningKey(@Value("${vbr.jwt.key}") String jwtKey) {
        signingKey = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public User getUser(String userId) {
        return userDao
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", userId)));
    }

    @Override
    public Optional<User> getUserFromToken(String token) {
        Optional<Claims> optionalClaims = parseToken(token);
        Optional<User> optionalUser;

        if (optionalClaims.isPresent()) {
            Claims claims = optionalClaims.get();
            if (claims.getExpiration().toInstant().isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                optionalUser = userDao.findById(claims.getSubject());
            } else {
                optionalUser = Optional.empty();
            }
        } else {
            optionalUser = Optional.empty();
        }

        return optionalUser;
    }

    @Override
    public UserSummary getUserFromPurchaseToken(String purchaseToken) {
        try {
            subscriptionService.refreshSubscriptionPurchaseToken(purchaseToken);
        } catch (ResponseStatusException e) {
            subscriptionService.validatePurchaseToken(purchaseToken);
        }

        return userDao
                .findUserByPurchaseToken(purchaseToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user for purchase token %s", purchaseToken)));
    }

    @Override
    public UserToken createUser(User user) {
        if (userDao.existsByPurchaseToken(user.getPurchaseToken())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Found an existing user with purchase token %s", user.getPurchaseToken()));
        }

        SubscriptionPurchase subscription = subscriptionService.validatePurchaseToken(user.getPurchaseToken());

        String password = user.getPassword().trim();
        validatePassword(password);

        Optional<User> optionalExistingUser = userDao.findByEmail(user.getEmail());

        if (optionalExistingUser.isPresent()) {
            User existingUser = optionalExistingUser.get();

            if (existingUser.isAccountNonExpired()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Found an existing user with email %s", user.getEmail()));
            } else {
                user.setId(existingUser.getId());
                user.setPseudo(existingUser.getPseudo());
                user.setFriends(existingUser.getFriends());
            }
        } else {
            boolean idExists = userDao.existsById(user.getId());
            boolean pseudoExists = userDao.existsByPseudo(user.getPseudo());

            if (idExists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Found an existing user with id %s", user.getId()));
            } else if (pseudoExists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Found an existing user with pseudo %s", user.getPseudo()));
            } else {
                user.setFriends(new ArrayList<>());
                user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            }
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setFailedAuthentication(new User.FailedAuthentication(0, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));
        user.setEnabled(true);
        user.setAdmin(false);
        user.setSubscription(subscription != null);
        user.setSubscriptionExpiryAt(subscription == null ? 0L : subscription.getExpiryTimeMillis());
        userDao.save(user);
        log.info(String.format("Created user with id %s and pseudo %s", user.getId(), user.getPseudo()));
        emailService.sendUserCreatedNotificationEmail(user);
        return signInUser(user.getEmail(), password);
    }

    @Override
    public UserToken signInUser(String userEmail, String userPassword) {
        userPassword = userPassword.trim();

        User user = userDao
                .findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", userEmail)));

        if (isLocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Access is locked for user %s", user.getId()));
        }

        if (user.isSubscription() && LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() > user.getSubscriptionExpiryAt()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format("Subscription is expired for user %s", user.getId()));
        }

        if (passwordEncoder.matches(userPassword, user.getPassword())) {
            UserToken userToken = buildToken(user);
            userSignedIn(user);
            return userToken;
        } else {
            addFailedAuthentication(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid password for user %s", userEmail));
        }
    }

    @Override
    public UserToken updateUserPassword(User user, UserPasswordUpdate userPasswordUpdate) {
        if (!passwordEncoder.matches(userPasswordUpdate.currentPassword(), user.getPassword())) {
            addFailedAuthentication(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("Invalid password for user %s", user.getId()));
        }

        String newPassword = userPasswordUpdate.newPassword().trim();
        updateUserPassword(newPassword, user);

        return signInUser(user.getEmail(), newPassword);
    }

    @Override
    public UUID initiatePasswordReset(String userEmail) {
        User user = userDao
                .findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find user %s", userEmail)));

        PasswordReset passwordReset = PasswordReset
                .builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30L))
                .build();
        passwordResetDao.save(passwordReset);
        emailService.sendPasswordResetEmail(user.getEmail(), passwordReset.getId());

        return passwordReset.getId();
    }

    @Override
    public String followPasswordReset(UUID passwordResetId) {
        PasswordReset passwordReset = passwordResetDao
                .findById(passwordResetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find password reset %s", passwordResetId)));

        getUser(passwordReset.getUserId());

        return String.format("%s/password-reset", webDomain);
    }

    @Override
    public UserToken resetPassword(UUID passwordResetId, String userPassword) {
        userPassword = userPassword.trim();

        PasswordReset passwordReset = passwordResetDao
                .findById(passwordResetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Could not find password reset %s", passwordResetId)));

        User user = getUser(passwordReset.getUserId());

        updateUserPassword(userPassword, user);

        passwordResetDao.delete(passwordReset);

        return signInUser(user.getEmail(), userPassword);
    }

    @Override
    public void purgeOldPasswordResets(int days) {
        passwordResetDao.deleteByExpiresAtBefore(LocalDateTime.now(ZoneOffset.UTC).minusDays(days));
    }

    @Override
    public void deleteUser(User user) {
        user.getFriends().forEach(friend -> userDao.removeFriend(friend.getId(), user.getId()));
        userDao.delete(user);
    }

    private UserToken buildToken(User user) {
        LocalDateTime iat = LocalDateTime.now();
        LocalDateTime exp = iat.plusMonths(3L);

        String token = Jwts
                .builder()
                .setIssuer("com.tonkar.volleyballreferee")
                .setSubject(user.getId())
                .setIssuedAt(Date.from(iat.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(exp.toInstant(ZoneOffset.UTC)))
                .signWith(signingKey)
                .compact();

        return new UserToken(
                token,
                Date.from(exp.toInstant(ZoneOffset.UTC)).getTime(),
                new UserSummary(user.getId(), user.getPseudo(), user.getEmail(), user.isAdmin())
        );
    }

    private Optional<Claims> parseToken(String token) {
        Optional<Claims> optionalClaims;

        try {
            optionalClaims = Optional.of(Jwts
                    .parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody());
        } catch (JwtException e) {
            log.error(String.format("Failed to parse token %s", token));
            optionalClaims = Optional.empty();
        }

        return optionalClaims;
    }

    private boolean isLocked(User user) {
        return user.getFailedAuthentication().getAttempts() >= 5
                && LocalDateTime.now(ZoneOffset.UTC).isBefore(LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getFailedAuthentication().getResetsAt()), ZoneOffset.UTC));
    }

    private void userSignedIn(User user) {
        userDao.updateUserSignedIn(user.getId(), LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    private void addFailedAuthentication(User user) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime willResetAt = now.plusMinutes(20L);
        User.FailedAuthentication failedAuthentication = user.getFailedAuthentication();
        if (now.isAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getFailedAuthentication().getResetsAt()), ZoneOffset.UTC))) {
            // Reset attempts
            failedAuthentication = new User.FailedAuthentication(0, now.toInstant(ZoneOffset.UTC).toEpochMilli());
        }

        failedAuthentication.setAttempts(user.getFailedAuthentication().getAttempts() == 5 ? 5 : user.getFailedAuthentication().getAttempts() + 1);
        failedAuthentication.setResetsAt(willResetAt.toInstant(ZoneOffset.UTC).toEpochMilli());
        userDao.addFailedAuthentication(user.getId(), failedAuthentication);
    }

    private void updateUserPassword(String password, User user) {
        if (passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Provided password is the same as the previous");
        }
        validatePassword(password);
        userDao.updateUserPassword(user.getId(), passwordEncoder.encode(password));
        emailService.sendPasswordUpdatedNotificationEmail(user);
    }

    private void validatePassword(String password) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Password must contain at least %d uppercase characters", 1));
        }

        if (numberOfSpecialCharacters < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Password must contain at least %d special characters", 1));
        }

        if (maxNumberORepeatedCharacters > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Password must contain at most %d repeating characters", 3));
        }
    }
}
