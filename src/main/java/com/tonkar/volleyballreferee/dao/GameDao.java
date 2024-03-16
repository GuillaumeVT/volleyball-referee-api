package com.tonkar.volleyballreferee.dao;

import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.entity.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class GameDao {

    private final static ProjectionOperation sGameSummaryProjection = Aggregation
            .project()
            .and(_id)
            .as(_id)
            .and(Game.Fields.createdBy)
            .as(GameSummary.Fields.createdBy)
            .and(Game.Fields.createdAt)
            .as(GameSummary.Fields.createdAt)
            .and(Game.Fields.updatedAt)
            .as(GameSummary.Fields.updatedAt)
            .and(Game.Fields.scheduledAt)
            .as(GameSummary.Fields.scheduledAt)
            .and(Game.Fields.refereedBy)
            .as(GameSummary.Fields.refereedBy)
            .and(Game.Fields.refereeName)
            .as(GameSummary.Fields.refereeName)
            .and(Game.Fields.kind)
            .as(GameSummary.Fields.kind)
            .and(Game.Fields.gender)
            .as(GameSummary.Fields.gender)
            .and(Game.Fields.usage)
            .as(GameSummary.Fields.usage)
            .and(Game.Fields.status)
            .as(GameSummary.Fields.status)
            .and(Game.Fields.indexed)
            .as(GameSummary.Fields.indexed)
            .and(Game.Fields.league + "." + _id)
            .as(GameSummary.Fields.leagueId)
            .and(Game.Fields.league + "." + LeagueSummary.Fields.name)
            .as(GameSummary.Fields.leagueName)
            .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
            .as(GameSummary.Fields.divisionName)
            .and(Game.Fields.homeTeam + "." + _id)
            .as(GameSummary.Fields.homeTeamId)
            .and(Game.Fields.homeTeam + "." + Team.Fields.name)
            .as(GameSummary.Fields.homeTeamName)
            .and(Game.Fields.guestTeam + "." + _id)
            .as(GameSummary.Fields.guestTeamId)
            .and(Game.Fields.guestTeam + "." + Team.Fields.name)
            .as(GameSummary.Fields.guestTeamName)
            .and(Game.Fields.homeSets)
            .as(GameSummary.Fields.homeSets)
            .and(Game.Fields.guestSets)
            .as(GameSummary.Fields.guestSets)
            .and(Game.Fields.rules + "." + _id)
            .as(GameSummary.Fields.rulesId)
            .and(Game.Fields.rules + "." + Rules.Fields.name)
            .as(GameSummary.Fields.rulesName)
            .and(Game.Fields.score)
            .as(GameSummary.Fields.score)
            .and(Game.Fields.referee1)
            .as(GameSummary.Fields.referee1Name)
            .and(Game.Fields.referee2)
            .as(GameSummary.Fields.referee2Name)
            .and(Game.Fields.scorer)
            .as(GameSummary.Fields.scorerName);

    private final MongoTemplate mongoTemplate;

    public void save(Game game) {
        mongoTemplate.save(game);
    }

    public Page<GameSummary> listLiveGames(List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.status)
                .is(GameStatus.LIVE)
                .and(Game.Fields.indexed)
                .is(true)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesMatchingToken(String token,
                                                    List<GameStatus> statuses,
                                                    List<GameType> kinds,
                                                    List<GenderType> genders,
                                                    Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.indexed)
                .is(true)
                .and(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders)
                .orOperator(Criteria.where(Game.Fields.homeTeam + "." + Team.Fields.name).regex(".*" + token + ".*", "i"),
                            Criteria.where(Game.Fields.guestTeam + "." + Team.Fields.name).regex(".*" + token + ".*", "i"),
                            Criteria.where(Game.Fields.league + "." + LeagueSummary.Fields.name).regex(".*" + token + ".*", "i"),
                            Criteria.where(Game.Fields.refereeName).regex(".*" + token + ".*", "i"));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesWithScheduleDate(LocalDate date,
                                                       List<GameStatus> statuses,
                                                       List<GameType> kinds,
                                                       List<GenderType> genders,
                                                       Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        long fromDate = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long toDate = date.atStartOfDay().plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli();

        Criteria criteria = Criteria
                .where(Game.Fields.indexed)
                .is(true)
                .and(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders)
                .andOperator(Criteria.where(Game.Fields.scheduledAt).gte(fromDate), Criteria.where(Game.Fields.scheduledAt).lt(toDate));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public List<GameSummary> listGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(Game.Fields.league + "." + _id).is(leagueId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public List<GameSummary> listGamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public Page<GameSummary> listGamesInLeague(UUID leagueId, List<GameStatus> statuses, List<GenderType> genders, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.league + "." + _id)
                .is(leagueId)
                .and(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesInDivision(UUID leagueId,
                                                 String divisionName,
                                                 List<GameStatus> statuses,
                                                 List<GenderType> genders,
                                                 Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.league + "." + _id)
                .is(leagueId)
                .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                .is(divisionName)
                .and(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, List<GameStatus> statuses, Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);

        Criteria criteria = Criteria
                .where(Game.Fields.league + "." + _id)
                .is(leagueId)
                .and(Game.Fields.status)
                .in(statuses)
                .orOperator(Criteria.where(Game.Fields.homeTeam + "." + _id).is(teamId),
                            Criteria.where(Game.Fields.guestTeam + "." + _id).is(teamId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public LeagueDashboard findGamesInLeagueGroupedByStatus(UUID leagueId) {
        MatchOperation leagueMatchOperation = Aggregation.match(Criteria.where(Game.Fields.league + "." + _id).is(leagueId));
        MatchOperation liveMatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.LIVE));
        MatchOperation last10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.COMPLETED));
        MatchOperation next10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);

        FacetOperation facetOperation = Aggregation
                .facet(liveMatchOperation, sGameSummaryProjection, sortOperation)
                .as(LeagueDashboard.Fields.liveGames)
                .and(last10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboard.Fields.last10Games)
                .and(next10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboard.Fields.next10Games);

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(leagueMatchOperation, facetOperation), mongoTemplate.getCollectionName(Game.class),
                           LeagueDashboard.class)
                .getUniqueMappedResult();
    }

    public List<GameSummary> listLiveGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Game.Fields.league + "." + _id).is(leagueId).and(Game.Fields.status).is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public List<GameSummary> listLast10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Game.Fields.league + "." + _id).is(leagueId).and(Game.Fields.status).is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public List<GameSummary> listNext10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Game.Fields.league + "." + _id).is(leagueId).and(Game.Fields.status).is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, GameSummary.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public Page<GameSummary> listGamesOfTeamInDivision(UUID leagueId,
                                                       String divisionName,
                                                       UUID teamId,
                                                       List<GameStatus> statuses,
                                                       Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);

        Criteria criteria = Criteria
                .where(Game.Fields.league + "." + _id)
                .is(leagueId)
                .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                .is(divisionName)
                .and(Game.Fields.status)
                .in(statuses)
                .orOperator(Criteria.where(Game.Fields.homeTeam + "." + _id).is(teamId),
                            Criteria.where(Game.Fields.guestTeam + "." + _id).is(teamId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public LeagueDashboard findGamesInDivisionGroupedByStatus(UUID leagueId, String divisionName) {
        MatchOperation divisionMatchOperation = Aggregation.match(Criteria
                                                                          .where(Game.Fields.league + "." + _id)
                                                                          .is(leagueId)
                                                                          .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                          .is(divisionName));
        MatchOperation liveMatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.LIVE));
        MatchOperation last10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.COMPLETED));
        MatchOperation next10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);

        FacetOperation facetOperation = Aggregation
                .facet(liveMatchOperation, sGameSummaryProjection, sortOperation)
                .as(LeagueDashboard.Fields.liveGames)
                .and(last10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboard.Fields.last10Games)
                .and(next10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboard.Fields.next10Games);

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(divisionMatchOperation, facetOperation), mongoTemplate.getCollectionName(Game.class),
                           LeagueDashboard.class)
                .getUniqueMappedResult();
    }

    public List<GameSummary> listLiveGamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public List<GameSummary> listLast10GamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public List<GameSummary> listNext10GamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, GameSummary.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public Page<GameSummary> listGames(String userId,
                                       List<GameStatus> statuses,
                                       List<GameType> kinds,
                                       List<GenderType> genders,
                                       Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.createdBy)
                .is(userId)
                .and(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public List<GameSummary> listAvailableGames(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.status)
                                                                  .in(GameStatus.SCHEDULED, GameStatus.LIVE)
                                                                  .orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                              Criteria.where(Game.Fields.refereedBy).is(userId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, GameSummary.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
    }

    public Page<GameSummary> listCompletedGames(String userId, Pageable pageable) {
        Criteria criteria = Criteria
                .where(Game.Fields.status)
                .is(GameStatus.COMPLETED)
                .orOperator(Criteria.where(Game.Fields.createdBy).is(userId), Criteria.where(Game.Fields.refereedBy).is(userId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public Page<GameSummary> listGamesInLeague(String userId,
                                               UUID leagueId,
                                               List<GameStatus> statuses,
                                               List<GenderType> genders,
                                               Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.createdBy)
                .is(userId)
                .and(Game.Fields.league + "." + _id)
                .is(leagueId)
                .and(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummary.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummary> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummary.class)
                .getMappedResults();
        return new PageImpl<>(games, pageable, total);
    }

    public List<String> listDivisionsInLeague(String userId, UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.createdBy)
                                                                  .is(userId)
                                                                  .and(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .exists(true)
                                                                  .ne(""));
        ProjectionOperation projectionOperation = Aggregation
                .project()
                .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                .as(GameSummary.Fields.divisionName);
        GroupOperation groupOperation = Aggregation
                .group(GameSummary.Fields.divisionName)
                .first(GameSummary.Fields.divisionName)
                .as(GameSummary.Fields.divisionName);

        List<DivisionNameContainer> containers = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation),
                           mongoTemplate.getCollectionName(Game.class), DivisionNameContainer.class)
                .getMappedResults();

        return containers.stream().map(DivisionNameContainer::getDivisionName).sorted().collect(Collectors.toList());
    }

    public List<GameScore> findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(UUID leagueId,
                                                                                           String divisionName,
                                                                                           GameStatus status) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(status));

        ProjectionOperation projectionOperation = Aggregation
                .project()
                .and(_id)
                .as(_id)
                .and(GameSummary.Fields.scheduledAt)
                .as(GameScore.Fields.scheduledAt)
                .and(Game.Fields.homeTeam + "." + Team.Fields.name)
                .as(GameScore.Fields.homeTeamName)
                .and(Game.Fields.guestTeam + "." + Team.Fields.name)
                .as(GameScore.Fields.guestTeamName)
                .and(Game.Fields.homeTeam + "." + Team.Fields.color)
                .as(GameScore.Fields.homeTeamColor)
                .and(Game.Fields.guestTeam + "." + Team.Fields.color)
                .as(GameScore.Fields.guestTeamColor)
                .and(Game.Fields.homeSets)
                .as(GameScore.Fields.homeSets)
                .and(Game.Fields.guestSets)
                .as(GameScore.Fields.guestSets)
                .and(Game.Fields.sets + "." + Set.Fields.homePoints)
                .as(GameScore.Fields.sets + "." + SetSummary.Fields.homePoints)
                .and(Game.Fields.sets + "." + Set.Fields.guestPoints)
                .as(GameScore.Fields.sets + "." + SetSummary.Fields.guestPoints);

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.ASC, GameScore.Fields.scheduledAt));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameScore.class)
                .getMappedResults();
    }

    public Optional<Game> findById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndCreatedByAndStatusNot(UUID id, String userId, GameStatus status) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Game.Fields.createdBy).is(userId).and(Game.Fields.status).ne(status));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUser(UUID id, String userId) {
        Query query = Query.query(Criteria
                                          .where(_id)
                                          .is(id)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUserAndStatus(UUID id, String userId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(_id)
                                          .is(id)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUserAndStatusNot(UUID id, String userId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(_id)
                                          .is(id)
                                          .and(Game.Fields.status)
                                          .ne(status)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndRules_IdAndStatus(String userId, UUID rulesId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .and(Game.Fields.rules + "." + _id)
                                          .is(rulesId));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndTeamAndStatus(String userId, UUID teamId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .andOperator(
                                                  new Criteria().orOperator(Criteria.where(Game.Fields.homeTeam + "." + _id).is(teamId),
                                                                            Criteria.where(Game.Fields.guestTeam + "." + _id).is(teamId))));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndLeague_IdAndStatus(String userId, UUID leagueId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .and(Game.Fields.league + "." + _id)
                                          .is(leagueId));
        return mongoTemplate.exists(query, Game.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where(Game.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, Game.class);
    }

    public long countByCreatedByAndLeague_Id(String userId, UUID leagueId) {
        Query query = Query.query(Criteria.where(Game.Fields.createdBy).is(userId).and(Game.Fields.league + "." + _id).is(leagueId));
        return mongoTemplate.count(query, Game.class);
    }

    public long countByAllowedUserAndStatusNot(String userId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.status)
                                          .ne(status)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return mongoTemplate.count(query, Game.class);
    }

    public void deleteByCreatedByAndStatus(String userId, GameStatus status) {
        Query query = Query.query(Criteria.where(Game.Fields.createdBy).is(userId).and(Game.Fields.status).is(status));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByCreatedByAndStatusAndLeague_Id(String userId, GameStatus status, UUID leagueId) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .and(Game.Fields.league + "." + _id)
                                          .is(leagueId));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Game.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByScheduledAtLessThanAndStatus(long scheduledAt, GameStatus status) {
        Query query = Query.query(Criteria.where(Game.Fields.scheduledAt).lt(scheduledAt).and(Game.Fields.status).is(status));
        mongoTemplate.remove(query, Game.class);
    }

    public boolean updateReferee(UUID id, String refereedBy, String refereeName, long updatedAt) {
        Query query = new Query(Criteria.where(_id).is(id));
        Update update = new Update()
                .set(Game.Fields.refereedBy, refereedBy)
                .set(Game.Fields.refereeName, refereeName)
                .set(Game.Fields.updatedAt, updatedAt);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Game.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateIndexed(UUID id, boolean indexed, long updatedAt) {
        Query query = new Query(Criteria.where(_id).is(id));
        Update update = new Update().set(Game.Fields.indexed, indexed).set(Game.Fields.updatedAt, updatedAt);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Game.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPseudo(String id, String pseudo) {
        Query query = new Query(Criteria.where(Game.Fields.refereedBy).is(id));
        Update update = new Update().set(Game.Fields.refereeName, pseudo);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, Game.class);
        return (updateResult.getMatchedCount() > 0 && updateResult.getModifiedCount() > 0) || updateResult.getMatchedCount() == 0;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    private static class DivisionNameContainer {
        private String divisionName;
    }
}
