package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.GameDescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GameDescriptionRepository extends MongoRepository<GameDescription,String> {

    boolean existsByDate(long date);

    boolean existsByDateAndStatus(long date, String status);

    boolean existsByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(String socialId, String provider, String status, String rulesName);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndHName(String socialId, String provider, String status, String teamName);

    boolean existsByUserId_SocialIdAndUserId_ProviderAndStatusAndGName(String socialId, String provider, String status, String teamName);

    GameDescription findByDate(long date);

    GameDescription findByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    List<GameDescription> findByStatus(String status);

    List<GameDescription> findByHNameIgnoreCaseLikeOrGNameIgnoreCaseLikeOrLeagueIgnoreCaseLikeOrRefereeIgnoreCaseLike(String hName, String gName, String leagueName, String refereeName);

    List<GameDescription> findByScheduleBetween(long fromDate, long toDate);

    List<GameDescription> findByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    List<GameDescription> findByUserId_SocialIdAndUserId_ProviderAndKindAndLeague(String socialId, String provider, String kind, String leagueName);

    List<GameDescription> findByUserId_SocialIdAndUserId_ProviderAndStatusAndRules(String socialId, String provider, String status, String rulesName);

    @Query("{ '$and': [ { 'userId.socialId': ?0 }, { 'userId.provider': ?1 }, { 'status': ?2 }, { '$or': [ { 'hName': ?3 }, { 'gName': ?3 } ] } ] }")
    List<GameDescription> findByUserId_SocialIdAndUserId_ProviderAndStatusAndTeamName(String socialId, String provider, String status, String teamName);

    @Query("{ '$and': [ { 'userId.socialId': ?0 }, { 'userId.provider': ?1 }, { 'kind': ?2 }, { 'league': ?3 }, { '$or': [ { 'hName': ?4 }, { 'gName': ?4 } ] }, { 'gender': ?5 } ] }")
    List<GameDescription> findByUserId_SocialIdAndUserId_ProviderAndKindAndLeagueAndTeamNameAndGender(String socialId, String provider, String kind, String leagueName, String teamName, String teamGender);

    List<GameDescription> findTop10ByUserId_SocialIdAndUserId_ProviderAndKindAndStatusAndLeagueOrderByScheduleAsc(String socialId, String provider, String kind, String status, String leagueName);

    List<GameDescription> findTop10ByUserId_SocialIdAndUserId_ProviderAndKindAndStatusAndLeagueOrderByScheduleDesc(String socialId, String provider, String kind, String status, String leagueName);

    List<GameDescription> findByAndUserId_SocialIdAndUserId_ProviderAndKindAndStatusAndLeague(String socialId, String provider, String kind, String status, String leagueName);

    void deleteByDateAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    void deleteByDateAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    long deleteByDateLessThanAndUserId_SocialIdAndUserId_Provider(long date, String socialId, String provider);

    long deleteByDateLessThanAndUserId_SocialIdAndUserId_ProviderAndStatus(long date, String socialId, String provider, String status);

    long count();

    long countByStatus(String status);

    long countByUserId_SocialIdAndUserId_Provider(String socialId, String provider);

    long countByUserId_SocialIdAndUserId_ProviderAndKindAndLeague(String socialId, String provider, String kind, String leagueName);
}