package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.GameStatus;
import com.tonkar.volleyballreferee.entity.GameType;
import com.tonkar.volleyballreferee.entity.Rules;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RulesDao {

    private final static ProjectionOperation sRulesSummaryProjection = Aggregation.project()
            .and("_id").as("_id")
            .and("createdBy").as("createdBy")
            .and("createdAt").as("createdAt")
            .and("updatedAt").as("updatedAt")
            .and("name").as("name")
            .and("kind").as("kind");

    private final MongoTemplate mongoTemplate;

    public void save(Rules rules) {
        mongoTemplate.save(rules);
    }

    public List<RulesSummary> listRules(String userId, List<GameType> kinds) {
        kinds = DaoUtils.computeKinds(kinds);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").in(kinds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Rules.class), RulesSummary.class).getMappedResults();
    }

    public List<RulesSummary> listRulesOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId).and("kind").is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Rules.class), RulesSummary.class).getMappedResults();
    }

    public void updateScheduledGamesWithRules(String userId, Rules rules) {
        Query query = Query.query(
                Criteria.where("createdBy").is(userId)
                        .and("status").is(GameStatus.SCHEDULED)
                        .and("rules._id").is(rules.getId())
                        .and("rules.createdBy").is(userId));
        mongoTemplate.updateMulti(query, Update.update("rules", rules), mongoTemplate.getCollectionName(Game.class));
    }

    public CloseableIterator<RulesSummary> findByCreatedByOrderByNameAsc(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("createdBy").is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, "name");
        return mongoTemplate.aggregateStream(
                Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                mongoTemplate.getCollectionName(Rules.class), RulesSummary.class);
    }

    public Optional<Rules> findByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, Rules.class));
    }

    public Optional<Rules> findByIdAndCreatedByAndKind(UUID id, String userId, GameType kind) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId).and("kind").is(kind));
        return Optional.ofNullable(mongoTemplate.findOne(query, Rules.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, Rules.class);
    }

    public boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind) {
        Query query = Query.query(Criteria.where("createdBy").is(userId).and("name").is(name).and("kind").is(kind));
        return mongoTemplate.exists(query, Rules.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where("createdBy").is(userId));
        return mongoTemplate.count(query, Rules.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where("_id").is(id).and("createdBy").is(userId));
        mongoTemplate.remove(query, Rules.class);
    }
}
