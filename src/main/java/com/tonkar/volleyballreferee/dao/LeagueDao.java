package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.LeagueDescription;
import com.tonkar.volleyballreferee.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LeagueDao {

    private static ProjectionOperation sLeagueDescriptionProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("name").as("name")
            .and("kind").as("kind");

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<LeagueDescription> listLeagues(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sLeagueDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(League.class), LeagueDescription.class).getMappedResults();
    }

    public List<LeagueDescription> listLeaguesOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sLeagueDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(League.class), LeagueDescription.class).getMappedResults();
    }
}
