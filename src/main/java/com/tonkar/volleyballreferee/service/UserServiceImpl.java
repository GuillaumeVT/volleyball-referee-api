package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.model.*;
import com.tonkar.volleyballreferee.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private RulesRepository rulesRepository;

    @Autowired
    private TeamRepository  teamRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameDescriptionRepository gameDescriptionRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private CodeRepository codeRepository;

    @Autowired
    private GameService gameService;

    private final Random random = new Random();

    @Override
    public List<Rules> listUserRules(String userId) {
        return rulesRepository.findByUserId(userId);
    }

    @Override
    public List<Rules> listDefaultRules() {
        List<Rules> rules = new ArrayList<>();
        rules.add(Rules.OFFICIAL_INDOOR_RULES);
        rules.add(Rules.OFFICIAL_BEACH_RULES);
        rules.add(Rules.DEFAULT_INDOOR_4X4_RULES);
        return rules;
    }

    @Override
    public Rules getUserRules(String userId, String name) {
        return rulesRepository.findByNameAndUserId(name, userId);
    }

    @Override
    public Rules getRules(String userId, String name) {
        Rules rules = getUserRules(userId, name);

        if (rules == null) {
            for (Rules defaultRules : listDefaultRules()) {
                if (defaultRules.getName().equals(name)) {
                    rules = defaultRules;
                }
            }
        }

        return rules;
    }

    @Override
    public long getNumberOfUserRules(String userId) {
        return rulesRepository.countByUserId(userId);
    }

    @Override
    public boolean createUserRules(Rules rules) {
        final boolean created;

        if (getUserRules(rules.getUserId(), rules.getName()) == null) {
            rulesRepository.insert(rules);
            LOGGER.debug(String.format("Created rules %s for user %s", rules.getName(), rules.getUserId()));
            created = true;
        } else {
            LOGGER.error(String.format("Could not create rules %s for user %s because they already exist", rules.getName(), rules.getUserId()));
            created = false;
        }

        return created;
    }

    @Override
    public boolean updateUserRules(Rules rules) {
        final boolean updated;

        Rules savedRules = getUserRules(rules.getUserId(), rules.getName());

        if (savedRules == null) {
            LOGGER.error(String.format("Could not update rules %s for user %s because they don't exist", rules.getName(), rules.getUserId()));
            updated = false;
        } else {
            savedRules.setDate(rules.getDate());
            savedRules.setSetsPerGame(rules.getSetsPerGame());
            savedRules.setPointsPerSet(rules.getPointsPerSet());
            savedRules.setTieBreakInLastSet(rules.isTieBreakInLastSet());
            savedRules.setPointsInTieBreak(rules.getPointsInTieBreak());
            savedRules.setTwoPointsDifference(rules.isTwoPointsDifference());
            savedRules.setSanctions(rules.isSanctions());
            savedRules.setTeamTimeouts(rules.isTeamTimeouts());
            savedRules.setTeamTimeoutsPerSet(rules.getTeamTimeoutsPerSet());
            savedRules.setTeamTimeoutDuration(rules.getTeamTimeoutDuration());
            savedRules.setTechnicalTimeouts(rules.isTechnicalTimeouts());
            savedRules.setTechnicalTimeoutDuration(rules.getTechnicalTimeoutDuration());
            savedRules.setGameIntervals(rules.isGameIntervals());
            savedRules.setGameIntervalDuration(rules.getGameIntervalDuration());
            savedRules.setTeamSubstitutionsPerSet(rules.getTeamSubstitutionsPerSet());
            savedRules.setBeachCourtSwitches(rules.isBeachCourtSwitches());
            savedRules.setBeachCourtSwitchFreq(rules.getBeachCourtSwitchFreq());
            savedRules.setBeachCourtSwitchFreqTieBreak(rules.getBeachCourtSwitchFreqTieBreak());
            savedRules.setCustomConsecutiveServesPerPlayer(rules.getCustomConsecutiveServesPerPlayer());
            rulesRepository.save(savedRules);
            LOGGER.debug(String.format("Updated rules %s for user %s", rules.getName(), rules.getUserId()));

            for (GameDescription gameDescription : gameService.listGameDescriptionsUsingRules(rules.getName(), rules.getUserId())) {
                updateUserGame(gameDescription);
            }

            updated = true;
        }

        return updated;
    }

    @Override
    public boolean deleteUserRules(String userId, String name) {
        final boolean deleted;

        if (gameService.hasGameUsingRules(name, userId)) {
            LOGGER.debug(String.format("Could not delete rules %s for user %s because they are used in a game", name, userId));
            deleted = false;
        } else {
            rulesRepository.deleteByNameAndUserId(name, userId);
            LOGGER.debug(String.format("Deleted rules %s for user %s", name, userId));
            deleted = true;
        }

        return deleted;
    }

    @Override
    public List<Team> listUserTeams(String userId) {
        return teamRepository.findByUserId(userId);
    }

    @Override
    public List<Team> listUserTeamsOfKind(String userId, String kind) {
        return teamRepository.findByUserIdAndKind(userId, kind);
    }

    @Override
    public List<Team> listUserTeamsOfKindInLeague(long date) {
        League league = getUserLeague(date);
        List<Team> allTeamsOfKind = listUserTeamsOfKind(league.getUserId(), league.getKind());
        List<GameDescription> leagueGames = listUserGamesInLeague(league.getUserId(), league.getKind(), league.getName());

        List<Team> teams = new ArrayList<>();

        for (Team team : allTeamsOfKind) {
            boolean found = false;
            int index = 0;

            while (!found && index < leagueGames.size()) {
                GameDescription game = leagueGames.get(index);

                if (team.getName().equals(game.gethName()) || team.getName().equals(game.getgName())) {
                    teams.add(team);
                    found = true;
                }

                index++;
            }
        }

        return teams;
    }

    @Override
    public Team getUserTeam(String userId, String name, String gender) {
        return teamRepository.findByNameAndUserIdAndGender(name, userId, gender);
    }

    @Override
    public long getNumberOfUserTeams(String userId) {
        return teamRepository.countByUserId(userId);
    }

    @Override
    public boolean createUserTeam(Team team) {
        final boolean created;

        if (getUserTeam(team.getUserId(), team.getName(), team.getGender()) == null) {
            teamRepository.insert(team);
            LOGGER.debug(String.format("Created team %s for user %s", team.getName(), team.getUserId()));
            created = true;
        } else {
            LOGGER.error(String.format("Could not create team %s for user %s because it already exists", team.getName(), team.getUserId()));
            created = false;
        }

        return created;
    }

    @Override
    public boolean updateUserTeam(Team team) {
        final boolean updated;

        Team savedTeam = getUserTeam(team.getUserId(), team.getName(), team.getGender());

        if (savedTeam == null) {
            LOGGER.error(String.format("Could not update team %s for user %s because it does not exist", team.getName(), team.getUserId()));
            updated = false;
        } else {
            savedTeam.setKind(team.getKind());
            savedTeam.setDate(team.getDate());
            savedTeam.setColor(team.getColor());
            savedTeam.setLiberoColor(team.getLiberoColor());
            savedTeam.setPlayers(team.getPlayers());
            savedTeam.setLiberos(team.getLiberos());
            savedTeam.setCaptain(team.getCaptain());
            savedTeam.setGender(team.getGender());
            teamRepository.save(savedTeam);
            LOGGER.debug(String.format("Updated team %s for user %s", team.getName(), team.getUserId()));

            for (GameDescription gameDescription : gameService.listGameDescriptionsUsingTeam(team.getName(), team.getUserId())) {
                updateUserGame(gameDescription);
            }

            updated = true;
        }

        return updated;
    }

    @Override
    public boolean deleteUserTeam(String userId, String name, String gender) {
        final boolean deleted;

        if (gameService.hasGameUsingTeam(name, userId)) {
            LOGGER.debug(String.format("Could not delete team %s for user %s because it is used in a game", name, userId));
            deleted = false;
        } else {
            teamRepository.deleteByNameAndUserIdAndGender(name, userId, gender);
            LOGGER.debug(String.format("Deleted team %s for user %s", name, userId));
            deleted = true;
        }

        return deleted;
    }

    @Override
    public List<GameDescription> listUserGames(String userId) {
        return gameDescriptionRepository.findByUserId(userId);
    }

    @Override
    public List<GameDescription> listAvailableUserGames(String userId) {
        return gameDescriptionRepository.findByUserIdAndAvailable(userId);
    }

    @Override
    public List<GameDescription> listUserGamesInLeague(String userId, String kind, String leagueName) {
        return gameDescriptionRepository.findByUserIdAndKindAndLeague(userId, kind, leagueName);
    }

    @Override
    public List<GameDescription> listUserGamesInLeague(long leagueDate) {
        League league = leagueRepository.findByDate(leagueDate);

        if (league == null) {
            return new ArrayList<>();
        } else {
            return gameDescriptionRepository.findByUserIdAndKindAndLeague(league.getUserId(), league.getKind(), league.getName());
        }
    }

    @Override
    public List<GameDescription> listUserGamesOfTeamInLeague(long leagueDate, String teamName, String teamGender) {
        League league = leagueRepository.findByDate(leagueDate);

        if (league == null) {
            return new ArrayList<>();
        } else {
            return gameDescriptionRepository.findByUserIdAndKindAndLeagueAndTeamNameAndGender(league.getUserId(), league.getKind(), league.getName(), teamName, teamGender);
        }
    }

    @Override
    public List<GameDescription> listLiveUserGamesInLeague(long leagueDate) {
        League league = leagueRepository.findByDate(leagueDate);

        if (league == null) {
            return new ArrayList<>();
        } else {
            return gameDescriptionRepository.findByAndUserIdAndKindAndStatusAndLeague(
                    league.getUserId(), league.getKind(), GameStatus.LIVE.toString(), league.getName());
        }
    }

    @Override
    public List<GameDescription> listLast10UserGamesInLeague(long leagueDate) {
        League league = leagueRepository.findByDate(leagueDate);

        if (league == null) {
            return new ArrayList<>();
        } else {
            return gameDescriptionRepository.findTop10ByUserIdAndKindAndStatusAndLeagueOrderByScheduleDesc(
                    league.getUserId(), league.getKind(), GameStatus.COMPLETED.toString(), league.getName());
        }
    }

    @Override
    public List<GameDescription> listNext10UserGamesInLeague(long leagueDate) {
        League league = leagueRepository.findByDate(leagueDate);

        if (league == null) {
            return new ArrayList<>();
        } else {
            return gameDescriptionRepository.findTop10ByUserIdAndKindAndStatusAndLeagueOrderByScheduleAsc(
                    league.getUserId(), league.getKind(), GameStatus.SCHEDULED.toString(), league.getName());
        }
    }

    @Override
    public GameDescription getUserGame(String userId, long date) {
        return gameDescriptionRepository.findByDateAndUserId(date, userId);
    }

    @Override
    public Game getUserGameFull(String userId, long date) {
        return gameRepository.findByDateAndUserId(date, userId);
    }

    @Override
    public long getNumberOfUserGames(String userId) {
        return gameDescriptionRepository.countByUserId(userId);
    }

    @Override
    public long getNumberOfUserGames(String userId, String kind, String leagueName) {
        return gameDescriptionRepository.countByUserIdAndKindAndLeague(userId, kind, leagueName);
    }

    @Override
    public boolean createUserGame(GameDescription gameDescription) {
        final boolean created;
        final String userId = gameDescription.getUserId();

        if (getUserGame(userId, gameDescription.getDate()) == null) {
            Code code = new Code();
            code.setDate(gameDescription.getDate());
            code.setCode(allocateUniqueCode());

            Team hTeam = getUserTeam(userId, gameDescription.gethName(), gameDescription.getGender());
            Team gTeam = getUserTeam(userId, gameDescription.getgName(), gameDescription.getGender());
            Rules rules = getRules(userId, gameDescription.getRules());

            if (hTeam == null || gTeam == null || rules == null || code.getCode() < 0) {
                LOGGER.error(String.format("Could not create game with date %d (%s vs %s) for user %s because at least one input was not found",
                        gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
                created = false;
            } else if (!gameDescription.getKind().equals(hTeam.getKind()) || !gameDescription.getKind().equals(gTeam.getKind())) {
                LOGGER.error(String.format("Could not create game with date %d (%s vs %s) for user %s because the game kind doesn't match with the team kinds",
                        gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
                created = false;
            } else {
                Game game = new Game();
                game.setUserId(userId);
                game.setKind(gameDescription.getKind());
                game.setDate(gameDescription.getDate());
                game.setSchedule(gameDescription.getSchedule());
                game.setGender(gameDescription.getGender());
                game.setUsage(gameDescription.getUsage());
                game.setStatus(gameDescription.getStatus());
                game.setReferee(gameDescription.getReferee());
                game.setLeague(gameDescription.getLeague());
                game.sethTeam(hTeam);
                game.setgTeam(gTeam);
                game.setRules(rules);
                game.sethSets(gameDescription.gethSets());
                game.setgSets(gameDescription.getgSets());
                game.setSets(new ArrayList<>());
                game.sethCards(new ArrayList<>());
                game.setgCards(new ArrayList<>());

                gameService.createGame(game);
                codeRepository.insert(code);
                LOGGER.debug(String.format("Created game with date %d (%s vs %s) for user %s",
                        gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
                created = true;
            }
        } else {
            LOGGER.error(String.format("Could not create game with date %d (%s vs %s) for user %s because it already exists",
                    gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
            created = false;
        }

        return created;
    }

    @Override
    public boolean updateUserGame(GameDescription gameDescription) {
        final boolean updated;

        final String userId = gameDescription.getUserId();
        final Game game = getUserGameFull(userId, gameDescription.getDate());

        if (game == null) {
            LOGGER.error(String.format("Could not update game with date %d (%s vs %s) for user %s because it does not exist",
                    gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
            updated = false;
        } else if (!GameStatus.SCHEDULED.toString().equals(game.getStatus())) {
            LOGGER.error(String.format("Could not update game with date %d (%s vs %s) for user %s because it is %s",
                    gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId, game.getStatus()));
            updated = false;
        } else {
            Team hTeam = getUserTeam(userId, gameDescription.gethName(), gameDescription.getGender());
            Team gTeam = getUserTeam(userId, gameDescription.getgName(), gameDescription.getGender());
            Rules rules = getRules(userId, gameDescription.getRules());

            if (hTeam == null || gTeam == null || rules == null) {
                LOGGER.error(String.format("Could not update game with date %d (%s vs %s) for user %s because at least one input was not found",
                        gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
                updated = false;
            } else if (!gameDescription.getKind().equals(hTeam.getKind()) || !gameDescription.getKind().equals(gTeam.getKind())) {
                LOGGER.error(String.format("Could not update game with date %d (%s vs %s) for user %s because the game kind doesn't match with the team kinds",
                        gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
                updated = false;
            } else {
                game.setKind(gameDescription.getKind());
                game.setSchedule(gameDescription.getSchedule());
                game.setGender(gameDescription.getGender());
                game.setUsage(gameDescription.getUsage());
                game.setStatus(gameDescription.getStatus());
                game.setReferee(gameDescription.getReferee());
                game.setLeague(gameDescription.getLeague());
                game.sethTeam(hTeam);
                game.setgTeam(gTeam);
                game.setRules(rules);
                game.sethSets(gameDescription.gethSets());
                game.setgSets(gameDescription.getgSets());
                game.setSets(new ArrayList<>());
                game.sethCards(new ArrayList<>());
                game.setgCards(new ArrayList<>());

                gameService.updateGame(game.getDate(), game);
                LOGGER.debug(String.format("Updated game with date %d (%s vs %s) for user %s",
                        gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
                updated = true;
            }
        }

        return updated;
    }

    @Override
    public boolean deleteUserGame(String userId, long date) {
        codeRepository.deleteByDate(date);
        gameService.deleteGame(date, userId);
        LOGGER.debug(String.format("Deleted game with date %d for user %s", date, userId));
        return true;
    }

    @Override
    public int getUserGameCode(String userId, long date) {
        final int code;

        if (gameDescriptionRepository.existsByDateAndUserId(date, userId)) {
            code = codeRepository.findByDate(date).getCode();
        } else {
            code = -1;
        }

        return code;
    }

    @Override
    public List<League> listUserLeagues(String userId) {
        return leagueRepository.findByUserId(userId);
    }

    @Override
    public List<League> listUserLeaguesOfKind(String userId, String kind) {
        return leagueRepository.findByUserIdAndKind(userId, kind);
    }

    @Override
    public League getUserLeague(long date) {
        return leagueRepository.findByDate(date);
    }

    @Override
    public League getUserLeague(String userId, long date) {
        return leagueRepository.findByDateAndUserId(date, userId);
    }

    @Override
    public League getUserLeague(String userId, String name) {
        return leagueRepository.findByNameAndUserId(name, userId);
    }

    @Override
    public long getNumberOfUserLeagues(String userId) {
        return leagueRepository.countByUserId(userId);
    }

    @Override
    public boolean createUserLeague(League league) {
        final boolean created;

        if (getUserLeague(league.getUserId(), league.getName()) == null) {
            leagueRepository.insert(league);
            LOGGER.debug(String.format("Created league %s for user %s", league.getName(), league.getUserId()));
            created = true;
        } else {
            LOGGER.error(String.format("Could not create league %s for user %s because it already exists", league.getName(), league.getUserId()));
            created = false;
        }

        return created;
    }

    @Override
    public boolean deleteUserLeague(String userId, long date) {
        leagueRepository.deleteByDateAndUserId(date, userId);
        LOGGER.debug(String.format("Deleted league with date %d for user %s", date, userId));
        return true;
    }

    private int allocateUniqueCode() {
        boolean exists = true;
        int code = -1;

        while (exists) {
            code = random.nextInt(99999999 - 10000000) + 10000000;
            exists = (codeRepository.findByCode(code) != null);
        }

        return code;
    }
}
