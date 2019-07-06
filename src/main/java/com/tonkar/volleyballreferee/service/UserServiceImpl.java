package com.tonkar.volleyballreferee.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.PasswordReset;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.*;
import com.tonkar.volleyballreferee.repository.FriendRequestRepository;
import com.tonkar.volleyballreferee.repository.PasswordResetRepository;
import com.tonkar.volleyballreferee.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${vbr.web.domain}")
    private String webDomain;

    @Value("${vbr.android.app.packageName}")
    private String androidPackageName;

    @Value("${vbr.android.app.purchase.sku}")
    private String androidSku;

    @Value("${vbr.android.app.purchase.credential}")
    private String androidCredential;

    @Value("${vbr.jwt.key}")
    private String jwtKey;

    @Override
    public User getUser(String userId) throws NotFoundException {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userId)));
    }

    private User getUserByPseudo(String pseudo) throws NotFoundException {
        return userRepository
                .findByPseudo(pseudo)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", pseudo)));
    }

    @Override
    public UserToken createUser(User user) throws UnauthorizedException, ForbiddenException, ConflictException, NotFoundException, BadRequestException {
        if (userRepository.existsByPurchaseToken(user.getPurchaseToken())) {
            throw new ConflictException(String.format("Found an existing user with purchase token %s", user.getPurchaseToken()));
        }

        if (!isValidPurchaseToken(user.getPurchaseToken())) {
            throw new ForbiddenException(String.format("User with email %s provided an invalid purchase token %s", user.getEmail(), user.getPurchaseToken()));
        }

        String password = user.getPassword().trim();
        validatePassword(password);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException(String.format("Found an existing user with email %s", user.getEmail()));
        }

        boolean idExists = userRepository.existsById(user.getId());
        boolean pseudoExists = userRepository.existsByPseudo(user.getPseudo());

        // Temporarily until everyone has migrated
        if (idExists && pseudoExists && (user.getId().endsWith("@google") || user.getId().endsWith("@facebook"))) {
            User savedUser = getUser(user.getId());
            savedUser.setEmail(user.getEmail());
            savedUser.setPassword(passwordEncoder.encode(password));
            savedUser.setPurchaseToken(user.getPurchaseToken());
            savedUser.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            savedUser.setFailedAuthentication(new User.FailedAuthentication(0, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));
            userRepository.save(savedUser);
            log.info(String.format("Updated user with id %s and pseudo %s", savedUser.getId(), savedUser.getPseudo()));
            emailService.sendUserCreatedNotificationEmail(savedUser);
            return signInUser(savedUser.getEmail(), password);
        } else if (idExists) {
            throw new ConflictException(String.format("Found an existing user with id %s", user.getId()));
        } else if (pseudoExists) {
            throw new ConflictException(String.format("Found an existing user with pseudo %s", user.getPseudo()));
        } else {
            user.setPassword(passwordEncoder.encode(password));
            user.setFriends(new ArrayList<>());
            user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            user.setFailedAuthentication(new User.FailedAuthentication(0, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));
            userRepository.save(user);
            log.info(String.format("Created user with id %s and pseudo %s", user.getId(), user.getPseudo()));
            emailService.sendUserCreatedNotificationEmail(user);
            return signInUser(user.getEmail(), password);
        }
    }

    @Override
    public UserToken signInUser(String userEmail, String userPassword) throws NotFoundException, UnauthorizedException, ForbiddenException {
        userPassword = userPassword.trim();

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userEmail)));

        if (isLocked(user)) {
            throw new ForbiddenException(String.format("Access is locked for user %s", user.getId()));
        }

        if (passwordEncoder.matches(userPassword, user.getPassword())) {
            String token = buildToken(user);
            userSignedIn(user);
            return UserToken
                    .builder()
                    .user(new UserSummary(user.getId(), user.getPseudo(), user.getEmail()))
                    .token(token)
                    .build();
        } else {
            addFailedAuthentication(user);
            throw new UnauthorizedException(String.format("Invalid password for user %s", userEmail));
        }
    }

    @Override
    public Optional<User> getUserFromToken(String token) {
        Optional<Claims> optionalClaims = parseToken(token);
        Optional<User> optionalUser;

        if (optionalClaims.isPresent()) {
            Claims claims = optionalClaims.get();
            if (claims.getExpiration().toInstant().isAfter(LocalDateTime.now().toInstant(ZoneOffset.UTC))) {
                optionalUser = userRepository.findById(claims.getSubject());
            } else {
                optionalUser = Optional.empty();
            }
        } else {
            optionalUser = Optional.empty();
        }

        return optionalUser;
    }

    @Override
    public Count getNumberOfFriendRequestsReceivedBy(User user) {
        return new Count(friendRequestRepository.countByReceiverId(user.getId()));
    }

    @Override
    public List<FriendRequest> listFriendRequestsSentBy(User user) {
        return friendRequestRepository.findBySenderId(user.getId());
    }

    @Override
    public List<FriendRequest> listFriendRequestsReceivedBy(User user) {
        return friendRequestRepository.findByReceiverId(user.getId());
    }

    @Override
    public FriendsAndRequests listFriendsAndRequests(User user) {
        FriendsAndRequests friendsAndRequests = new FriendsAndRequests();

        friendsAndRequests.setFriends(user.getFriends());
        friendsAndRequests.setReceivedFriendRequests(friendRequestRepository.findByReceiverId(user.getId()));
        friendsAndRequests.setSentFriendRequests(friendRequestRepository.findBySenderId(user.getId()));

        return friendsAndRequests;
    }

    @Override
    public void sendFriendRequest(User user, String receiverPseudo) throws ConflictException, NotFoundException {
        User receiverUser = getUserByPseudo(receiverPseudo);

        if (user.getId().equals(receiverUser.getId())) {
            throw new ConflictException(String.format("%s cannot be friend with himself", user.getId()));
        } else if (userRepository.areFriends(user.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("%s and %s are already friends", user.getId(), receiverUser.getId()));
        } else if (friendRequestRepository.existsBySenderIdAndReceiverId(user.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("Found an existing friend request from %s to %s", user.getId(), receiverUser.getId()));
        } else {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setId(UUID.randomUUID());
            friendRequest.setSenderId(user.getId());
            friendRequest.setSenderPseudo(user.getPseudo());
            friendRequest.setReceiverId(receiverUser.getId());
            friendRequest.setReceiverPseudo(receiverUser.getPseudo());
            friendRequestRepository.save(friendRequest);
        }
    }

    @Override
    public void acceptFriendRequest(User user, UUID friendRequestId) throws ConflictException, NotFoundException {
        FriendRequest friendRequest = friendRequestRepository
                .findByIdAndReceiverId(friendRequestId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find friend request %s with receiver %s", friendRequestId, user.getId())));

        User senderUser = getUser(friendRequest.getSenderId());
        User receiverUser = getUser(friendRequest.getReceiverId());

        if (userRepository.areFriends(senderUser.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("%s and %s are already friends", senderUser.getId(), receiverUser.getId()));
        } else {
            senderUser.getFriends().add(new User.Friend(receiverUser.getId(), receiverUser.getPseudo()));
            receiverUser.getFriends().add(new User.Friend(senderUser.getId(), senderUser.getPseudo()));

            userRepository.save(senderUser);
            userRepository.save(receiverUser);
            log.info(String.format("%s and %s are now friends", senderUser.getId(), receiverUser.getId()));
        }

        friendRequestRepository.delete(friendRequest);
    }

    @Override
    public void rejectFriendRequest(User user, UUID friendRequestId) {
        friendRequestRepository.deleteByIdAndReceiverId(friendRequestId, user.getId());
    }

    @Override
    public void removeFriend(User user, String friendId) throws NotFoundException {
        if (userRepository.areFriends(user.getId(), friendId)) {
            User friendUser = getUser(friendId);

            user.getFriends().removeIf(friend -> friend.getId().equals(friendUser.getId()));
            friendUser.getFriends().removeIf(friend -> friend.getId().equals(user.getId()));

            userRepository.save(user);
            userRepository.save(friendUser);
            log.info(String.format("%s and %s are no longer friends", user.getId(), friendUser.getId()));
        } else {
            throw new NotFoundException(String.format("%s and %s are not friends", user.getId(), friendId));
        }
    }

    @Override
    public UserToken updateUserPassword(User user, UserPasswordUpdate userPasswordUpdate) throws BadRequestException, ConflictException, NotFoundException, UnauthorizedException, ForbiddenException {
        if (!passwordEncoder.matches(userPasswordUpdate.getCurrentPassword(), user.getPassword())) {
            addFailedAuthentication(user);
            throw new UnauthorizedException(String.format("Invalid password for user %s", user.getId()));
        }

        String newPassword = userPasswordUpdate.getNewPassword().trim();
        updateUserPassword(newPassword, user);

        return signInUser(user.getEmail(), newPassword);
    }

    @Override
    public void initiatePasswordReset(String userEmail) throws NotFoundException {
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userEmail)));

        PasswordReset passwordReset = PasswordReset
                .builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30L))
                .build();
        passwordResetRepository.save(passwordReset);
        emailService.sendPasswordResetEmail(user.getEmail(), passwordReset.getId());
    }

    @Override
    public String followPasswordReset(UUID passwordResetId) throws NotFoundException {
        PasswordReset passwordReset = passwordResetRepository
                .findById(passwordResetId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find password reset %s", passwordResetId)));

        getUser(passwordReset.getUserId());

        return String.format("%s/password-reset", webDomain);
    }

    @Override
    public UserToken resetPassword(UUID passwordResetId, String userPassword) throws BadRequestException, ConflictException, NotFoundException, UnauthorizedException, ForbiddenException {
        userPassword = userPassword.trim();

        PasswordReset passwordReset = passwordResetRepository
                .findById(passwordResetId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find password reset %s", passwordResetId)));

        User user = getUser(passwordReset.getUserId());

        updateUserPassword(userPassword, user);

        passwordResetRepository.deleteById(passwordReset.getId());

        return signInUser(user.getEmail(), userPassword);
    }

    @Override
    public void purgeOldPasswordResets(int days) {
        passwordResetRepository.deleteByExpiresAtBefore(LocalDateTime.now(ZoneOffset.UTC).minusDays(days));
    }

    private boolean isValidPurchaseToken(String purchaseToken) {
        boolean valid = false;

        try (InputStream stream = Files.newInputStream(Paths.get(androidCredential))) {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleCredential credential = GoogleCredential.fromStream(stream).createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
            AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName(androidPackageName).build();
            publisher.purchases().products().get(androidPackageName, androidSku, purchaseToken).execute();
            valid = true;
        } catch (IOException | GeneralSecurityException e) {
            log.error(e.getMessage());
        }

        return valid;
    }

    private String buildToken(User user) {
        return Jwts
                .builder()
                .setIssuer(androidPackageName)
                .setSubject(user.getId())
                .setIssuedAt(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(LocalDateTime.now().plusMonths(3L).toInstant(ZoneOffset.UTC)))
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();
    }

    private Optional<Claims> parseToken(String token) {
        Optional<Claims> optionalClaims;

        try {
            optionalClaims = Optional.of(Jwts
                    .parser()
                    .setSigningKey(jwtKey)
                    .parseClaimsJws(token)
                    .getBody());
        } catch (JwtException e) {
            log.error(String.format("Failed to parse token %s", token), e);
            optionalClaims = Optional.empty();
        }

        return optionalClaims;
    }

    private boolean isLocked(User user) {
        return user.getFailedAuthentication().getAttempts() >= 5
                && LocalDateTime.now(ZoneOffset.UTC).isBefore(LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getFailedAuthentication().getResetsAt()), ZoneOffset.UTC));
    }

    private void userSignedIn(User user) {
        user.setLastLoginAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        user.getFailedAuthentication().setAttempts(0);
        userRepository.save(user);
    }

    private void addFailedAuthentication(User user) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime willResetAt = now.plusMinutes(30L);
        if (now.isAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getFailedAuthentication().getResetsAt()), ZoneOffset.UTC))) {
            // Reset attempts
            user.setFailedAuthentication(new User.FailedAuthentication(0, now.toInstant(ZoneOffset.UTC).toEpochMilli()));
        }
        user.getFailedAuthentication().setAttempts(user.getFailedAuthentication().getAttempts() == 5 ? 5 : user.getFailedAuthentication().getAttempts() + 1);
        user.getFailedAuthentication().setResetsAt(willResetAt.toInstant(ZoneOffset.UTC).toEpochMilli());
        userRepository.save(user);
    }

    private void updateUserPassword(String password, User user) throws BadRequestException, ConflictException {
        if (passwordEncoder.matches(password, user.getPassword())) {
            throw new ConflictException("Provided password is the same as the previous");
        }
        validatePassword(password);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        emailService.sendPasswordUpdatedNotificationEmail(user);
    }

   private void validatePassword(String password) throws BadRequestException {
        if (password.length() < 8) {
            throw new BadRequestException(String.format("Password must contain at least %d characters", 8));
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
            throw new BadRequestException(String.format("Password must contain at least %d digits", 1));
        }

        if (numberOfUppercaseCharacters < 1) {
            throw new BadRequestException(String.format("Password must contain at least %d uppercase characters", 1));
        }

        if (numberOfSpecialCharacters < 1) {
            throw new BadRequestException(String.format("Password must contain at least %d special characters", 1));
        }

        if (maxNumberORepeatedCharacters > 3) {
            throw new BadRequestException(String.format("Password must contain at most %d repeating characters", 3));
        }
    }
}
