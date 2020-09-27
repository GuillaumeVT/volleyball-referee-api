package com.tonkar.volleyballreferee.dao;

import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.dto.GameScore;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
            .and("score").as("score")
            .and("referee1").as("referee1Name")
            .and("referee2").as("referee2Name")
            .and("scorer").as("scorerName");

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(Game game) {
        mongoTemplate.save(game);
    }

    public Page<GameSummary> listLiveGames(List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria.where("status").is(GameStatus.LIVE).and("indexed").is(true).and("kind").in(kinds).and("gender").in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesMatchingToken(String token, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria.where("indexed").is(true).and("status").in(statuses).and("kind").in(kinds).and("gender").in(genders).orOperator(
                Criteria.where("homeTeam.name").regex(".*" + token + ".*", "i"),
                Criteria.where("guestTeam.name").regex(".*" + token + ".*", "i"),
                Criteria.where("league.name").regex(".*" + token + ".*", "i"),
                Criteria.where("refereeName").regex(".*" + token + ".*", "i"));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesWithScheduleDate(LocalDate date, List<GameStatus> statuses, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        long fromDate = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long toDate = date.atStartOfDay().plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli();

        Criteria criteria = Criteria.where("indexed").is(true).and("status").in(statuses).and("kind").in(kinds).and("gender").in(genders)
                .andOperator(Criteria.where("scheduledAt").gte(fromDate), Criteria.where("scheduledAt").lt(toDate));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
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

        Criteria criteria = Criteria.where("league._id").is(leagueId).and("status").in(statuses).and("gender").in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesInDivision(UUID leagueId, String divisionName, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").in(statuses).and("gender").in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, List<GameStatus> statuses, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);

        Criteria criteria = Criteria.where("league._id").is(leagueId).and("status").in(statuses)
                .orOperator(Criteria.where("homeTeam._id").is(teamId), Criteria.where("guestTeam._id").is(teamId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
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

        Criteria criteria = Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").in(statuses)
                .orOperator(Criteria.where("homeTeam._id").is(teamId), Criteria.where("guestTeam._id").is(teamId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
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

        Criteria criteria = Criteria.where("createdBy").is(userId).and("status").in(statuses).and("kind").in(kinds).and("gender").in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
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
        Criteria criteria = Criteria.where("status").is(GameStatus.COMPLETED).orOperator(
                Criteria.where("createdBy").is(userId), Criteria.where("refereedBy").is(userId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesInLeague(String userId, UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria.where("createdBy").is(userId).and("league._id").is(leagueId).and("status").in(statuses).and("gender").in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, "scheduledAt");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Game.class), GameSummary.class).getMappedResults();
        return new PageImpl<>(games, pageable, total);
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

    public List<GameScore> findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(UUID leagueId, String divisionName, GameStatus status) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("league._id").is(leagueId).and("league.division").is(divisionName).and("status").is(status));

        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("_id")
                .and("scheduledAt").as("scheduledAt")
                .and("homeTeam.name").as("homeTeamName")
                .and("guestTeam.name").as("guestTeamName")
                .and("homeTeam.color").as("homeTeamColor")
                .and("guestTeam.color").as("guestTeamColor")
                .and("homeSets").as("homeSets")
                .and("guestSets").as("guestSets")
                .and("sets.homePoints").as("sets.homePoints")
                .and("sets.guestPoints").as("sets.guestPoints");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.ASC, "scheduledAt"));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, sortOperation), mongoTemplate.getCollectionName(Game.class), GameScore.class)
                .getMappedResults();
    }

    public Optional<Game> findById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndCreatedByAndStatusNot(UUID id, String userId, GameStatus status) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId).and("status").ne(status));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUser(UUID id, String userId) {
        Query query = Query.query(
                Criteria
                        .where("_id").is(id)
                        .andOperator(new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("refereedBy").is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUserAndStatus(UUID id, String userId, GameStatus status) {
        Query query = Query.query(
                Criteria
                        .where("_id").is(id)
                        .and("status").is(status)
                        .andOperator(new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("refereedBy").is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUserAndStatusNot(UUID id, String userId, GameStatus status) {
        Query query = Query.query(
                Criteria
                        .where("_id").is(id)
                        .and("status").ne(status)
                        .andOperator(new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("refereedBy").is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndRules_IdAndStatus(String userId, UUID rulesId, GameStatus status) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("status").is(status).and("rules._id").is(rulesId));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndTeamAndStatus(String userId, UUID teamId, GameStatus status) {
        Query query = Query.query(
                Criteria
                        .where("createdBy").is(userId)
                        .and("status").is(status)
                        .andOperator(new Criteria().orOperator(
                                Criteria.where("homeTeam._id").is(teamId),
                                Criteria.where("guestTeam._id").is(teamId))));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndLeague_IdAndStatus(String userId, UUID leagueId, GameStatus status) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("status").is(status).and("league._id").is(leagueId));
        return mongoTemplate.exists(query, Game.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where("createdBy").is(userId));
        return mongoTemplate.count(query, Game.class);
    }

    public long countByCreatedByAndLeague_Id(String userId, UUID leagueId) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("league._id").is(leagueId));
        return mongoTemplate.count(query, Game.class);
    }

    public long countByAllowedUserAndStatusNot(String userId, GameStatus status) {
        Query query = Query.query(
                Criteria
                        .where("status").ne(status)
                        .andOperator(new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("refereedBy").is(userId))));
        return mongoTemplate.count(query, Game.class);
    }

    public void deleteByCreatedByAndStatus(String userId, GameStatus status) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("status").is(status));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByCreatedByAndStatusAndLeague_Id(String userId, GameStatus status, UUID leagueId) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("status").is(status).and("league._id").is(leagueId));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByScheduledAtLessThanAndStatus(long scheduledAt, GameStatus status) {
        Query query = Query.query(Criteria.where("scheduledAt").lt(scheduledAt).and("status").is(status));
        mongoTemplate.remove(query, Game.class);
    }

    public boolean updateReferee(UUID id, String refereedBy, String refereeName, long updatedAt) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("refereedBy", refereedBy).set("refereeName", refereeName).set("updatedAt", updatedAt);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Game.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateIndexed(UUID id, boolean indexed, long updatedAt) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("indexed", indexed).set("updatedAt", updatedAt);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Game.class);
        return updateResult.getModifiedCount() > 0;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    private static class DivisionNameContainer {
        private String divisionName;
    }
}
