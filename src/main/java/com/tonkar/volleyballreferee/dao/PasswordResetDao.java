package com.tonkar.volleyballreferee.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class PasswordResetDao {

    private final MongoTemplate mongoTemplate;

    public void save(PasswordReset passwordReset) {
        mongoTemplate.save(passwordReset);
    }

    public void delete(PasswordReset passwordReset) {
        mongoTemplate.remove(passwordReset);
    }

    public void deleteByExpiresAtBefore(LocalDateTime dateTime) {
        Query query = Query.query(Criteria.where(PasswordReset.Fields.expiresAt).lte(dateTime));
        mongoTemplate.remove(query, PasswordReset.class);
    }

    public Optional<PasswordReset> findById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, PasswordReset.class));
    }
}
