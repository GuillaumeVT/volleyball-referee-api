package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.TeamSummary;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class TeamDao {

    private final static ProjectionOperation sTeamSummaryProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("name").as("name")
            .and("kind").as("kind")
            .and("gender").as("gender");

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<TeamSummary> listTeams(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sTeamSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Team.class), TeamSummary.class).getMappedResults();
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

}
