package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;

import java.util.List;

public interface UserService {

    List<Rules> listUserRules(String userId);

    List<Rules> listDefaultRules();

    Rules getUserRules(String userId, String name);

    Rules getRules(String userId, String name);

    long getNumberOfUserRules(String userId);

    boolean createUserRules(Rules rules);

    boolean updateUserRules(Rules rules);

    boolean deleteUserRules(String userId, String name);

    List<Team> listUserTeams(String userId);

    List<Team> listUserTeamsOfKind(String userId, String kind);

    List<Team> listUserTeamsInLeague(long date);

    Team getUserTeam(String userId, String name, String gender);

    long getNumberOfUserTeams(String userId);

    boolean createUserTeam(Team team);

    boolean updateUserTeam(Team team);

    boolean deleteUserTeam(String userId, String name, String gender);

    List<GameDescription> listUserGames(String userId);

    List<GameDescription> listAvailableUserGames(String userId);

    List<GameDescription> listUserGamesInLeague(String userId, String kind, String leagueName);

    List<GameDescription> listUserGamesInLeague(long leagueDate);

    List<GameDescription> listUserGamesOfTeamInLeague(long leagueDate, String teamName, String teamGender);

    List<GameDescription> listLiveUserGamesInLeague(long leagueDate);

    List<GameDescription> listLast10UserGamesInLeague(long leagueDate);

    List<GameDescription> listNext10UserGamesInLeague(long leagueDate);

    GameDescription getUserGame(String userId, long date);

    Game getUserGameFull(String userId, long date);

    long getNumberOfUserGames(String userId);

    long getNumberOfUserGames(String userId, String kind, String leagueName);

    boolean createUserGame(GameDescription gameDescription);

    boolean updateUserGame(GameDescription gameDescription);

    boolean deleteUserGame(String userId, long date);

    int getUserGameCode(String userId, long date);

    List<League> listUserLeagues(String userId);

    List<League> listUserLeaguesOfKind(String userId, String kind);

    List<String> listUserDivisionsOfKind(String userId, String kind);

    League getUserLeague(long date);

    League getUserLeague(String userId, long date);

    League getUserLeague(String userId, String name);

    long getNumberOfUserLeagues(String userId);

    boolean createUserLeague(League league);

    boolean deleteUserLeague(String userId, long date);

    byte[] getCsvLeague(String userId, String leagueName, String divisionName);

}
