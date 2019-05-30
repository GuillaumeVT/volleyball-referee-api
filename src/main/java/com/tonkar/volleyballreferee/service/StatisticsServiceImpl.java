package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dto.Statistics;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.Team;
import com.tonkar.volleyballreferee.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Statistics getStatistics() {
        ProjectionOperation projectionOperation = Aggregation.project("kind");
        GroupOperation groupOperation = Aggregation
                .group("kind")
                .first("kind").as("kind")
                .count().as("count");

        List<Statistics.Count> gameStatistics = mongoTemplate.aggregate(Aggregation.newAggregation(projectionOperation, groupOperation),
                mongoTemplate.getCollectionName(Game.class), Statistics.Count.class).getMappedResults();

        List<Statistics.Count> teamStatistics = mongoTemplate.aggregate(Aggregation.newAggregation(projectionOperation, groupOperation),
                mongoTemplate.getCollectionName(Team.class), Statistics.Count.class).getMappedResults();

        Statistics statistics = new Statistics();
        statistics.getGameStatistics().addAll(gameStatistics);
        statistics.getTeamStatistics().addAll(teamStatistics);
        return statistics;
    }

    @Override
    public Statistics getStatistics(User user) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(user.getId()));
        ProjectionOperation projectionOperation = Aggregation.project("kind");
        GroupOperation groupOperation = Aggregation
                .group("kind")
                .first("kind").as("kind")
                .count().as("count");

        List<Statistics.Count> gameStatistics = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation),
                mongoTemplate.getCollectionName(Game.class), Statistics.Count.class).getMappedResults();

        List<Statistics.Count> teamStatistics = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation),
                mongoTemplate.getCollectionName(Team.class), Statistics.Count.class).getMappedResults();

        Statistics statistics = new Statistics();
        statistics.getGameStatistics().addAll(gameStatistics);
        statistics.getTeamStatistics().addAll(teamStatistics);
        return statistics;
    }

}
