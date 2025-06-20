package com.tonkar.volleyballreferee.dao;

import com.mongodb.client.result.UpdateResult;
import com.tonkar.volleyballreferee.entity.FriendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.tonkar.volleyballreferee.dao.DaoUtils._id;

@Repository
@RequiredArgsConstructor
public class FriendRequestDao {

    private final MongoTemplate mongoTemplate;

    public void save(FriendRequest friendRequest) {
        mongoTemplate.save(friendRequest);
    }

    public long countByReceiverId(UUID receiverId) {
        Query query = Query.query(Criteria.where(FriendRequest.Fields.receiverId).is(receiverId));
        return mongoTemplate.count(query, FriendRequest.class);
    }

    public boolean existsBySenderIdAndReceiverId(UUID senderId, UUID receiverId) {
        Query query = Query.query(
                Criteria.where(FriendRequest.Fields.senderId).is(senderId).and(FriendRequest.Fields.receiverId).is(receiverId));
        return mongoTemplate.exists(query, FriendRequest.class);
    }

    public Optional<FriendRequest> findByIdAndReceiverId(UUID id, UUID receiverId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(FriendRequest.Fields.receiverId).is(receiverId));
        return Optional.ofNullable(mongoTemplate.findOne(query, FriendRequest.class));
    }

    public List<FriendRequest> findBySenderId(UUID senderId) {
        Query query = Query.query(Criteria.where(FriendRequest.Fields.senderId).is(senderId));
        return mongoTemplate.find(query, FriendRequest.class);
    }

    public List<FriendRequest> findByReceiverId(UUID receiverId) {
        Query query = Query.query(Criteria.where(FriendRequest.Fields.receiverId).is(receiverId));
        return mongoTemplate.find(query, FriendRequest.class);
    }

    public boolean updateReceiverPseudo(UUID receiverId, String pseudo) {
        Query query = Query.query(Criteria.where(FriendRequest.Fields.receiverId).is(receiverId));
        Update update = new Update().set(FriendRequest.Fields.receiverPseudo, pseudo);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, FriendRequest.class);
        return (updateResult.getMatchedCount() > 0 && updateResult.getModifiedCount() > 0) || updateResult.getMatchedCount() == 0;
    }

    public boolean updateSenderPseudo(UUID senderId, String pseudo) {
        Query query = Query.query(Criteria.where(FriendRequest.Fields.senderId).is(senderId));
        Update update = new Update().set(FriendRequest.Fields.senderPseudo, pseudo);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, FriendRequest.class);
        return (updateResult.getMatchedCount() > 0 && updateResult.getModifiedCount() > 0) || updateResult.getMatchedCount() == 0;
    }

    public void deleteByIdAndReceiverId(UUID id, UUID receiverId) {
        Query query = Query.query(Criteria.where(_id).is(id).and(FriendRequest.Fields.receiverId).is(receiverId));
        mongoTemplate.remove(query, FriendRequest.class);
    }

    public void deleteById(FriendRequest friendRequest) {
        mongoTemplate.remove(friendRequest);
    }
}
