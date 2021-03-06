package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.TeamSummary;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamDao {

    private final static ProjectionOperation sTeamSummaryProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("name").as("name")
            .and("kind").as("kind")
            .and("gender").as("gender");

    private final MongoTemplate mongoTemplate;

    public void save(Team team) {
        mongoTemplate.save(team);
    }

    public Page<TeamSummary> listTeams(String userId, List<GameType> kinds, List<GenderType> genders, Pageable pageable) {
        kinds = DaoUtils.computeKinds(kinds);
        genders = DaoUtils.computeGenders(genders);

        Criteria criteria = Criteria.where("createdBy").is(userId).and("kind").in(kinds).and("gender").in(genders);

        long total = mongoTemplate.count(Query.query(criteria), Team.class);

        MatchOperation matchOperation = Aggregation.match(criteria);
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * (long) pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());
        List<TeamSummary> teams = mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation, skipOperation, limitOperation),
                mongoTemplate.getCollectionName(Team.class), TeamSummary.class).getMappedResults();
        return new PageImpl<>(teams, pageable, total);
    }

    public List<TeamSummary> listTeamsOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Team.class), TeamSummary.class).getMappedResults();
    }

    public List<TeamSummary> listTeamsWithIds(Set<UUID> teamIds) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("_id").in(teamIds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Team.class), TeamSummary.class).getMappedResults();
    }

    public void updateScheduledGamesWithHomeTeam(String userId, Team team) {
        Query query = Query.query(
                Criteria.where("createdBy").is(userId)
                        .and("status").is(GameStatus.SCHEDULED)
                        .and("homeTeam._id").is(team.getId())
                        .and("homeTeam.createdBy").is(userId));
        mongoTemplate.updateMulti(query, Update.update("homeTeam", team), mongoTemplate.getCollectionName(Game.class));
    }

    public void updateScheduledGamesWithGuestTeam(String userId, Team team) {
        Query query = Query.query(
                Criteria.where("createdBy").is(userId)
                        .and("status").is(GameStatus.SCHEDULED)
                        .and("guestTeam._id").is(team.getId())
                        .and("guestTeam.createdBy").is(userId));
        mongoTemplate.updateMulti(query, Update.update("guestTeam", team), mongoTemplate.getCollectionName(Game.class));
    }

    public CloseableIterator<TeamSummary> findByCreatedByOrderByNameAsc(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregateStream(
                Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Team.class), TeamSummary.class);
    }

    public Optional<Team> findByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, Team.class));
    }

    public Optional<Team> findByIdAndCreatedByAndKind(UUID id, String userId, GameType kind) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId).and("kind").is(kind));
        return Optional.ofNullable(mongoTemplate.findOne(query, Team.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, Team.class);
    }

    public boolean existsByCreatedByAndNameAndKindAndGender(String userId, String name, GameType kind, GenderType gender) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("name").is(name).and("kind").is(kind).and("gender").is(gender));
        return mongoTemplate.exists(query, Team.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where("createdBy").is(userId));
        return mongoTemplate.count(query, Team.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        mongoTemplate.remove(query, Team.class);
    }
}
