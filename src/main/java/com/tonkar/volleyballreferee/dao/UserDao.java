package com.tonkar.volleyballreferee.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final static ProjectionOperation sUserSummaryProjection = Aggregation
            .project()
            .and(_id)
            .as(_id)
            .and(User.Fields.pseudo)
            .as(UserSummary.Fields.pseudo)
            .and(User.Fields.email)
            .as(UserSummary.Fields.email)
            .and(User.Fields.admin)
            .as(UserSummary.Fields.admin)
            .and(User.Fields.subscription)
            .as(UserSummary.Fields.subscription)
            .and(User.Fields.subscriptionExpiryAt)
            .as(UserSummary.Fields.subscriptionExpiryAt);

    private final MongoTemplate mongoTemplate;

    public void save(User user) {
        mongoTemplate.save(user);
    }

    public void delete(User user) {
        mongoTemplate.remove(user);
    }

    public boolean existsById(String id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, User.class);
    }

    public boolean existsByPseudo(String pseudo) {
        Query query = Query.query(Criteria.where(User.Fields.pseudo).is(pseudo));
        return mongoTemplate.exists(query, User.class);
    }

    public Optional<User> findById(String id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public Optional<User> findByPseudo(String pseudo) {
        Query query = Query.query(Criteria.where(User.Fields.pseudo).is(pseudo));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public Optional<User> findByEmail(String email) {
        Query query = Query.query(Criteria.where(User.Fields.email).is(email));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    public boolean areFriends(String id1, String id2) {
        Query query = Query.query(Criteria.where("id").is(id1).and(User.Fields.friends + "." + User.Friend.Fields.id).is(id2));
        return mongoTemplate.exists(query, User.class);
    }

    public boolean existsByPurchaseToken(String purchaseToken) {
        Query query = Query.query(Criteria.where(User.Fields.purchaseToken).is(purchaseToken));
        return mongoTemplate.exists(query, User.class);
    }

    public Optional<UserSummary> findUserByPurchaseToken(String purchaseToken) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(User.Fields.purchaseToken).is(purchaseToken));
        return Optional.ofNullable(mongoTemplate
                                           .aggregate(Aggregation.newAggregation(matchOperation, sUserSummaryProjection),
                                                      mongoTemplate.getCollectionName(User.class), UserSummary.class)
                                           .getUniqueMappedResult());
    }

    public void updateSubscriptionPurchaseToken(String id, String purchaseToken, long subscriptionExpiryAt) {
        Query query = Query.query(Criteria.where(_id).is(id));
        Update update = new Update();
        update.set(User.Fields.purchaseToken, purchaseToken).set(User.Fields.subscriptionExpiryAt, subscriptionExpiryAt);
        mongoTemplate.updateFirst(query, update, mongoTemplate.getCollectionName(User.class));
    }

    public List<User> findUsersBySubscriptionExpiryBefore(long monthsAgo) {
        long accountRemovalThresholdDate = LocalDateTime.now(ZoneOffset.UTC).minusMonths(monthsAgo).toEpochSecond(ZoneOffset.UTC) * 1000L;
        return mongoTemplate.find(Query.query(
                                          Criteria.where(User.Fields.subscription).is(true).and(User.Fields.subscriptionExpiryAt).lt(accountRemovalThresholdDate)),
                                  User.class);
    }

    public boolean updateUserSignedIn(String userId, long lastLoginAt) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update()
                .set(User.Fields.lastLoginAt, lastLoginAt)
                .set(User.Fields.failedAuthentication + "." + User.FailedAuthentication.Fields.attempts, 0);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean addFailedAuthentication(String userId, User.FailedAuthentication failedAuthentication) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().set(User.Fields.failedAuthentication, failedAuthentication);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPassword(String userId, String password) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update()
                .set(User.Fields.password, password)
                .set(User.Fields.failedAuthentication + "." + User.FailedAuthentication.Fields.attempts, 0);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPseudo(String userId, String pseudo) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().set(User.Fields.pseudo, pseudo);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateFriendPseudo(String userId, String pseudo) {
        Query query = new Query(Criteria.where(User.Fields.friends + "." + User.Friend.Fields.id).is(userId));
        Update update = new Update().set(User.Fields.friends + ".$." + User.Friend.Fields.pseudo, pseudo);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean addFriend(String userId, User.Friend friend) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().addToSet(User.Fields.friends, friend);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean removeFriend(String userId, String friendId) {
        Query query = new Query(Criteria.where(_id).is(userId));
        Update update = new Update().pull(User.Fields.friends, new BasicDBObject(_id, friendId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, User.class);
        return updateResult.getModifiedCount() > 0;
    }

    public Page<User> listUsers(String filter, Pageable pageable) {
        Criteria criteria;

        if (filter != null && filter.trim().length() > 0) {
            criteria = Criteria
                    .where(User.Fields.admin)
                    .is(false)
                    .orOperator(Criteria.where(User.Fields.pseudo).regex(".*" + filter + ".*", "i"),
                                Criteria.where(User.Fields.email).regex(".*" + filter + ".*", "i"),
                                Criteria.where(User.Fields.purchaseToken).regex(".*" + filter + ".*", "i"));
        } else {
            criteria = Criteria.where(User.Fields.admin).is(false);
        }

        long total = mongoTemplate.count(Query.query(criteria), User.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, User.Fields.pseudo);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<User> users = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(User.class), User.class)
                .getMappedResults();
        return new PageImpl<>(users, pageable, total);
    }
}
