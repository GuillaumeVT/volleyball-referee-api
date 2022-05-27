package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.StatisticsGroup;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
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

    public StatisticsGroup findGlobalStatistics() {
        List<StatisticsGroup.Count> gameStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Game.class), StatisticsGroup.Count.class)
                .getMappedResults();

        List<StatisticsGroup.Count> teamStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Team.class), StatisticsGroup.Count.class)
                .getMappedResults();

        return new StatisticsGroup(new StatisticsGroup.Statistics(gameStatistics, teamStatistics));
    }

    public StatisticsGroup findUserStatistics(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));

        FacetOperation gameFacetOperation = Aggregation
                .facet(sStatisticsProjection, sStatisticsGroup).as("globalStatistics")
                .and(matchOperation, sStatisticsProjection, sStatisticsGroup).as("userStatistics");

        FacetOperation teamFacetOperation = Aggregation
                .facet(sStatisticsProjection, sStatisticsGroup).as("globalStatistics")
                .and(matchOperation, sStatisticsProjection, sStatisticsGroup).as("userStatistics");

        FacetStatistics gameFacetStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(gameFacetOperation), mongoTemplate.getCollectionName(Game.class), FacetStatistics.class)
                .getUniqueMappedResult();

        FacetStatistics teamFacetStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(teamFacetOperation), mongoTemplate.getCollectionName(Team.class), FacetStatistics.class)
                .getUniqueMappedResult();

        assert gameFacetStatistics != null;
        assert teamFacetStatistics != null;

        return new StatisticsGroup(
                new StatisticsGroup.Statistics(gameFacetStatistics.getGlobalStatistics(), teamFacetStatistics.getGlobalStatistics()),
                new StatisticsGroup.Statistics(gameFacetStatistics.getUserStatistics(), teamFacetStatistics.getUserStatistics())
        );
    }

    @NoArgsConstructor
    @Getter
    @Setter
    private static class FacetStatistics {
        private List<StatisticsGroup.Count> globalStatistics;
        private List<StatisticsGroup.Count> userStatistics;
    }
}
