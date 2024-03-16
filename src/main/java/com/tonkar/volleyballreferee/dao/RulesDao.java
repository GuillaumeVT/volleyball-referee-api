package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.RulesSummary;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class RulesDao {

    private final static ProjectionOperation sRulesSummaryProjection = Aggregation
            .project()
            .and(_id)
            .as(_id)
            .and(Rules.Fields.createdBy)
            .as(RulesSummary.Fields.createdBy)
            .and(Rules.Fields.createdAt)
            .as(RulesSummary.Fields.createdAt)
            .and(Rules.Fields.updatedAt)
            .as(RulesSummary.Fields.updatedAt)
            .and(Rules.Fields.name)
            .as(RulesSummary.Fields.name)
            .and(Rules.Fields.kind)
            .as(RulesSummary.Fields.kind);

    private final MongoTemplate mongoTemplate;

    public void save(Rules rules) {
        mongoTemplate.save(rules);
    }

    public List<RulesSummary> listRules(String userId, List<GameType> kinds) {
        kinds = DaoUtils.computeKinds(kinds);

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Rules.Fields.createdBy).is(userId).and(Rules.Fields.kind).in(kinds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, RulesSummary.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Rules.class), RulesSummary.class)
                .getMappedResults();
    }

    public List<RulesSummary> listRulesOfKind(String userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Rules.Fields.createdBy).is(userId).and(Rules.Fields.kind).is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, RulesSummary.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Rules.class), RulesSummary.class)
                .getMappedResults();
    }

    public void updateScheduledGamesWithRules(String userId, Rules rules) {
        Query query = Query.query(Criteria
                                          .where(Game.Fields.createdBy)
                                          .is(userId)
                                          .and(Game.Fields.status)
                                          .is(GameStatus.SCHEDULED)
                                          .and(Game.Fields.rules + "." + _id)
                                          .is(rules.getId())
                                          .and(Game.Fields.rules + "." + Rules.Fields.createdBy)
                                          .is(userId));
        mongoTemplate.updateMulti(query, Update.update(Game.Fields.rules, rules), mongoTemplate.getCollectionName(Game.class));
    }

    public Stream<RulesSummary> findByCreatedByOrderByNameAsc(String userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(Rules.Fields.createdBy).is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, RulesSummary.Fields.name);
        return mongoTemplate.aggregateStream(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                                             mongoTemplate.getCollectionName(Rules.class), RulesSummary.class);
    }

    public Optional<Rules> findByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Rules.Fields.createdBy).is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, Rules.class));
    }

    public Optional<Rules> findByIdAndCreatedByAndKind(UUID id, String userId, GameType kind) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Rules.Fields.createdBy).is(userId).and(Rules.Fields.kind).is(kind));
        return Optional.ofNullable(mongoTemplate.findOne(query, Rules.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, Rules.class);
    }

    public boolean existsByCreatedByAndNameAndKind(String userId, String name, GameType kind) {
        Query query = Query.query(
                Criteria.where(Rules.Fields.createdBy).is(userId).and(Rules.Fields.name).is(name).and(Rules.Fields.kind).is(kind));
        return mongoTemplate.exists(query, Rules.class);
    }

    public long countByCreatedBy(String userId) {
        Query query = Query.query(Criteria.where(Rules.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, Rules.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, String userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Rules.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, Rules.class);
    }
}
