package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.GenderType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    private final static ProjectionOperation sGameSummaryProjection = Aggregation.project()
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
            .and("league._id").as("leagueId")
            .and("league.name").as("leagueName")
            .and("league.division").as("divisionName")
            .and("homeTeam._id").as("homeTeamId")
            .and("homeTeam.name").as("homeTeamName")
            .and("guestTeam._id").as("guestTeamId")
            .and("guestTeam.name").as("guestTeamName")
            .and("homeSets").as("homeSets")
            .and("guestSets").as("guestSets")
            .and("rules._id").as("rulesId")
            .and("rules.name").as("rulesName")
            .and("score").as("score");

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<GameSummary> listLiveGames(List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("status").is(GameStatus.LIVE).and("indexed").is(true).and("kind").in(kinds).and("gender").in(genders));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public Page<GameSummary> listGamesMatchingToken(String token, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("indexed").is(true).and("status").in(statuses).and("kind").in(kinds).and("gender").in(genders).orOperator(
                Criteria.where("homeTeam.name").regex(".*" + token + ".*", "i"),
                Criteria.where("guestTeam.name").regex(".*" + token + ".*", "i"),
                Criteria.where("league.name").regex(".*" + token + ".*", "i"),
                Criteria.where("refereeName").regex(".*" + token + ".*", "i")
        ));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public Page<GameSummary> listGamesWithScheduleDate(LocalDate date, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        long fromDate = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long toDate = date.atStartOfDay().plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli();

        MatchOperation matchOperation = Aggregation.match(Criteria.where("indexed").is(true).and("status").in(statuses).and("kind").in(kinds).and("gender").in(genders)
                .andOperator(Criteria.where("scheduledAt").gte(fromDate), Criteria.where("scheduledAt").lt(toDate)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public List<GameSummary> listGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public List<GameSummary> listGamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public Page<GameSummary> listGamesInLeague(UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("status").in(statuses).and("gender").in(genders));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public Page<GameSummary> listGamesInDivision(UUID leagueId, String divisionName, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").in(statuses).and("gender").in(genders));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public Page<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, List<GameStatus> statuses, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("status").in(statuses)
                .orOperator(Criteria.where("homeTeam._id").is(teamId), Criteria.where("guestTeam._id").is(teamId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public List<GameSummary> listLiveGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("status").is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public List<GameSummary> listLast10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("status").is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public List<GameSummary> listNext10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("status").is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "scheduledAt");
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public Page<GameSummary> listGamesOfTeamInDivision(UUID leagueId, String divisionName, UUID teamId, List<GameStatus> statuses, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").in(statuses)
                        .orOperator(Criteria.where("homeTeam._id").is(teamId), Criteria.where("guestTeam._id").is(teamId))
        );
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public List<GameSummary> listLiveGamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public List<GameSummary> listLast10GamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public List<GameSummary> listNext10GamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "scheduledAt");
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public Page<GameSummary> listGames(String userId, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("status").in(statuses).and("kind").in(kinds).and("gender").in(genders));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public List<GameSummary> listAvailableGames(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                .where("status").in(GameStatus.SCHEDULED, GameStatus.LIVE)
                .orOperator(Criteria.where("createdBy").is(userId), Criteria.where("refereedBy").is(userId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "scheduledAt");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
    }

    public Page<GameSummary> listCompletedGames(String userId, Pageable pageable) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                .where("status").is(GameStatus.COMPLETED)
                .orOperator(Criteria.where("createdBy").is(userId), Criteria.where("refereedBy").is(userId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public Page<GameSummary> listGamesInLeague(String userId, UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("league._id").is(leagueId).and("status").in(statuses).and("gender").in(genders));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, games.size());
    }

    public List<String> listDivisionsInLeague(String userId, UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("league._id").is(leagueId).and("league.division").exists(true).ne(""));
        ProjectionOperation projectionOperation = Aggregation.project().and("league.division").as("divisionName");
        GroupOperation groupOperation = Aggregation
                .group("divisionName")
                .first("divisionName").as("divisionName");

        List<DivisionNameContainer> containers = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation),
                mongoTemplate.getCollectionName(Game.class), DivisionNameContainer.class).getMappedResults();

        return containers.stream().map(DivisionNameContainer::getDivisionName).sorted().collect(Collectors.toList());
    }

    @NoArgsConstructor
    @Getter
    @Setter
    private static class DivisionNameContainer {
        private String divisionName;
    }
}
