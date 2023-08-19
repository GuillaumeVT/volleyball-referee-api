package com.tonkar.volleyballreferee.configuration;

import com.tonkar.volleyballreferee.dto.GameSummary;
import com.tonkar.volleyballreferee.dto.NewUser;
import com.tonkar.volleyballreferee.dto.UserToken;
import com.tonkar.volleyballreferee.entity.*;
import com.tonkar.volleyballreferee.service.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@TestConfiguration
public class VbrTestConfiguration {

    private final UserService   userService;
    private final FriendService friendService;
    private final RulesService  rulesService;
    private final TeamService   teamService;
    private final GameService   gameService;
    private final LeagueService leagueService;

    public VbrTestConfiguration(@Autowired UserService userService, @Autowired FriendService friendService,
                                @Autowired RulesService rulesService, @Autowired TeamService teamService,
                                @Autowired GameService gameService, @Autowired LeagueService leagueService) {
        this.userService = userService;
        this.friendService = friendService;
        this.rulesService = rulesService;
        this.teamService = teamService;
        this.gameService = gameService;
        this.leagueService = leagueService;
    }

    @Bean
    public Faker faker() {
        return new Faker(Locale.ENGLISH);
    }

    @Bean
    public Sandbox sandbox() {
        return new Sandbox();
    }

    public class Sandbox {

        private final Faker faker;

        public Sandbox() {
            faker = faker();
        }

        public NewUser generateNewUser(String email) {
            return new NewUser(UUID.randomUUID().toString(), faker.name().firstName(), email == null ? faker.internet().safeEmailAddress() : email, "Password1234+", faker.finance().iban());
        }

        public User generateUser(String email) {
            final var now = LocalDateTime.now();
            final var nowMillis = now.toInstant(ZoneOffset.UTC).toEpochMilli();
            final var expiryMillis = now.plusYears(50).toInstant(ZoneOffset.UTC).toEpochMilli();

            var user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setPseudo(faker.name().firstName());
            user.setEmail(email == null ? faker.internet().safeEmailAddress() : email);
            user.setPassword("Password1234+");
            user.setPurchaseToken(faker.finance().iban());
            user.setSubscriptionExpiryAt(expiryMillis);
            user.setFriends(new ArrayList<>());
            user.setCreatedAt(nowMillis);
            user.setLastLoginAt(nowMillis);
            user.setFailedAuthentication(new User.FailedAuthentication());

            return user;
        }

        public UserToken createUser() {
            return createUser(null);
        }

        public UserToken createUser(String email) {
            return userService.createUser(generateNewUser(email));
        }

        public User getUser(String userId) {
            return userService.getUser(userId);
        }

        public void addFriend(User user1, User user2) {
            UUID friendRequestId = friendService.sendFriendRequest(user1, user2.getPseudo());
            friendService.acceptFriendRequest(user2, friendRequestId);
        }

        public GameSummary createScheduledBeachGame(String userId) {
            GameSummary gameSummary = generateScheduledBeachGame(userId, true);
            gameService.createGame(getUser(userId), gameSummary);
            return gameSummary;
        }

        public GameSummary generateScheduledBeachGame(String userId, boolean createRequiredData) {
            var gameSummary = new GameSummary();
            var team1 = generateBeachTeam(userId);
            var team2 = generateBeachTeam(userId);
            var rules = generateBeachRules(userId);
            var league = generateSelectedBeachLeague(userId);

            if (createRequiredData) {
                var user = getUser(userId);
                rulesService.createRules(user, rules);
                teamService.createTeam(user, team1);
                teamService.createTeam(user, team2);
            }

            gameSummary.setId(UUID.randomUUID());
            gameSummary.setCreatedBy(userId);
            gameSummary.setCreatedAt(System.currentTimeMillis());
            gameSummary.setUpdatedAt(System.currentTimeMillis());
            gameSummary.setScheduledAt(System.currentTimeMillis());
            gameSummary.setRefereedBy(userId);
            gameSummary.setRefereeName(getUser(userId).getPseudo());
            gameSummary.setKind(GameType.BEACH);
            gameSummary.setGender(GenderType.LADIES);
            gameSummary.setUsage(UsageType.NORMAL);
            gameSummary.setStatus(GameStatus.SCHEDULED);
            gameSummary.setIndexed(true);
            gameSummary.setLeagueId(league.getId());
            gameSummary.setLeagueName(league.getName());
            gameSummary.setDivisionName(league.getDivision());
            gameSummary.setHomeTeamId(team1.getId());
            gameSummary.setHomeTeamName(team1.getName());
            gameSummary.setGuestTeamId(team2.getId());
            gameSummary.setGuestTeamName(team2.getName());
            gameSummary.setHomeSets(0);
            gameSummary.setGuestSets(0);
            gameSummary.setRulesId(rules.getId());
            gameSummary.setRulesName(rules.getName());
            gameSummary.setScore("");
            gameSummary.setReferee1Name(faker.name().fullName());
            gameSummary.setReferee2Name(faker.name().fullName());
            gameSummary.setScorerName(faker.name().fullName());

            return gameSummary;
        }

        public Game createBeachGame(String userId) {
            Game game = generateBeachGame(userId);
            gameService.createGame(getUser(userId), game);
            return game;
        }

        public Game generateBeachGame(String userId) {
            var game = new Game();
            var team1 = generateBeachTeam(userId);
            var team2 = generateBeachTeam(userId);
            var rules = generateBeachRules(userId);
            var league = generateSelectedBeachLeague(userId);

            game.setId(UUID.randomUUID());
            game.setCreatedBy(userId);
            game.setCreatedAt(System.currentTimeMillis());
            game.setUpdatedAt(System.currentTimeMillis());
            game.setScheduledAt(System.currentTimeMillis());
            game.setRefereedBy(userId);
            game.setRefereeName(getUser(userId).getPseudo());
            game.setKind(GameType.BEACH);
            game.setGender(GenderType.LADIES);
            game.setUsage(UsageType.NORMAL);
            game.setStatus(GameStatus.LIVE);
            game.setIndexed(true);
            game.setLeague(league);
            game.setHomeTeam(team1);
            game.setGuestTeam(team2);
            game.setHomeSets(0);
            game.setGuestSets(0);
            game.setRules(rules);
            game.setSets(new ArrayList<>());
            game.getSets().add(generateSet());
            game.setHomeCards(new ArrayList<>());
            game.setGuestCards(new ArrayList<>());
            game.setScore("");

            return game;
        }

        public Team createBeachTeam(String userId) {
            var team = generateBeachTeam(userId);
            teamService.createTeam(getUser(userId), team);
            return team;
        }

        public Team generateBeachTeam(String userId) {
            var team = new Team();
            team.setId(UUID.randomUUID());
            team.setCreatedBy(userId);
            team.setCreatedAt(System.currentTimeMillis());
            team.setUpdatedAt(System.currentTimeMillis());
            team.setName(faker.team().name());
            team.setKind(GameType.BEACH);
            team.setGender(GenderType.LADIES);
            team.setColor(faker.color().hex());
            team.setLiberoColor(faker.color().hex());
            team.setPlayers(new ArrayList<>());
            team.getPlayers().add(new Team.Player(1, faker.name().fullName()));
            team.getPlayers().add(new Team.Player(2, faker.name().fullName()));
            team.setLiberos(new ArrayList<>());
            team.setCaptain(1);
            return team;
        }

        public Rules generateBeachRules(String userId) {
            return new Rules(UUID.randomUUID(), userId, 0L, 0L, faker.company().buzzword(), GameType.BEACH,
                    3, 21, true, 15, true, true, Rules.WIN_TERMINATION, true, 1, 30,
                    true, 30, true, 60,
                    Rules.FIVB_LIMITATION, 0, true, 7, 5, 9999);
        }

        public Rules generateIndoorRules(String userId) {
            return new Rules(UUID.randomUUID(), userId, 0L, 0L, faker.company().buzzword(), GameType.INDOOR,
                    5, 25, true, 15, true, true, Rules.WIN_TERMINATION, true, 2, 30,
                    false, 60, true, 180,
                    Rules.NO_LIMITATION, 6, false, 0, 0, 9999);
        }

        public Rules createIndoorRules(String userId) {
            var rules = generateIndoorRules(userId);
            rulesService.createRules(getUser(userId), rules);
            return rules;
        }

        public Game.SelectedLeague generateSelectedBeachLeague(String userId) {
            return generateSelectedLeague(userId, GameType.BEACH);
        }

        public Game.SelectedLeague generateSelectedLeague(String userId, GameType kind) {
            var selectedLeague = new Game.SelectedLeague();
            selectedLeague.setId(UUID.randomUUID());
            selectedLeague.setCreatedBy(userId);
            selectedLeague.setCreatedAt(System.currentTimeMillis());
            selectedLeague.setUpdatedAt(System.currentTimeMillis());
            selectedLeague.setName(faker.country().name());
            selectedLeague.setKind(kind);
            selectedLeague.setDivision(faker.country().capital());
            return selectedLeague;
        }

        public League createLeague(String userId, GameType kind) {
            var league = generateLeague(userId, kind);
            leagueService.createLeague(getUser(userId), league);
            return league;
        }

        public League generateLeague(String userId, GameType kind) {
            var league = new League();
            league.setId(UUID.randomUUID());
            league.setCreatedBy(userId);
            league.setCreatedAt(System.currentTimeMillis());
            league.setUpdatedAt(System.currentTimeMillis());
            league.setName(faker.country().name());
            league.setKind(kind);
            league.setDivisions(List.of(faker.country().capital(), faker.country().capital()));
            return league;
        }

        public Set generateSet() {
            var set = new Set();

            set.setLadder(new ArrayList<>());
            set.setServing("H");
            set.setFirstServing("H");
            set.setHomeSubstitutions(new ArrayList<>());
            set.setGuestSubstitutions(new ArrayList<>());
            set.setHomeCalledTimeouts(new ArrayList<>());
            set.setGuestCalledTimeouts(new ArrayList<>());

            return set;
        }
    }
}
