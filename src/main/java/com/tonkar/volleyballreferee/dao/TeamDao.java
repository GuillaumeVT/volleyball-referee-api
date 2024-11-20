package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.*;
import java.util.stream.Stream;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class TeamDao {

    private final static ProjectionOperation sTeamSummaryProjection = Aggregation
            .project()
            .and(_id)
            .as(_id)
            .and(Team.Fields.createdBy)
            .as(TeamSummaryDto.Fields.createdBy)
            .and(Team.Fields.createdAt)
            .as(TeamSummaryDto.Fields.createdAt)
            .and(Team.Fields.updatedAt)
            .as(TeamSummaryDto.Fields.updatedAt)
            .and(Team.Fields.name)
            .as(TeamSummaryDto.Fields.name)
            .and(Team.Fields.kind)
            .as(TeamSummaryDto.Fields.kind)
            .and(Team.Fields.gender)
            .as(TeamSummaryDto.Fields.gender);

    private final MongoTemplate mongoTemplate;

    public void save(Team team) {
        mongoTemplate.save(team);
    }

    public Page<TeamSummaryDto> listTeams(UUID userId,
                                          java.util.Set<GameType> kinds,
                                          java.util.Set<GenderType> genders,
                                          Pageable pageable) {
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria
                .where(Team.Fields.createdBy)
                .is(userId)
                .and(Team.Fields.kind)
                .in(kinds)
                .and(Team.Fields.gender)
                .in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Team.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, TeamSummaryDto.Fields.name);
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<TeamSummaryDto> teams = mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation, skipOperation, limitOperation),
                           mongoTemplate.getCollectionName(Team.class), TeamSummaryDto.class)
                .getMappedResults();
        return new PageDto<>(teams, pageable, total);
    }

    public List<TeamSummaryDto> listTeamsOfKind(UUID userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(Team.Fields.createdBy).is(userId).and(Team.Fields.kind).is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, TeamSummaryDto.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Team.class), TeamSummaryDto.class)
                .getMappedResults();
    }

    public List<TeamSummaryDto> listTeamsWithIds(Set<UUID> teamIds) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(_id).in(teamIds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, TeamSummaryDto.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Team.class), TeamSummaryDto.class)
                .getMappedResults();
    }

    public void updateScheduledGamesWithHomeTeam(UUID userId, Team team) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(GameStatus.SCHEDULED)
                                          .and(Game.Fields.homeTeam + "." + _id)
                                          .is(team.getId())
                                          .and(Game.Fields.homeTeam + "." + Team.Fields.createdBy)
                                          .is(userId));
        mongoTemplate.updateMulti(query, Update.update(Game.Fields.homeTeam, team), mongoTemplate.getCollectionName(Game.class));
    }

    public void updateScheduledGamesWithGuestTeam(UUID userId, Team team) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(GameStatus.SCHEDULED)
                                          .and(Game.Fields.guestTeam + "." + _id)
                                          .is(team.getId())
                                          .and(Game.Fields.guestTeam + "." + Team.Fields.createdBy)
                                          .is(userId));
        mongoTemplate.updateMulti(query, Update.update(Game.Fields.guestTeam, team), mongoTemplate.getCollectionName(Game.class));
    }

    public Stream<TeamSummaryDto> findByCreatedByOrderByNameAsc(UUID userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(Team.Fields.createdBy).is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, TeamSummaryDto.Fields.name);
        return mongoTemplate.aggregateStream(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                                             mongoTemplate.getCollectionName(Team.class), TeamSummaryDto.class);
    }

    public Optional<Team> findByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Team.Fields.createdBy).is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, Team.class));
    }

    public Optional<Team> findByIdAndCreatedByAndKind(UUID id, UUID userId, GameType kind) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Team.Fields.createdBy).is(userId).and(Team.Fields.kind).is(kind));
        return Optional.ofNullable(mongoTemplate.findOne(query, Team.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, Team.class);
    }

    public boolean existsByCreatedByAndNameAndKindAndGender(UUID userId, String name, GameType kind, GenderType gender) {
        Query query = Query.query(Criteria
                                          .where(Team.Fields.createdBy)
                                          .is(userId)
                                          .and(Team.Fields.name)
                                          .is(name)
                                          .and(Team.Fields.kind)
                                          .is(kind)
                                          .and(Team.Fields.gender)
                                          .is(gender));
        return mongoTemplate.exists(query, Team.class);
    }

    public long countByCreatedBy(UUID userId) {
        Query query = Query.query(Criteria.where(Team.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, Team.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Team.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, Team.class);
    }
}
