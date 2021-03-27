package com.tonkar.volleyballreferee.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.tonkar.volleyballreferee.dao.FriendRequestDao;
import com.tonkar.volleyballreferee.dao.PasswordResetDao;
import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.PasswordReset;
import com.tonkar.volleyballreferee.entity.User;
import com.tonkar.volleyballreferee.exception.*;
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
    private UserDao userDao;

    @Autowired
    private FriendRequestDao friendRequestDao;

    @Autowired
    private PasswordResetDao passwordResetDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${vbr.web.domain}")
    private String webDomain;

    @Value("${vbr.android.app.packageName}")
    private String androidPackageName;

    @Value("${vbr.android.app.billing.sku-purchase}")
    private String androidPurchaseSku;

    @Value("${vbr.android.app.billing.sku-subscription}")
    private String androidSubscriptionSku;

    @Value("${vbr.android.app.billing.credential}")
    private String androidCredential;

    @Value("${vbr.jwt.key}")
    private String jwtKey;

    @Override
    public User getUser(String userId) throws NotFoundException {
        return userDao
                .findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userId)));
    }

    private User getUserByPseudo(String pseudo) throws NotFoundException {
        return userDao
                .findByPseudo(pseudo)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", pseudo)));
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
    public UserSummary getUserFromPurchaseToken(String purchaseToken) throws NotFoundException {
        refreshSubscriptionPurchaseToken(purchaseToken);
        return userDao
                .findUserByPurchaseToken(purchaseToken)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user for purchase token %s", purchaseToken)));
    }

    @Override
    public void refreshSubscriptionPurchaseToken(String purchaseToken) {
        try (InputStream stream = Files.newInputStream(Paths.get(androidCredential))) {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleCredential credential = GoogleCredential.fromStream(stream).createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
            AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName(androidPackageName).build();
            AndroidPublisher.Purchases.Subscriptions subscriptions = publisher.purchases().subscriptions();

            // A subscription  may contain a linked purchase token which is the previous purchase token for this user.
            // Browse this chain of purchase tokens to find the user and when found, update their purchase token and expiry
            Optional<UserSummary> optionalUser = userDao.findUserByPurchaseToken(purchaseToken);
            SubscriptionPurchase subscription = subscriptions.get(androidPackageName, androidSubscriptionSku, purchaseToken).execute();
            final long subscriptionExpiryAt;

            if (subscription.getAutoRenewing() && (subscription.getPaymentState() == 0)) {
                subscriptionExpiryAt = subscription.getExpiryTimeMillis() + 604800000L; // Users have auto renew and the payment is pending, give 7 extra days of access
            } else {
                subscriptionExpiryAt = subscription.getExpiryTimeMillis();
            }

            String linkedPurchaseToken = purchaseToken;

            while (optionalUser.isEmpty() && linkedPurchaseToken != null) {
                SubscriptionPurchase linkedSubscription = subscriptions.get(androidPackageName, androidSubscriptionSku, linkedPurchaseToken).execute();
                optionalUser = userDao.findUserByPurchaseToken(linkedPurchaseToken);
                linkedPurchaseToken = linkedSubscription.getLinkedPurchaseToken();
            }

            // Store the latest purchase token and the subscription expiry date
            optionalUser.ifPresent(userSummary -> {
                log.info(String.format("Found the user %s from the linked purchase tokens, store new token %s with expiry %d", userSummary.getId(), purchaseToken, subscriptionExpiryAt));
                userDao.updateSubscriptionPurchaseToken(userSummary.getId(), purchaseToken, subscriptionExpiryAt);
            });

        } catch (IOException | GeneralSecurityException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public UserToken createUser(User user) throws UnauthorizedException, ForbiddenException, ConflictException, NotFoundException, BadRequestException {
        if (userDao.existsByPurchaseToken(user.getPurchaseToken())) {
            throw new ConflictException(String.format("Found an existing user with purchase token %s", user.getPurchaseToken()));
        }

        SubscriptionPurchase subscription = getSubscriptionPurchase(user.getPurchaseToken());

        if (subscription == null) {
            if (!isValidPurchaseToken(user.getPurchaseToken())) {
                throw new ForbiddenException(String.format("User with email %s provided an invalid purchase token %s", user.getEmail(), user.getPurchaseToken()));
            }
        }

        String password = user.getPassword().trim();
        validatePassword(password);

        Optional<User> optionalExistingUser = userDao.findByEmail(user.getEmail());

        if (optionalExistingUser.isPresent()) {
            User existingUser = optionalExistingUser.get();

            if (existingUser.isAccountNonExpired()) {
                throw new ConflictException(String.format("Found an existing user with email %s", user.getEmail()));
            } else {
                user.setId(existingUser.getId());
                user.setPseudo(existingUser.getPseudo());
                user.setFriends(existingUser.getFriends());
            }
        } else {
            boolean idExists = userDao.existsById(user.getId());
            boolean pseudoExists = userDao.existsByPseudo(user.getPseudo());

            if (idExists) {
                throw new ConflictException(String.format("Found an existing user with id %s", user.getId()));
            } else if (pseudoExists) {
                throw new ConflictException(String.format("Found an existing user with pseudo %s", user.getPseudo()));
            } else {
                user.setFriends(new ArrayList<>());
                user.setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
            }
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setFailedAuthentication(new User.FailedAuthentication(0, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));
        user.setEnabled(true);
        user.setSubscription(subscription != null);
        user.setSubscriptionExpiryAt(subscription == null ? 0L : subscription.getExpiryTimeMillis());
        userDao.save(user);
        log.info(String.format("Created user with id %s and pseudo %s", user.getId(), user.getPseudo()));
        emailService.sendUserCreatedNotificationEmail(user);
        return signInUser(user.getEmail(), password);
    }

    @Override
    public UserToken signInUser(String userEmail, String userPassword) throws NotFoundException, UnauthorizedException, ForbiddenException {
        userPassword = userPassword.trim();

        User user = userDao
                .findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userEmail)));

        if (isLocked(user)) {
            throw new ForbiddenException(String.format("Access is locked for user %s", user.getId()));
        }

        if (user.isSubscription() && LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() > user.getSubscriptionExpiryAt()) {
            throw new ForbiddenException(String.format("Subscription is expired for user %s", user.getId()));
        }

        if (passwordEncoder.matches(userPassword, user.getPassword())) {
            UserToken userToken = buildToken(user);
            userSignedIn(user);
            return userToken;
        } else {
            addFailedAuthentication(user);
            throw new UnauthorizedException(String.format("Invalid password for user %s", userEmail));
        }
    }

    @Override
    public Count getNumberOfFriendRequestsReceivedBy(User user) {
        return new Count(friendRequestDao.countByReceiverId(user.getId()));
    }

    @Override
    public List<FriendRequest> listFriendRequestsSentBy(User user) {
        return friendRequestDao.findBySenderId(user.getId());
    }

    @Override
    public List<FriendRequest> listFriendRequestsReceivedBy(User user) {
        return friendRequestDao.findByReceiverId(user.getId());
    }

    @Override
    public FriendsAndRequests listFriendsAndRequests(User user) {
        FriendsAndRequests friendsAndRequests = new FriendsAndRequests();

        friendsAndRequests.setFriends(user.getFriends());
        friendsAndRequests.setReceivedFriendRequests(friendRequestDao.findByReceiverId(user.getId()));
        friendsAndRequests.setSentFriendRequests(friendRequestDao.findBySenderId(user.getId()));

        return friendsAndRequests;
    }

    @Override
    public void sendFriendRequest(User user, String receiverPseudo) throws ConflictException, NotFoundException {
        User receiverUser = getUserByPseudo(receiverPseudo);

        if (user.getId().equals(receiverUser.getId())) {
            throw new ConflictException(String.format("%s cannot be friend with himself", user.getId()));
        } else if (userDao.areFriends(user.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("%s and %s are already friends", user.getId(), receiverUser.getId()));
        } else if (friendRequestDao.existsBySenderIdAndReceiverId(user.getId(), receiverUser.getId())) {
            throw new ConflictException(String.format("Found an existing friend request from %s to %s", user.getId(), receiverUser.getId()));
        } else {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setId(UUID.randomUUID());
            friendRequest.setSenderId(user.getId());
            friendRequest.setSenderPseudo(user.getPseudo());
            friendRequest.setReceiverId(receiverUser.getId());
            friendRequest.setReceiverPseudo(receiverUser.getPseudo());
            friendRequestDao.save(friendRequest);
            emailService.sendFriendRequestEmail(user, receiverUser);
        }
    }

    @Override
    public void acceptFriendRequest(User user, UUID friendRequestId) throws ConflictException, NotFoundException {
        FriendRequest friendRequest = friendRequestDao
                .findByIdAndReceiverId(friendRequestId, user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Could not find friend request %s with receiver %s", friendRequestId, user.getId())));

        User senderUser = getUser(friendRequest.getSenderId());
        User receiverUser = getUser(friendRequest.getReceiverId());

        if (userDao.areFriends(friendRequest.getSenderId(), friendRequest.getReceiverId())) {
            throw new ConflictException(String.format("%s and %s are already friends", senderUser.getId(), receiverUser.getId()));
        } else {
            userDao.addFriend(senderUser.getId(), new User.Friend(receiverUser.getId(), receiverUser.getPseudo()));
            userDao.addFriend(receiverUser.getId(), new User.Friend(senderUser.getId(), senderUser.getPseudo()));
            log.info(String.format("%s and %s are now friends", senderUser.getId(), receiverUser.getId()));
            emailService.sendAcceptFriendRequestEmail(receiverUser, senderUser);
        }

        friendRequestDao.deleteById(friendRequest);
    }

    @Override
    public void rejectFriendRequest(User user, UUID friendRequestId) {
        friendRequestDao.deleteByIdAndReceiverId(friendRequestId, user.getId());
    }

    @Override
    public void removeFriend(User user, String friendId) throws NotFoundException {
        if (userDao.areFriends(user.getId(), friendId)) {
            userDao.removeFriend(user.getId(), friendId);
            userDao.removeFriend(friendId, user.getId());
            log.info(String.format("%s and %s are no longer friends", user.getId(), friendId));
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
        User user = userDao
                .findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find user %s", userEmail)));

        PasswordReset passwordReset = PasswordReset
                .builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(30L))
                .build();
        passwordResetDao.save(passwordReset);
        emailService.sendPasswordResetEmail(user.getEmail(), passwordReset.getId());
    }

    @Override
    public String followPasswordReset(UUID passwordResetId) throws NotFoundException {
        PasswordReset passwordReset = passwordResetDao
                .findById(passwordResetId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find password reset %s", passwordResetId)));

        getUser(passwordReset.getUserId());

        return String.format("%s/password-reset", webDomain);
    }

    @Override
    public UserToken resetPassword(UUID passwordResetId, String userPassword) throws BadRequestException, ConflictException, NotFoundException, UnauthorizedException, ForbiddenException {
        userPassword = userPassword.trim();

        PasswordReset passwordReset = passwordResetDao
                .findById(passwordResetId)
                .orElseThrow(() -> new NotFoundException(String.format("Could not find password reset %s", passwordResetId)));

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

    private SubscriptionPurchase getSubscriptionPurchase(String purchaseToken) {
        SubscriptionPurchase subscription = null;

        try (InputStream stream = Files.newInputStream(Paths.get(androidCredential))) {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleCredential credential = GoogleCredential.fromStream(stream).createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
            AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName(androidPackageName).build();
            subscription = publisher.purchases().subscriptions().get(androidPackageName, androidSubscriptionSku, purchaseToken).execute();
        } catch (IOException | GeneralSecurityException e) {
            log.error(e.getMessage());
        }

        return subscription;
    }

    private boolean isValidPurchaseToken(String purchaseToken) {
        boolean valid = false;

        try (InputStream stream = Files.newInputStream(Paths.get(androidCredential))) {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleCredential credential = GoogleCredential.fromStream(stream).createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
            AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential).setApplicationName(androidPackageName).build();
            publisher.purchases().products().get(androidPackageName, androidPurchaseSku, purchaseToken).execute();
            valid = true;
        } catch (IOException | GeneralSecurityException e) {
            log.error(e.getMessage());
        }

        return valid;
    }

    private UserToken buildToken(User user) {
        LocalDateTime iat = LocalDateTime.now();
        LocalDateTime exp = iat.plusMonths(3L);

        String token = Jwts
                .builder()
                .setIssuer(androidPackageName)
                .setSubject(user.getId())
                .setIssuedAt(Date.from(iat.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(exp.toInstant(ZoneOffset.UTC)))
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();

        return UserToken
                .builder()
                .user(new UserSummary(user.getId(), user.getPseudo(), user.getEmail()))
                .token(token)
                .tokenExpiry(Date.from(exp.toInstant(ZoneOffset.UTC)).getTime())
                .build();
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

    private void updateUserPassword(String password, User user) throws BadRequestException, ConflictException {
        if (passwordEncoder.matches(password, user.getPassword())) {
            throw new ConflictException("Provided password is the same as the previous");
        }
        validatePassword(password);
        userDao.updateUserPassword(user.getId(), passwordEncoder.encode(password));
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
