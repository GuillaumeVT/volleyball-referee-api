package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.entity.PasswordReset;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository extends MongoRepository<PasswordReset, UUID> {

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    // Tests only
    Optional<PasswordReset> findByUserId(String userId);
}
