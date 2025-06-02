package com.tonkar.volleyballreferee.dao;

import com.tonkar.volleyballreferee.dto.LeagueSummaryDto;
import com.tonkar.volleyballreferee.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.Set;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class LeagueDao {

    private final static ProjectionOperation sLeagueSummaryProjection = Aggregation
            .project()
            .and(_id)
            .as(_id)
            .and(League.Fields.createdBy)
            .as(LeagueSummaryDto.Fields.createdBy)
            .and(League.Fields.createdAt)
            .as(LeagueSummaryDto.Fields.createdAt)
            .and(League.Fields.updatedAt)
            .as(LeagueSummaryDto.Fields.updatedAt)
            .and(League.Fields.name)
            .as(LeagueSummaryDto.Fields.name)
            .and(League.Fields.kind)
            .as(LeagueSummaryDto.Fields.kind);

    private final MongoTemplate mongoTemplate;

    public List<LeagueSummaryDto> listLeagues(UUID userId, Set<GameType> kinds) {
        kinds = DaoUtils.computeKinds(kinds);

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(League.Fields.createdBy).is(userId).and(League.Fields.kind).in(kinds));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, LeagueSummaryDto.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sLeagueSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(League.class), LeagueSummaryDto.class)
                .getMappedResults();
    }

    public List<LeagueSummaryDto> listLeaguesOfKind(UUID userId, GameType kind) {
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where(League.Fields.createdBy).is(userId).and(League.Fields.kind).is(kind));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, LeagueSummaryDto.Fields.name);
        return mongoTemplate
                .aggregate(Aggregation.newAggregation(matchOperation, sLeagueSummaryProjection, sortOperation),
                           mongoTemplate.getCollectionName(League.class), LeagueSummaryDto.class)
                .getMappedResults();
    }

    public void save(League league) {
        mongoTemplate.save(league);
    }

    public Optional<League> findById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, League.class));
    }

    public Optional<League> findByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(League.Fields.createdBy).is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, League.class));
    }

    public boolean existsById(UUID id) {
        Query query = Query.query(Criteria.where(_id).is(id));
        return mongoTemplate.exists(query, League.class);
    }

    public boolean existsByCreatedByAndNameAndKind(UUID userId, String name, GameType kind) {
        Query query = Query.query(
                Criteria.where(League.Fields.createdBy).is(userId).and(League.Fields.name).is(name).and(League.Fields.kind).is(kind));
        return mongoTemplate.exists(query, League.class);
    }

    public long countByCreatedBy(UUID userId) {
        Query query = Query.query(Criteria.where(League.Fields.createdBy).is(userId));
        return mongoTemplate.count(query, League.class);
    }

    public void deleteByIdAndCreatedBy(UUID id, UUID userId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(League.Fields.createdBy).is(userId));
        mongoTemplate.remove(query, League.class);
    }
}
