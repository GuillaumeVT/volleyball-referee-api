package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.entity.FriendRequest;
import com.tonkar.volleyballreferee.entity.PasswordReset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PasswordResetDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(PasswordReset passwordReset) {
        mongoTemplate.save(passwordReset);
    }

    public void delete(PasswordReset passwordReset) {
        mongoTemplate.remove(passwordReset);
    }

    public void deleteByExpiresAtBefore(LocalDateTime dateTime) {
        Query query = Query.query(Criteria.where("expiresAt").lte(dateTime));
        mongoTemplate.remove(query, FriendRequest.class);
    }

    public Optional<PasswordReset> findById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, PasswordReset.class));
    }

    // Tests only
    public Optional<PasswordReset> findByUserId(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, PasswordReset.class));
    }
}
