package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.GameDescription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameDescriptionRepository extends MongoRepository<GameDescription,String> {

    boolean existsByDate(long date);

    boolean existsByDateAndStatus(long date, String status);

    boolean existsByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(String socialId, String provider, String status, String rulesName);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndHName(String socialId, String provider, String status, String teamName);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndGName(String socialId, String provider, String status, String teamName);

    GameDescription findGameDescriptionByDate(long date);

    GameDescription findGameDescriptionByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    List<GameDescription> findGameDescriptionsByStatus(String status);

    List<GameDescription> findGameDescriptionsByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLikeOrRefereeIgnoreCaseLike(String hName, String gName, String league, String referee);

    List<GameDescription> findGameDescriptionsByDateBetween(long fromDate, long toDate);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(String socialId, String provider, String status, String rulesName);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndStatusAndHName(String socialId, String provider, String status, String teamName);

    List<GameDescription> findGameDescriptionsByUserId_SocialIdAndUserId_ProviderAndStatusAndGName(String socialId, String provider, String status, String teamName);

    void deleteGameDescriptionByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    void deleteGameDescriptionByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    long deleteGameDescriptionsByDateLessThanAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteGameDescriptionsByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    long count();

    long countByStatus(String status);

    long countByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

}