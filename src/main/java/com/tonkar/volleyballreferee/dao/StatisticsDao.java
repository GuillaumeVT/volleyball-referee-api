package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatisticsDao {

    private static final ProjectionOperation sStatisticsProjection = Aggregation.project("kind");

    private static final GroupOperation sStatisticsGroup = Aggregation
            .group("kind")
            .first("kind").as("kind")
            .count().as("count");

    private final MongoTemplate mongoTemplate;

    public List<Statistics.Count> findGameStatistics() {
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Game.class), Statistics.Count.class)
                .getMappedResults();
    }

    public List<Statistics.Count> findTeamStatistics() {
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Team.class), Statistics.Count.class)
                .getMappedResults();
    }

    public List<Statistics.Count> findGameStatistics(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Game.class), Statistics.Count.class)
                .getMappedResults();
    }

    public List<Statistics.Count> findTeamStatistics(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Team.class), Statistics.Count.class)
                .getMappedResults();
    }
}
