package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.LeagueSummary;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.League;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LeagueDao {

    private final static ProjectionOperation sLeagueSummaryProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("name").as("name")
            .and("kind").as("kind");

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<LeagueSummary> listLeagues(String userId, List<GameType> kinds) {
        kinds = DaoUtils.computeKinds(kinds);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").in(kinds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sLeagueSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(League.class), LeagueSummary.class).getMappedResults();
    }

    public List<LeagueSummary> listLeaguesOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sLeagueSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(League.class), LeagueSummary.class).getMappedResults();
    }

    public void save(League league) {
        mongoTemplate.save(league);
    }

    public Optional<League> findById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, League.class));
    }

    public Optional<League> findByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, League.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, League.class);
    }

    public boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("name").is(name).and("kind").is(kind));
        return mongoTemplate.exists(query, League.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where("createdBy").is(userId));
        return mongoTemplate.count(query, League.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        mongoTemplate.remove(query, League.class);
    }
}
