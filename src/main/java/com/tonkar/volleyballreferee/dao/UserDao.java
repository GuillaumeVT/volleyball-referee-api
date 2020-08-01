package com.tonkar.volleyballreferee.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final static ProjectionOperation sUserSummaryProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("pseudo").as("pseudo")
            .and("email").as("email");

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(User user) {
        mongoTemplate.save(user);
    }

    public boolean existsById(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, User.class);
    }

    public boolean existsByPseudo(String pseudo) {
        Query query = Query.query(Criteria.where("pseudo").is(pseudo));
        return mongoTemplate.exists(query, User.class);
    }

    public boolean existsByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, User.class);
    }

    public Optional<User> findById(String id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public Optional<User> findByPseudo(String pseudo) {
        Query query = Query.query(Criteria.where("pseudo").is(pseudo));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public Optional<User> findByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public boolean areFriends(String id1, String id2) {
        Query query = Query.query(Criteria.where("id").is(id1).and("friends.id").is(id2));
        return mongoTemplate.exists(query, User.class);
    }

    public boolean existsByPurchaseToken(String purchaseToken) {
        Query query = Query.query(Criteria.where("purchaseToken").is(purchaseToken));
        return mongoTemplate.exists(query, User.class);
    }

    public Optional<UserSummary> findUserByPurchaseToken(String purchaseToken) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("purchaseToken").is(purchaseToken));
        return Optional.ofNullable(mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sUserSummaryProjection),
                mongoTemplate.getCollectionName(User.class), UserSummary.class).getUniqueMappedResult());
    }

    public void updateSubscriptionPurchaseToken(String id, String purchaseToken, long subscriptionExpiryAt) {
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("purchaseToken", purchaseToken).set("subscriptionExpiryAt", subscriptionExpiryAt);
        mongoTemplate.updateFirst(query, update, mongoTemplate.getCollectionName(User.class));
    }

    public List<User> findUsersBySubscriptionExpiryBefore(long monthsAgo) {
        long accountRemovalThresholdDate = LocalDateTime.now(ZoneOffset.UTC).minusMonths(monthsAgo).toEpochSecond(ZoneOffset.UTC) * 1000L;
        return mongoTemplate.find(
                Query.query(Criteria.where("subscription").is(true).and("subscriptionExpiryAt").lt(accountRemovalThresholdDate)),
                User.class);
    }

    public boolean updateUserSignedIn(String userId, long lastLoginAt) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().set("lastLoginAt", lastLoginAt).set("failedAuthentication.attempts", 0);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean addFailedAuthentication(String userId, User.FailedAuthentication failedAuthentication) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().set("failedAuthentication", failedAuthentication);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPassword(String userId, String password) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().set("password", password).set("failedAuthentication.attempts", 0);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean addFriend(String userId, User.Friend friend) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().addToSet("friends", friend);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean removeFriend(String userId, String friendId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().pull("friends", new BasicDBObject("_id", friendId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }
}
