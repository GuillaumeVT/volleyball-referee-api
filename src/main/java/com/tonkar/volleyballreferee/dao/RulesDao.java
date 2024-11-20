package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.RulesSummaryDto;
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
            .as(RulesSummaryDto.Fields.createdBy)
            .and(Rules.Fields.createdAt)
            .as(RulesSummaryDto.Fields.createdAt)
            .and(Rules.Fields.updatedAt)
            .as(RulesSummaryDto.Fields.updatedAt)
            .and(Rules.Fields.name)
            .as(RulesSummaryDto.Fields.name)
            .and(Rules.Fields.kind)
            .as(RulesSummaryDto.Fields.kind);

    private final MongoTemplate mongoTemplate;

    public void save(Rules rules) {
        mongoTemplate.save(rules);
    }

    public List<RulesSummaryDto> listRules(UUID userId, java.util.Set<GameType> kinds) {
        kinds = DaoUtils.computeKinds(kinds);

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Rules.Fields.createdBy).is(userId).and(Rules.Fields.kind).in(kinds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, RulesSummaryDto.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Rules.class), RulesSummaryDto.class)
                .getMappedResults();
    }

    public List<RulesSummaryDto> listRulesOfKind(UUID userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(Rules.Fields.createdBy).is(userId).and(Rules.Fields.kind).is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, RulesSummaryDto.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(Rules.class), RulesSummaryDto.class)
                .getMappedResults();
    }

    public void updateScheduledGamesWithRules(UUID userId, Rules rules) {
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

    public Stream<RulesSummaryDto> findByCreatedByOrderByNameAsc(UUID userId) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(Rules.Fields.createdBy).is(userId));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, RulesSummaryDto.Fields.name);
        return mongoTemplate.aggregateStream(Aggregation.newAggregation(matchOperation, sRulesSummaryProjection, sortOperation),
                                             mongoTemplate.getCollectionName(Rules.class), RulesSummaryDto.class);
    }

    public Optional<Rules> findByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Rules.Fields.createdBy).is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, Rules.class));
    }

    public Optional<Rules> findByIdAndCreatedByAndKind(UUID id, UUID userId, GameType kind) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Rules.Fields.createdBy).is(userId).and(Rules.Fields.kind).is(kind));
        return Optional.ofNullable(mongoTemplate.findOne(query, Rules.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, Rules.class);
    }

    public boolean existsByCreatedByAndNameAndKind(UUID userId, String name, GameType kind) {
        Query query = Query.query(
                Criteria.where(Rules.Fields.createdBy).is(userId).and(Rules.Fields.name).is(name).and(Rules.Fields.kind).is(kind));
        return mongoTemplate.exists(query, Rules.class);
    }

    public long countByCreatedBy(UUID userId) {
        Query query = Query.query(Criteria.where(Rules.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, Rules.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(Rules.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, Rules.class);
    }
}
