package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;

import java.util.List;

public interface UserService {

    List<Rules> listUserRules(UserId userId);

    List<Rules> listDefaultRules();

    Rules getUserRules(UserId userId, String name);

    Rules getRules(UserId userId, String name);

    long getNumberOfUserRules(UserId userId);

    boolean createUserRules(Rules rules);

    boolean updateUserRules(Rules rules);

    boolean deleteUserRules(UserId userId, String name);

    List<Team> listUserTeams(UserId userId);

    List<Team> listUserTeamsOfKind(UserId userId, String kind);

    List<Team> listUserTeamsOfKindInLeague(long date);

    Team getUserTeam(UserId userId, String name, String gender);

    long getNumberOfUserTeams(UserId userId);

    boolean createUserTeam(Team team);

    boolean updateUserTeam(Team team);

    boolean deleteUserTeam(UserId userId, String name, String gender);

    List<GameDescription> listUserGames(UserId userId);

    List<GameDescription> listUserGamesInLeague(UserId userId, String kind, String leagueName);

    List<GameDescription> listUserGamesInLeague(long leagueDate);

    List<GameDescription> listUserGamesOfTeamInLeague(long leagueDate, String teamName, String teamGender);

    List<GameDescription> listLiveUserGamesInLeague(long leagueDate);

    List<GameDescription> listLast10UserGamesInLeague(long leagueDate);

    List<GameDescription> listNext10UserGamesInLeague(long leagueDate);

    GameDescription getUserGame(UserId userId, long date);

    long getNumberOfUserGames(UserId userId);

    long getNumberOfUserGames(UserId userId, String kind, String leagueName);

    boolean createUserGame(GameDescription gameDescription);

    boolean updateUserGame(GameDescription gameDescription);

    boolean deleteUserGame(UserId userId, long date);

    int getUserGameCode(UserId userId, long date);

    List<League> listUserLeagues(UserId userId);

    List<League> listUserLeaguesOfKind(UserId userId, String kind);

    League getUserLeague(long date);

    League getUserLeague(UserId userId, long date);

    League getUserLeague(UserId userId, String name);

    long getNumberOfUserLeagues(UserId userId);

    boolean createUserLeague(League league);

    boolean deleteUserLeague(UserId userId, long date);
}
