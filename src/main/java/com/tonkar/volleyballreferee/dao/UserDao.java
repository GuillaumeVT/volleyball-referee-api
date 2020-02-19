package com.tonkar.volleyballreferee.dao;

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

    public Optional<UserSummary> findUserByPurchaseToken(String purchaseToken) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("purchaseToken").is(purchaseToken));
        return Optional.ofNullable(mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sUserSummaryProjection),
                mongoTemplate.getCollectionName(User.class), UserSummary.class).getUniqueMappedResult());
    }

    public void updateSubscriptionPurchaseToken(String id, String purchaseToken, long subscriptionExpiredAt) {
        Query query = Query.query(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("purchaseToken", purchaseToken).set("subscriptionExpiredAt", subscriptionExpiredAt);
        mongoTemplate.updateFirst(query, update, mongoTemplate.getCollectionName(User.class));
    }

    public List<User> findUsersBySubscriptionExpiryBefore(long monthsAgo) {
        long accountRemovalThresholdDate = LocalDateTime.now(ZoneOffset.UTC).minusMonths(monthsAgo).toEpochSecond(ZoneOffset.UTC) * 1000L;
        return mongoTemplate.find(
                Query.query(Criteria.where("subscription").is(true).and("subscriptionExpiredAt").lt(accountRemovalThresholdDate)),
                User.class);
    }
}
