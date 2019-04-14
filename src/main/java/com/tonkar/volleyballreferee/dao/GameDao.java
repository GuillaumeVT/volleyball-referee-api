package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.GameDescription;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class GameDao {

    private static ProjectionOperation sGameDescriptionProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("scheduledAt").as("scheduledAt")
            .and("refereedBy").as("refereedBy")
            .and("refereeName").as("refereeName")
            .and("kind").as("kind")
            .and("gender").as("gender")
            .and("usage").as("usage")
            .and("status").as("status")
            .and("indexed").as("indexed")
            .and("leagueId").as("leagueId")
            .and("leagueName").as("leagueName")
            .and("divisionName").as("divisionName")
            .and("homeTeam._id").as("homeTeamId")
            .and("homeTeam.name").as("homeTeamName")
            .and("guestTeam._id").as("guestTeamId")
            .and("guestTeam.name").as("guestTeamName")
            .and("hSets").as("hSets")
            .and("gSets").as("gSets")
            .and("rules._id").as("rulesId")
            .and("rules.name").as("rulesName");

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<GameDescription> listLiveGames() {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("status").is(GameStatus.LIVE).and("indexed").is(true));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGamesMatchingToken(String token) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("indexed").is(true).orOperator(
                Criteria.where("homeTeam.name").regex(".*" + token + ".*", "i"),
                Criteria.where("guestTeam.name").regex(".*" + token + ".*", "i"),
                Criteria.where("leagueName").regex(".*" + token + ".*", "i"),
                Criteria.where("refereeName").regex(".*" + token + ".*", "i")
        ));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGamesWithScheduleDate(LocalDate date) {
        long fromDate = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long toDate = date.atStartOfDay().plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli();

        MatchOperation matchOperation = Aggregation.match(Criteria.where("indexed").is(true).andOperator(Criteria.where("scheduledAt").gte(fromDate), Criteria.where("scheduledAt").lt(toDate)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("leagueId").is(leagueId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGamesOfTeamInLeague(UUID leagueId, UUID teamId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("leagueId").is(leagueId).orOperator(Criteria.where("homeTeam._id").is(teamId), Criteria.where("guestTeam._id").is(teamId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listLiveGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("leagueId").is(leagueId).and("status").is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listLast10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("leagueId").is(leagueId).and("status").is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listNext10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("leagueId").is(leagueId).and("status").is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "scheduledAt");
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGames(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGamesWithStatus(String userId, GameStatus status) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("status").is(status));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listAvailableGames(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                .where("status").in(GameStatus.SCHEDULED, GameStatus.LIVE)
                .orOperator(Criteria.where("createdBy").is(userId), Criteria.where("refereedBy").is(userId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<GameDescription> listGamesInLeague(String userId, UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("leagueId").is(leagueId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameDescriptionProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameDescription.class).getMappedResults();
    }

    public List<String> listDivisionsInLeague(String userId, UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("leagueId").is(leagueId).and("divisionName").exists(true).ne(""));
        ProjectionOperation projectionOperation = Aggregation.project("divisionName");
        GroupOperation groupOperation = Aggregation
                .group("divisionName")
                .first("divisionName").as("divisionName");

        List<DivisionNameContainer> containers = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation),
                mongoTemplate.getCollectionName(Game.class), DivisionNameContainer.class).getMappedResults();

        return containers.stream().map(DivisionNameContainer::getDivisionName).sorted().collect(Collectors.toList());
    }

    @NoArgsConstructor @Getter @Setter
    private static class DivisionNameContainer {
        private String divisionName;
    }
}
