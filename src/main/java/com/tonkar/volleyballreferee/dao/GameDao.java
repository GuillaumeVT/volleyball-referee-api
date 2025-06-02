package com.tonkar.volleyballreferee.dao;

import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.entity.Set;
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
            .as(GameSummaryDto.Fields.createdBy)
            .and(Game.Fields.createdAt)
            .as(GameSummaryDto.Fields.createdAt)
            .and(Game.Fields.updatedAt)
            .as(GameSummaryDto.Fields.updatedAt)
            .and(Game.Fields.scheduledAt)
            .as(GameSummaryDto.Fields.scheduledAt)
            .and(Game.Fields.refereedBy)
            .as(GameSummaryDto.Fields.refereedBy)
            .and(Game.Fields.refereeName)
            .as(GameSummaryDto.Fields.refereeName)
            .and(Game.Fields.kind)
            .as(GameSummaryDto.Fields.kind)
            .and(Game.Fields.gender)
            .as(GameSummaryDto.Fields.gender)
            .and(Game.Fields.usage)
            .as(GameSummaryDto.Fields.usage)
            .and(Game.Fields.status)
            .as(GameSummaryDto.Fields.status)
            .and(Game.Fields.league + "." + _id)
            .as(GameSummaryDto.Fields.leagueId)
            .and(Game.Fields.league + "." + LeagueSummaryDto.Fields.name)
            .as(GameSummaryDto.Fields.leagueName)
            .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
            .as(GameSummaryDto.Fields.divisionName)
            .and(Game.Fields.homeTeam + "." + _id)
            .as(GameSummaryDto.Fields.homeTeamId)
            .and(Game.Fields.homeTeam + "." + Team.Fields.name)
            .as(GameSummaryDto.Fields.homeTeamName)
            .and(Game.Fields.guestTeam + "." + _id)
            .as(GameSummaryDto.Fields.guestTeamId)
            .and(Game.Fields.guestTeam + "." + Team.Fields.name)
            .as(GameSummaryDto.Fields.guestTeamName)
            .and(Game.Fields.homeSets)
            .as(GameSummaryDto.Fields.homeSets)
            .and(Game.Fields.guestSets)
            .as(GameSummaryDto.Fields.guestSets)
            .and(Game.Fields.rules + "." + _id)
            .as(GameSummaryDto.Fields.rulesId)
            .and(Game.Fields.rules + "." + Rules.Fields.name)
            .as(GameSummaryDto.Fields.rulesName)
            .and(Game.Fields.score)
            .as(GameSummaryDto.Fields.score)
            .and(Game.Fields.referee1)
            .as(GameSummaryDto.Fields.referee1Name)
            .and(Game.Fields.referee2)
            .as(GameSummaryDto.Fields.referee2Name)
            .and(Game.Fields.scorer)
            .as(GameSummaryDto.Fields.scorerName);

    private final MongoTemplate mongoTemplate;

    public void save(Game game) {
        mongoTemplate.save(game);
    }

    public Page<GameSummaryDto> listLiveGames(java.util.Set<GameType> kinds, java.util.Set<GenderType> genders, Pageable pageable) {
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.status)
                .is(GameStatus.LIVE)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public Page<GameSummaryDto> listGamesMatchingToken(String token,
                                                       java.util.Set<GameStatus> statuses,
                                                       java.util.Set<GameType> kinds,
                                                       java.util.Set<GenderType> genders,
                                                       Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders)
                .orOperator(Criteria.where(Game.Fields.homeTeam + "." + Team.Fields.name).regex(".*" + token + ".*", "i"),
                            Criteria.where(Game.Fields.guestTeam + "." + Team.Fields.name).regex(".*" + token + ".*", "i"),
                            Criteria.where(Game.Fields.league + "." + LeagueSummaryDto.Fields.name).regex(".*" + token + ".*", "i"),
                            Criteria.where(Game.Fields.refereeName).regex(".*" + token + ".*", "i"));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public Page<GameSummaryDto> listGamesWithScheduleDate(LocalDate date,
                                                          java.util.Set<GameStatus> statuses,
                                                          java.util.Set<GameType> kinds,
                                                          java.util.Set<GenderType> genders,
                                                          Pageable pageable) {
        statuses = DaoUtils.computeStatuses(statuses);
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        long fromDate = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        long toDate = date.atStartOfDay().plusDays(1L).toInstant(ZoneOffset.UTC).toEpochMilli();

        Criteria criteria = Criteria
                .where(Game.Fields.status)
                .in(statuses)
                .and(Game.Fields.kind)
                .in(kinds)
                .and(Game.Fields.gender)
                .in(genders)
                .andOperator(Criteria.where(Game.Fields.scheduledAt).gte(fromDate), Criteria.where(Game.Fields.scheduledAt).lt(toDate));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public List<GameSummaryDto> listGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(Game.Fields.league + "." + _id).is(leagueId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public List<GameSummaryDto> listGamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public Page<GameSummaryDto> listGamesInLeague(UUID leagueId,
                                                  java.util.Set<GameStatus> statuses,
                                                  java.util.Set<GenderType> genders,
                                                  Pageable pageable) {
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
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public Page<GameSummaryDto> listGamesInDivision(UUID leagueId,
                                                    String divisionName,
                                                    java.util.Set<GameStatus> statuses,
                                                    java.util.Set<GenderType> genders,
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
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public Page<GameSummaryDto> listGamesOfTeamInLeague(UUID leagueId, UUID teamId, java.util.Set<GameStatus> statuses, Pageable pageable) {
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
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public LeagueDashboardDto findGamesInLeagueGroupedByStatus(UUID leagueId) {
        MatchOperation leagueMatchOperation = Aggregation.match(Criteria.where(Game.Fields.league + "." + _id).is(leagueId));
        MatchOperation liveMatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.LIVE));
        MatchOperation last10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.COMPLETED));
        MatchOperation next10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);

        FacetOperation facetOperation = Aggregation
                .facet(liveMatchOperation, sGameSummaryProjection, sortOperation)
                .as(LeagueDashboardDto.Fields.liveGames)
                .and(last10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboardDto.Fields.last10Games)
                .and(next10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboardDto.Fields.next10Games);

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(leagueMatchOperation, facetOperation), mongoTemplate.getCollectionName(Game.class),
                           LeagueDashboardDto.class)
                .getUniqueMappedResult();
    }

    public List<GameSummaryDto> listLiveGamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Game.Fields.league + "." + _id).is(leagueId).and(Game.Fields.status).is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public List<GameSummaryDto> listLast10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Game.Fields.league + "." + _id).is(leagueId).and(Game.Fields.status).is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public List<GameSummaryDto> listNext10GamesInLeague(UUID leagueId) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Game.Fields.league + "." + _id).is(leagueId).and(Game.Fields.status).is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, GameSummaryDto.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public Page<GameSummaryDto> listGamesOfTeamInDivision(UUID leagueId,
                                                          String divisionName,
                                                          UUID teamId,
                                                          java.util.Set<GameStatus> statuses,
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
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public LeagueDashboardDto findGamesInDivisionGroupedByStatus(UUID leagueId, String divisionName) {
        MatchOperation divisionMatchOperation = Aggregation.match(Criteria
                                                                          .where(Game.Fields.league + "." + _id)
                                                                          .is(leagueId)
                                                                          .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                          .is(divisionName));
        MatchOperation liveMatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.LIVE));
        MatchOperation last10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.COMPLETED));
        MatchOperation next10MatchOperation = Aggregation.match(Criteria.where(Game.Fields.status).is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);

        FacetOperation facetOperation = Aggregation
                .facet(liveMatchOperation, sGameSummaryProjection, sortOperation)
                .as(LeagueDashboardDto.Fields.liveGames)
                .and(last10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboardDto.Fields.last10Games)
                .and(next10MatchOperation, sGameSummaryProjection, sortOperation, limitOperation)
                .as(LeagueDashboardDto.Fields.next10Games);

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(divisionMatchOperation, facetOperation), mongoTemplate.getCollectionName(Game.class),
                           LeagueDashboardDto.class)
                .getUniqueMappedResult();
    }

    public List<GameSummaryDto> listLiveGamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(GameStatus.LIVE));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public List<GameSummaryDto> listLast10GamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(GameStatus.COMPLETED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public List<GameSummaryDto> listNext10GamesInDivision(UUID leagueId, String divisionName) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.league + "." + _id)
                                                                  .is(leagueId)
                                                                  .and(Game.Fields.league + "." + Game.SelectedLeague.Fields.division)
                                                                  .is(divisionName)
                                                                  .and(Game.Fields.status)
                                                                  .is(GameStatus.SCHEDULED));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, GameSummaryDto.Fields.scheduledAt);
        LimitOperation limitOperation = Aggregation.limit(10L);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public Page<GameSummaryDto> listGames(UUID userId,
                                          java.util.Set<GameStatus> statuses,
                                          java.util.Set<GameType> kinds,
                                          java.util.Set<GenderType> genders,
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
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public List<GameSummaryDto> listAvailableGames(UUID userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria
                                                                  .where(Game.Fields.status)
                                                                  .in(GameStatus.SCHEDULED, GameStatus.LIVE)
                                                                  .orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                              Criteria.where(Game.Fields.refereedBy).is(userId)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, GameSummaryDto.Fields.scheduledAt);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
    }

    public Page<GameSummaryDto> listCompletedGames(UUID userId, Pageable pageable) {
        Criteria criteria = Criteria
                .where(Game.Fields.status)
                .is(GameStatus.COMPLETED)
                .orOperator(Criteria.where(Game.Fields.createdBy).is(userId), Criteria.where(Game.Fields.refereedBy).is(userId));

        long total = mongoTemplate.count(Query.query(criteria), Game.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public Page<GameSummaryDto> listGamesInLeague(UUID userId,
                                                  UUID leagueId,
                                                  java.util.Set<GameStatus> statuses,
                                                  java.util.Set<GenderType> genders,
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
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.DESC, GameSummaryDto.Fields.scheduledAt);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<GameSummaryDto> games = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sGameSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Game.class), GameSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(games, pageable, total);
    }

    public List<String> listDivisionsInLeague(UUID userId, UUID leagueId) {
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
                .as(GameSummaryDto.Fields.divisionName);
        GroupOperation groupOperation = Aggregation
                .group(GameSummaryDto.Fields.divisionName)
                .first(GameSummaryDto.Fields.divisionName)
                .as(GameSummaryDto.Fields.divisionName);

        List<DivisionNameContainer> containers = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, groupOperation),
                           mongoTemplate.getCollectionName(Game.class), DivisionNameContainer.class)
                .getMappedResults();

        return containers.stream().map(DivisionNameContainer::getDivisionName).sorted().collect(Collectors.toList());
    }

    public List<GameScoreDto> findByLeague_IdAndLeague_DivisionAndStatusOrderByScheduledAtAsc(UUID leagueId,
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
                .and(GameSummaryDto.Fields.scheduledAt)
                .as(GameScoreDto.Fields.scheduledAt)
                .and(Game.Fields.homeTeam + "." + Team.Fields.name)
                .as(GameScoreDto.Fields.homeTeamName)
                .and(Game.Fields.guestTeam + "." + Team.Fields.name)
                .as(GameScoreDto.Fields.guestTeamName)
                .and(Game.Fields.homeTeam + "." + Team.Fields.color)
                .as(GameScoreDto.Fields.homeTeamColor)
                .and(Game.Fields.guestTeam + "." + Team.Fields.color)
                .as(GameScoreDto.Fields.guestTeamColor)
                .and(Game.Fields.homeSets)
                .as(GameScoreDto.Fields.homeSets)
                .and(Game.Fields.guestSets)
                .as(GameScoreDto.Fields.guestSets)
                .and(Game.Fields.sets + "." + Set.Fields.homePoints)
                .as(GameScoreDto.Fields.sets + "." + SetSummaryDto.Fields.homePoints)
                .and(Game.Fields.sets + "." + Set.Fields.guestPoints)
                .as(GameScoreDto.Fields.sets + "." + SetSummaryDto.Fields.guestPoints);

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.ASC, GameScoreDto.Fields.scheduledAt));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, sortOperation),
                           mongoTemplate.getCollectionName(Game.class), GameScoreDto.class)
                .getMappedResults();
    }

    public Optional<Game> findById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndCreatedByAndStatusNot(UUID id, UUID userId, GameStatus status) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Game.Fields.createdBy).is(userId).and(Game.Fields.status).ne(status));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUser(UUID id, UUID userId) {
        Query query = Query.query(Criteria
                                          .where(_id)
                                          .is(id)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public Optional<Game> findByIdAndAllowedUserAndStatus(UUID id, UUID userId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(_id)
                                          .is(id)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return Optional.ofNullable(mongoTemplate.findOne(query, Game.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndRules_IdAndStatus(UUID userId, UUID rulesId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .and(Game.Fields.rules + "." + _id)
                                          .is(rulesId));
        return mongoTemplate.exists(query, Game.class);
    }

    public boolean existsByCreatedByAndTeamAndStatus(UUID userId, UUID teamId, GameStatus status) {
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

    public boolean existsByCreatedByAndLeague_IdAndStatus(UUID userId, UUID leagueId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .and(Game.Fields.league + "." + _id)
                                          .is(leagueId));
        return mongoTemplate.exists(query, Game.class);
    }

    public long countByCreatedBy(UUID userId) {
        Query query = Query.query(Criteria.where(Game.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, Game.class);
    }

    public long countByCreatedByAndLeague_Id(UUID userId, UUID leagueId) {
        Query query = Query.query(Criteria.where(Game.Fields.createdBy).is(userId).and(Game.Fields.league + "." + _id).is(leagueId));
        return mongoTemplate.count(query, Game.class);
    }

    public long countByAllowedUserAndStatusNot(UUID userId, GameStatus status) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.status)
                                          .ne(status)
                                          .andOperator(new Criteria().orOperator(Criteria.where(Game.Fields.createdBy).is(userId),
                                                                                 Criteria.where(Game.Fields.refereedBy).is(userId))));
        return mongoTemplate.count(query, Game.class);
    }

    public void deleteByCreatedByAndStatus(UUID userId, GameStatus status) {
        Query query = Query.query(Criteria.where(Game.Fields.createdBy).is(userId).and(Game.Fields.status).is(status));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByCreatedByAndStatusAndLeague_Id(UUID userId, GameStatus status, UUID leagueId) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(status)
                                          .and(Game.Fields.league + "." + _id)
                                          .is(leagueId));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Game.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, Game.class);
    }

    public void deleteByScheduledAtLessThanAndStatus(long scheduledAt, GameStatus status) {
        Query query = Query.query(Criteria.where(Game.Fields.scheduledAt).lt(scheduledAt).and(Game.Fields.status).is(status));
        mongoTemplate.remove(query, Game.class);
    }

    public boolean updateReferee(UUID id, UUID refereedBy, String refereeName, long updatedAt) {
        Query query = new Query(Criteria.where(_id).is(id));
        Update update = new Update()
                .set(Game.Fields.refereedBy, refereedBy)
                .set(Game.Fields.refereeName, refereeName)
                .set(Game.Fields.updatedAt, updatedAt);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Game.class);
        return updateResult.getModifiedCount() > 0;
    }

    public boolean updateUserPseudo(UUID id, String pseudo) {
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
