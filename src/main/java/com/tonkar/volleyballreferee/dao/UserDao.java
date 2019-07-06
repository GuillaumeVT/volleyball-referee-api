package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.UserSummary;
import com.tonkar.volleyballreferee.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao {

    private final static ProjectionOperation sUserSummaryProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("pseudo").as("pseudo")
            .and("email").as("email");

    @Autowired
    private MongoTemplate mongoTemplate;

    public Optional<UserSummary> findUserById(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("_id").is(userId));
        return Optional.ofNullable(mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sUserSummaryProjection),
                mongoTemplate.getCollectionName(User.class), UserSummary.class).getUniqueMappedResult());
    }

}
