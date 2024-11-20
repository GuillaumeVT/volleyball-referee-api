package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.StatisticsGroupDto;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class StatisticsDao {

    private static final ProjectionOperation sStatisticsProjection = Aggregation.project("kind");

    private static final GroupOperation sStatisticsGroup = Aggregation.group("kind").first("kind").as("kind").count().as("count");

    private final MongoTemplate mongoTemplate;

    public StatisticsGroupDto findGlobalStatistics() {
        List<StatisticsGroupDto.CountDto> gameStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Game.class),
                           StatisticsGroupDto.CountDto.class)
                .getMappedResults();

        List<StatisticsGroupDto.CountDto> teamStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(sStatisticsProjection, sStatisticsGroup), mongoTemplate.getCollectionName(Team.class),
                           StatisticsGroupDto.CountDto.class)
                .getMappedResults();

        return new StatisticsGroupDto(new StatisticsGroupDto.StatisticsDto(gameStatistics, teamStatistics));
    }

    public StatisticsGroupDto findUserStatistics(UUID userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));

        FacetOperation gameFacetOperation = Aggregation
                .facet(sStatisticsProjection, sStatisticsGroup)
                .as(FacetStatistics.Fields.globalStatistics)
                .and(matchOperation, sStatisticsProjection, sStatisticsGroup)
                .as(FacetStatistics.Fields.userStatistics);

        FacetOperation teamFacetOperation = Aggregation
                .facet(sStatisticsProjection, sStatisticsGroup)
                .as(FacetStatistics.Fields.globalStatistics)
                .and(matchOperation, sStatisticsProjection, sStatisticsGroup)
                .as(FacetStatistics.Fields.userStatistics);

        FacetStatistics gameFacetStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(gameFacetOperation), mongoTemplate.getCollectionName(Game.class),
                           FacetStatistics.class)
                .getUniqueMappedResult();

        FacetStatistics teamFacetStatistics = mongoTemplate
                .aggregate(Aggregation.newAggregation(teamFacetOperation), mongoTemplate.getCollectionName(Team.class),
                           FacetStatistics.class)
                .getUniqueMappedResult();

        assert gameFacetStatistics != null;
        assert teamFacetStatistics != null;

        return new StatisticsGroupDto(
                new StatisticsGroupDto.StatisticsDto(gameFacetStatistics.globalStatistics(), teamFacetStatistics.globalStatistics()),
                new StatisticsGroupDto.StatisticsDto(gameFacetStatistics.userStatistics(), teamFacetStatistics.userStatistics()));
    }

    @FieldNameConstants
    private record FacetStatistics(List<StatisticsGroupDto.CountDto> globalStatistics, List<StatisticsGroupDto.CountDto> userStatistics) {}
}
