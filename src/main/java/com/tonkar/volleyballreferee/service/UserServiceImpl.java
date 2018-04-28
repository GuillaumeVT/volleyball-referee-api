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
    private CodeRepository codeRepository;

    @Autowired
    private GameService gameService;

    private final Random random = new Random();

    @Override
    public List<Rules> getUserRules(UserId userId) {
        return rulesRepository.findRulesByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());
    }

    @Override
    public List<Rules> getDefaultRules() {
        List<Rules> rules = new ArrayList<>();
        rules.add(Rules.OFFICIAL_INDOOR_RULES);
        rules.add(Rules.OFFICIAL_BEACH_RULES);
        rules.add(Rules.DEFAULT_INDOOR_4X4_RULES);
        return rules;
    }

    @Override
    public Rules getUserRules(UserId userId, String name) {
        return rulesRepository.findRulesByNameAndUserId_SocialIdAndUserId_Provider(name, userId.getSocialId(), userId.getProvider());
    }

    @Override
    public Rules getRules(UserId userId, String name) {
        Rules rules = getUserRules(userId, name);

        if (rules == null) {
            for (Rules defaultRules : getDefaultRules()) {
                if (defaultRules.getName().equals(name)) {
                    rules = defaultRules;
                }
            }
        }

        return rules;
    }

    @Override
    public long getNumberOfUserRules(UserId userId) {
        return rulesRepository.countRulesByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());
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
            savedRules.setChangeSidesEvery7Points(rules.isChangeSidesEvery7Points());
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
    public boolean deleteUserRules(UserId userId, String name) {
        final boolean deleted;

        if (gameService.hasGameUsingRules(name, userId)) {
            LOGGER.debug(String.format("Could not delete rules %s for user %s because they are used in a game", name, userId));
            deleted = false;
        } else {
            rulesRepository.deleteRulesByNameAndUserId_SocialIdAndUserId_Provider(name, userId.getSocialId(), userId.getProvider());
            LOGGER.debug(String.format("Deleted rules %s for user %s", name, userId));
            deleted = true;
        }

        return deleted;
    }

    @Override
    public List<Team> getUserTeams(UserId userId) {
        return teamRepository.findTeamsByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());
    }

    @Override
    public List<Team> getUserTeams(UserId userId, String kind) {
        return teamRepository.findTeamsByUserId_SocialIdAndUserId_ProviderAndKind(userId.getSocialId(), userId.getProvider(), kind);
    }

    @Override
    public Team getUserTeam(UserId userId, String name) {
        return teamRepository.findTeamByNameAndUserId_SocialIdAndUserId_Provider(name, userId.getSocialId(), userId.getProvider());
    }

    @Override
    public long getNumberOfUserTeams(UserId userId) {
        return teamRepository.countTeamsByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());
    }

    @Override
    public boolean createUserTeam(Team team) {
        final boolean created;

        if (getUserTeam(team.getUserId(), team.getName()) == null) {
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

        Team savedTeam = getUserTeam(team.getUserId(), team.getName());

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
    public boolean deleteUserTeam(UserId userId, String name) {
        final boolean deleted;

        if (gameService.hasGameUsingTeam(name, userId)) {
            LOGGER.debug(String.format("Could not delete team %s for user %s because it is used in a game", name, userId));
            deleted = false;
        } else {
            teamRepository.deleteTeamByNameAndUserId_SocialIdAndUserId_Provider(name, userId.getSocialId(), userId.getProvider());
            LOGGER.debug(String.format("Deleted team %s for user %s", name, userId));
            deleted = true;
        }

        return deleted;
    }

    @Override
    public List<GameDescription> getUserGames(UserId userId) {
        return gameDescriptionRepository.findGameDescriptionsByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());
    }

    @Override
    public GameDescription getUserGame(UserId userId, long date) {
        return gameDescriptionRepository.findGameDescriptionByDateAndUserId_SocialIdAndUserId_Provider(date, userId.getSocialId(), userId.getProvider());
    }

    @Override
    public long getNumberOfUserGames(UserId userId) {
        return gameDescriptionRepository.countByUserId_SocialIdAndUserId_Provider(userId.getSocialId(), userId.getProvider());
    }

    @Override
    public boolean createUserGame(GameDescription gameDescription) {
        final boolean created;
        final UserId userId = gameDescription.getUserId();

        if (getUserGame(userId, gameDescription.getDate()) == null) {
            Code code = new Code();
            code.setDate(gameDescription.getDate());
            code.setCode(allocateUniqueCode());

            Team hTeam = getUserTeam(userId, gameDescription.gethName());
            Team gTeam = getUserTeam(userId, gameDescription.getgName());
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

        final UserId userId = gameDescription.getUserId();
        final Game game = gameRepository.findGameByDateAndUserId_SocialIdAndUserId_Provider(gameDescription.getDate(), userId.getSocialId(), userId.getProvider());

        if (game == null) {
            LOGGER.error(String.format("Could not update game with date %d (%s vs %s) for user %s because it does not exist",
                    gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId));
            updated = false;
        } else if (!GameStatus.SCHEDULED.toString().equals(game.getStatus())) {
            LOGGER.error(String.format("Could not update game with date %d (%s vs %s) for user %s because it is %s",
                    gameDescription.getDate(), gameDescription.gethName(), gameDescription.getgName(), userId, game.getStatus()));
            updated = false;
        } else {
            Team hTeam = getUserTeam(userId, gameDescription.gethName());
            Team gTeam = getUserTeam(userId, gameDescription.getgName());
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
    public boolean deleteUserGame(UserId userId, long date) {
        codeRepository.deleteCodeByDate(date);
        gameService.deleteGame(date, userId);
        LOGGER.debug(String.format("Deleted game with date %d for user %s", date, userId));
        return true;
    }

    @Override
    public int getUserGameCode(UserId userId, long date) {
        final int code;

        if (gameDescriptionRepository.existsByDateAndUserId_SocialIdAndUserId_Provider(date, userId.getSocialId(), userId.getProvider())) {
            code = codeRepository.findCodeByDate(date).getCode();
        } else {
            code = -1;
        }

        return code;
    }

    private int allocateUniqueCode() {
        boolean exists = true;
        int code = -1;

        while (exists) {
            code = random.nextInt(99999999 - 10000000) + 10000000;
            exists = (codeRepository.findCodeByCode(code) != null);
        }

        return code;
    }
}
