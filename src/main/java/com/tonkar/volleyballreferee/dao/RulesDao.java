package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.RulesDescription;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RulesDao {

    private static ProjectionOperation sRulesDescriptionProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("name").as("name")
            .and("kind").as("kind");

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<RulesDescription> listRules(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sRulesDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Rules.class), RulesDescription.class).getMappedResults();
    }

    public List<RulesDescription> listRulesOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sRulesDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Rules.class), RulesDescription.class).getMappedResults();
    }

    public void updateScheduledGamesWithRules(String userId, Rules rules) {
        Query query = Query.query(
                Criteria.where("createdBy").is(userId)
                        .and("status").is(GameStatus.SCHEDULED)
                        .and("rules._id").is(rules.getId())
                        .and("rules.createdBy").is(userId));
        mongoTemplate.updateMulti(query, Update.update("rules", rules), mongoTemplate.getCollectionName(Game.class));
    }
}
