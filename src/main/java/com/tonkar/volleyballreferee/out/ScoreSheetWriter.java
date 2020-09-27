package com.tonkar.volleyballreferee.out;

import com.tonkar.volleyballreferee.entity.FileWrapper;
import com.tonkar.volleyballreferee.entity.Game;
import com.tonkar.volleyballreferee.entity.Set;
import com.tonkar.volleyballreferee.entity.TeamType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
public class ScoreSheetWriter {

    private final Game       game;
    private final Document   document;
    private final Element    body;
    private final DateFormat dateFormatter;
    private final DateFormat timeFormatter;

    public static FileWrapper createScoreSheet(Game game) {
        DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        String date = formatter.format(new Date(game.getScheduledAt()));

        String homeTeam = game.getHomeTeam().getName();
        String guestTeam = game.getGuestTeam().getName();

        String filename = String.format(Locale.getDefault(), "%s__%s__%s.html", homeTeam, guestTeam, date);
        filename = filename.replaceAll("[\\s|\\?\\*<:>\\+\\[\\]/\\']", "_");
        ScoreSheetWriter scoreSheetWriter = new ScoreSheetWriter(game, filename);

        switch (game.getKind()) {
            case INDOOR -> scoreSheetWriter.createStoredIndoorGame();
            case BEACH -> scoreSheetWriter.createStoredBeachGame();
            case INDOOR_4X4 -> scoreSheetWriter.createStoredIndoor4x4Game();
            case SNOW -> scoreSheetWriter.createStoredSnowGame();
        }

        return new FileWrapper(filename, scoreSheetWriter.getScoreSheet());
    }

    private ScoreSheetWriter(Game game, String filename) {
        this.game = game;
        this.document = Jsoup.parse(htmlSkeleton(filename), "UTF-8");
        this.body = document.body();
        this.dateFormatter = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        this.dateFormatter.setTimeZone(TimeZone.getDefault());
        this.timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        this.timeFormatter.setTimeZone(TimeZone.getDefault());
    }

    private byte[] getScoreSheet() {
        return document.toString().getBytes();
    }

    private void createStoredIndoorGame() {
        body.appendChild(createStoredGameHeader());
        body.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < game.getSets().size(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (setIndex %2 == 1
                    || (setIndex == 0 && (game.getPlayers(TeamType.HOME).size() > 14 || game.getPlayers(TeamType.GUEST).size() > 14))
                    || game.getSubstitutions(TeamType.HOME, setIndex).size() > 6
                    || game.getSubstitutions(TeamType.GUEST, setIndex).size() > 6
                    || (game.getPoints(TeamType.HOME, setIndex) + game.getPoints(TeamType.GUEST, setIndex) > 64)) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            Element line2Div = new Element("div");
            line2Div.addClass("div-flex-row");
            line2Div.appendChild(createStoredStartingLineup(setIndex)).appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredSubstitutions(setIndex));
            if (game.getRules().isTeamTimeouts()) {
                line2Div.appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredTimeouts(setIndex));
            }
            cardDiv.appendChild(line2Div);

            cardDiv.appendChild(createStoredLadder(setIndex));

            body.appendChild(cardDiv);
        }

        body.appendChild(createFooter());
    }

    private Element createStoredGameHeader() {
        Element cardDiv = new Element("div");
        cardDiv.addClass("div-card");

        Element gameInfoDiv = new Element("div");
        gameInfoDiv.addClass("div-grid-game-header-info");

        Game.SelectedLeague selectedLeague = game.getLeague();
        String league = selectedLeague == null ? "" : selectedLeague.getName() + " / " + selectedLeague.getDivision();

        gameInfoDiv.appendChild(createCellSpan(league, true, false));
        gameInfoDiv.appendChild(createCellSpan(dateFormatter.format(new Date(game.getStartTime())), true, false));

        String startEndTimes = String.format("%s \u2192 %s", timeFormatter.format(new Date(game.getStartTime())), timeFormatter.format(new Date(game.getEndTime())));
        gameInfoDiv.appendChild(createCellSpan(startEndTimes, true, false));

        int duration = (int) Math.ceil((game.getEndTime() - game.getStartTime()) / 60000.0);
        gameInfoDiv.appendChild(createCellSpan(String.format(Locale.getDefault(), "%d min", duration), true, false));

        cardDiv.appendChild(gameInfoDiv);

        Element homeSetsInfoDiv = new Element("div");
        homeSetsInfoDiv.addClass("div-grid-sets-info");

        Element homeTeamNameSpan = createCellSpan(game.getHomeTeam().getName(), true, false);
        homeTeamNameSpan.addClass("vbr-home-team");
        homeSetsInfoDiv.appendChild(homeTeamNameSpan);

        homeSetsInfoDiv.appendChild(createCellSpan(String.valueOf(game.getHomeSets()), true, false));

        int homePointsTotal = 0;
        int setIndex = 0;
        for (Set set : game.getSets()) {
            int points = set.getHomePoints();
            homePointsTotal += points;
            homeSetsInfoDiv.appendChild(createSetCellAnchor(String.valueOf(points), setIndex));
            setIndex++;
        }

        homeSetsInfoDiv.appendChild(createCellSpan(String.valueOf(homePointsTotal), true, false));

        cardDiv.appendChild(homeSetsInfoDiv);

        Element guestSetsInfoDiv = new Element("div");
        guestSetsInfoDiv.addClass("div-grid-sets-info");

        Element guestTeamNameSpan = createCellSpan(game.getGuestTeam().getName(), true, false);
        guestTeamNameSpan.addClass("vbr-guest-team");
        guestSetsInfoDiv.appendChild(guestTeamNameSpan);

        guestSetsInfoDiv.appendChild(createCellSpan(String.valueOf(game.getGuestSets()), true, false));

        int guestPointsTotal = 0;
        setIndex = 0;
        for (Set set : game.getSets()) {
            int points = set.getGuestPoints();
            guestPointsTotal += points;
            guestSetsInfoDiv.appendChild(createSetCellAnchor(String.valueOf(points), setIndex));
            setIndex++;
        }

        guestSetsInfoDiv.appendChild(createCellSpan(String.valueOf(guestPointsTotal), true, false));

        cardDiv.appendChild(guestSetsInfoDiv);

        return cardDiv;
    }

    private Element createStoredTeams() {
        Element cardDiv = new Element("div");
        cardDiv.addClass("div-card").addClass("spacing-before");

        cardDiv.appendChild(createTitleDiv("Players"));

        Element teamsDiv = new Element("div");
        teamsDiv.addClass("div-grid-h-g");
        teamsDiv.appendChild(createTeamDiv(TeamType.HOME)).appendChild(createSpacingDiv()).appendChild(createTeamDiv(TeamType.GUEST));
        cardDiv.appendChild(teamsDiv);

        return cardDiv;
    }

    private Element createTeamDiv(TeamType teamType) {
        Element teamDiv = new Element("div");
        teamDiv.addClass("div-grid-team");

        game.getPlayers(teamType).forEach(player -> {
            teamDiv.appendChild(createPlayerSpan(teamType, player.getNum(), false));
            teamDiv.appendChild(createCellSpan(player.getName(), false, false));
        });
        game.getLiberos(teamType).forEach(player -> {
            teamDiv.appendChild(createPlayerSpan(teamType, player.getNum(), true));
            teamDiv.appendChild(createCellSpan(player.getName(), false, false));
        });

        return teamDiv;
    }

    private Element createTitleDiv(String title) {
        Element titleDiv = new Element("div");
        titleDiv.addClass("div-title");
        titleDiv.appendChild(createCellSpan(title, false, false));
        return titleDiv;
    }

    private Element createSetCellAnchor(String text, int setIndex) {
        Element anchor = new Element("a");
        anchor.addClass("bordered-cell").addClass("set-anchor");
        anchor.attr("href", String.format(Locale.getDefault(), "#div-set-%d", (1 + setIndex)));
        anchor.appendText(text);
        return anchor;
    }

    private Element createCellSpan(String text, boolean withBorder, boolean isBadge) {
        Element span = new Element("span");
        span.addClass(isBadge ? "badge" : (withBorder ? "bordered-cell" : "cell"));
        span.appendText(text);
        return span;
    }

    private Element createPlayerSpan(TeamType teamType, int player, boolean isLibero) {
        String playerStr = String.valueOf(player);

        if (player < 0) {
            playerStr = "-";
        } else if (player == 100) {
            playerStr = "C";
        } else if (player == 200) {
            playerStr = "T";
        }

        Element playerSpan = createCellSpan(playerStr, false, true);

        if (isLibero) {
            playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-libero" : "vbr-guest-libero");
        } else {
            if (game.getCaptain(teamType) == player) {
                playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-captain" : "vbr-guest-captain");
            } else {
                playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-team" : "vbr-guest-team");
            }
        }

        return playerSpan;
    }

    private Element createEmptyPlayerSpan(TeamType teamType) {
        Element playerSpan = createCellSpan("-", false, true);
        playerSpan.addClass(TeamType.HOME.equals(teamType) ? "vbr-home-team" : "vbr-guest-team");
        return playerSpan;
    }

    private Element createStoredSetHeader(int setIndex) {
        Set set = game.getSets().get(setIndex);

        Element setHeaderDiv = new Element("div");
        setHeaderDiv.addClass("div-flex-row");

        Element setInfoDiv = new Element("div");
        setInfoDiv.addClass("div-grid-set-header-info");

        Element indexSpan = createCellSpan(String.format(Locale.getDefault(), "Set %d", (setIndex + 1)), true, false);
        indexSpan.addClass("set-index-cell").addClass((set.getHomePoints() > set.getGuestPoints()) ? "vbr-home-team" : "vbr-guest-team");
        setInfoDiv.appendChild(indexSpan);

        setInfoDiv.appendChild(createCellSpan(String.valueOf(set.getHomePoints()), true, false));
        setInfoDiv.appendChild(createCellSpan(String.valueOf(set.getGuestPoints()), true, false));

        setHeaderDiv.appendChild(setInfoDiv);

        setHeaderDiv.appendChild(createSpacingDiv());
        setHeaderDiv.appendChild(createSpacingDiv());

        Element setTimeDiv = new Element("div");
        setTimeDiv.addClass("div-grid-set-header-time");

        String startEndTimes = String.format("%s \u2192 %s", timeFormatter.format(new Date(set.getStartTime())), timeFormatter.format(new Date(set.getEndTime())));
        int duration = (int) Math.ceil(set.getDuration() / 60000.0);

        setTimeDiv.appendChild(createCellSpan(startEndTimes, true, false));
        setTimeDiv.appendChild(createCellSpan(String.format(Locale.getDefault(), "%d min", duration), true, false));

        setHeaderDiv.appendChild(setTimeDiv);

        setHeaderDiv.appendChild(createSpacingDiv());
        setHeaderDiv.appendChild(createSpacingDiv());

        if (game.getRules().isSanctions()) {
            setHeaderDiv.appendChild(createStoredSanctions(setIndex));
        }

        return setHeaderDiv;
    }

    private Element createStoredStartingLineup(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Starting line-up").addClass("spacing-before"));

        Element lineupsDiv = new Element("div");
        lineupsDiv.addClass("div-grid-h-g");
        lineupsDiv.appendChild(createLineupDiv(TeamType.HOME, setIndex)).appendChild(createEmptyDiv()).appendChild(createLineupDiv(TeamType.GUEST, setIndex));
        wrapperDiv.appendChild(lineupsDiv);

        return wrapperDiv;
    }

    private Element createLineupDiv(TeamType teamType, int setIndex) {
        Element lineupDiv = new Element("div");
        lineupDiv.addClass("div-grid-lineup").addClass("border");

        if (game.isStartingLineupConfirmed(teamType, setIndex)) {
            Set.Court lineup = game.getStartingLineup(teamType, setIndex);

            lineupDiv.appendChild(createCellSpan("IV", false, false));
            lineupDiv.appendChild(createCellSpan("III", false, false));
            lineupDiv.appendChild(createCellSpan("II", false, false));

            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP4(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP3(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP2(), false));

            lineupDiv.appendChild(createCellSpan("V", false, false));
            lineupDiv.appendChild(createCellSpan("VI", false, false));
            lineupDiv.appendChild(createCellSpan("I", false, false));

            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP5(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP6(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP1(), false));
        } else {
            lineupDiv.appendChild(createCellSpan("IV", false, false));
            lineupDiv.appendChild(createCellSpan("III", false, false));
            lineupDiv.appendChild(createCellSpan("II", false, false));

            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));

            lineupDiv.appendChild(createCellSpan("V", false, false));
            lineupDiv.appendChild(createCellSpan("VI", false, false));
            lineupDiv.appendChild(createCellSpan("I", false, false));

            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
        }

        return lineupDiv;
    }

    private Element createStoredSubstitutions(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Substitutions").addClass("spacing-before"));

        Element substitutionsDiv = new Element("div");
        substitutionsDiv.addClass("div-grid-h-g");

        substitutionsDiv.appendChild(createSubstitutionsDiv(TeamType.HOME, setIndex)).appendChild(createEmptyDiv()).appendChild(createSubstitutionsDiv(TeamType.GUEST, setIndex));

        wrapperDiv.appendChild(substitutionsDiv);

        return wrapperDiv;
    }

    private Element createSubstitutionsDiv(TeamType teamType, int setIndex) {
        Element substitutionsDiv = new Element("div");
        substitutionsDiv.addClass("div-flex-column");

        for (Set.Substitution substitution : game.getSubstitutions(teamType, setIndex)) {
            substitutionsDiv.appendChild(createSubstitutionDiv(teamType, substitution));
        }

        return substitutionsDiv;
    }

    private Element createSubstitutionDiv(TeamType teamType, Set.Substitution substitution) {
        Element substitutionDiv = new Element("div");
        substitutionDiv.addClass("div-grid-substitution");

        String score = String.format(Locale.getDefault(), "%d-%d",
                TeamType.HOME.equals(teamType) ? substitution.getHomePoints() : substitution.getGuestPoints(),
                TeamType.HOME.equals(teamType) ? substitution.getGuestPoints() : substitution.getHomePoints());

        substitutionDiv.appendChild(createPlayerSpan(teamType, substitution.getPlayerIn(), false));
        substitutionDiv.appendChild(new Element("div").addClass("substitution-image"));
        substitutionDiv.appendChild(createPlayerSpan(teamType, substitution.getPlayerOut(), false));
        substitutionDiv.appendChild(createCellSpan(score, false, false));

        return substitutionDiv;
    }

    private Element createStoredTimeouts(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Timeouts").addClass("spacing-before"));

        Element timeoutsDiv = new Element("div");
        timeoutsDiv.addClass("div-grid-h-g");
        timeoutsDiv.appendChild(createTimeoutsDiv(TeamType.HOME, setIndex)).appendChild(createEmptyDiv()).appendChild(createTimeoutsDiv(TeamType.GUEST, setIndex));

        wrapperDiv.appendChild(timeoutsDiv);

        return wrapperDiv;
    }

    private Element createTimeoutsDiv(TeamType teamType, int setIndex) {
        Element timeoutsDiv = new Element("div");
        timeoutsDiv.addClass("div-flex-column");

        for (Set.Timeout timeout : game.getCalledTimeouts(teamType, setIndex)) {
            timeoutsDiv.appendChild(createTimeoutDiv(teamType, timeout));
        }

        return timeoutsDiv;
    }

    private Element createTimeoutDiv(TeamType teamType, Set.Timeout timeout) {
        Element timeoutDiv = new Element("div");
        timeoutDiv.addClass("div-grid-timeout");

        String score = String.format(Locale.getDefault(), "%d-%d",
                TeamType.HOME.equals(teamType) ? timeout.getHomePoints() : timeout.getGuestPoints(),
                TeamType.HOME.equals(teamType) ? timeout.getGuestPoints() : timeout.getHomePoints());

        timeoutDiv.appendChild(createPlayerSpan(teamType, -1, false).addClass(getTimeoutImageClass(game.getTeamColor(teamType))));
        timeoutDiv.appendChild(createCellSpan(score, false, false));

        return timeoutDiv;
    }

    private String getTimeoutImageClass(String backgroundColor) {
        Color color = Color.decode(backgroundColor);
        String imageClass;

        double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

        if (a < 0.5) {
            imageClass = "timeout-gray-image";
        } else {
            imageClass = "timeout-white-image";
        }

        return imageClass;
    }

    private Element createStoredSanctions(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Sanctions"));

        Element sanctionsDiv = new Element("div");
        sanctionsDiv.addClass("div-grid-h-g");
        sanctionsDiv.appendChild(createSanctionsDiv(TeamType.HOME, setIndex)).appendChild(createEmptyDiv()).appendChild(createSanctionsDiv(TeamType.GUEST, setIndex));

        wrapperDiv.appendChild(sanctionsDiv);

        return wrapperDiv;
    }

    private Element createSanctionsDiv(TeamType teamType, int setIndex) {
        Element sanctionsDiv = new Element("div");
        sanctionsDiv.addClass("div-flex-column");

        for (Game.Sanction sanction : game.getGivenSanctions(teamType, setIndex)) {
            sanctionsDiv.appendChild(createSanctionDiv(teamType, sanction));
        }

        return sanctionsDiv;
    }

    private Element createSanctionDiv(TeamType teamType, Game.Sanction sanction) {
        Element sanctionDiv = new Element("div");
        sanctionDiv.addClass("div-grid-sanction");

        int player = sanction.getNum();

        String score = String.format(Locale.getDefault(), "%d-%d",
                TeamType.HOME.equals(teamType) ? sanction.getHomePoints() : sanction.getGuestPoints(),
                TeamType.HOME.equals(teamType) ? sanction.getGuestPoints() : sanction.getHomePoints());

        sanctionDiv.appendChild(new Element("div").addClass(getSanctionImageClass(sanction.getCard())));
        sanctionDiv.appendChild(createPlayerSpan(teamType, player, game.isLibero(teamType, player)));
        sanctionDiv.appendChild(createCellSpan(score, false, false));

        return sanctionDiv;
    }

    private String getSanctionImageClass(String sanctionType) {
        return switch (sanctionType) {
            case "Y" -> "yellow-card-image";
            case "R" -> "red-card-image";
            case "RE" -> "expulsion-card-image";
            case "RD" -> "disqualification-card-image";
            case "DW" -> "delay-warning-image";
            default -> "delay-penalty-image";
        };
    }

    private Element createStoredLadder(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Points").addClass("spacing-before"));

        int homeScore = 0;
        int guestScore = 0;

        Element ladderDiv = new Element("div");
        ladderDiv.addClass("div-flex-row");

        ladderDiv.appendChild(createLadderItem("H".equals(game.getSets().get(setIndex).getFirstServing()) ? TeamType.HOME : TeamType.GUEST, "S."));

        for (String teamType : game.getSets().get(setIndex).getLadder()) {
            if ("H".equals(teamType)) {
                homeScore++;
                ladderDiv.appendChild(createLadderItem(TeamType.HOME, String.valueOf(homeScore)));
            } else {
                guestScore++;
                ladderDiv.appendChild(createLadderItem(TeamType.GUEST, String.valueOf(guestScore)));
            }
        }

        wrapperDiv.appendChild(ladderDiv);

        return wrapperDiv;
    }

    private Element createLadderItem(TeamType teamType, String content) {
        Element ladderItemDiv = new Element("div");
        ladderItemDiv.addClass("div-flex-column").addClass("ladder-spacing");

        if (TeamType.HOME.equals(teamType)) {
            ladderItemDiv.appendChild(createCellSpan(content, false, true).addClass("vbr-home-team"));
            ladderItemDiv.appendChild(createCellSpan(" ", false, true));
        } else {
            ladderItemDiv.appendChild(createCellSpan(" ", false, true));
            ladderItemDiv.appendChild(createCellSpan(content, false, true).addClass("vbr-guest-team"));
        }

        return ladderItemDiv;
    }

    private Element createFooter() {
        Element div = new Element("div");
        div.addClass("div-footer");
        div.appendText("Powered by Volleyball Referee");

        Element vbrLogo = new Element("div");
        vbrLogo.addClass("vbr-logo-image");
        div.appendChild(vbrLogo);

        return div;
    }

    private void createStoredIndoor4x4Game() {
        body.appendChild(createStoredGameHeader());
        body.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < game.getSets().size(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (setIndex %2 == 1
                    || (setIndex == 0 && (game.getPlayers(TeamType.HOME).size() > 14 || game.getPlayers(TeamType.GUEST).size() > 14))
                    || game.getSubstitutions(TeamType.HOME, setIndex).size() > 6
                    || game.getSubstitutions(TeamType.GUEST, setIndex).size() > 6
                    || (game.getPoints(TeamType.HOME, setIndex) + game.getPoints(TeamType.GUEST, setIndex) > 64)) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            Element line2Div = new Element("div");
            line2Div.addClass("div-flex-row");
            line2Div.appendChild(createStoredStartingLineup4x4(setIndex)).appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredSubstitutions(setIndex));
            if (game.getRules().isTeamTimeouts()) {
                line2Div.appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredTimeouts(setIndex));
            }
            cardDiv.appendChild(line2Div);

            cardDiv.appendChild(createStoredLadder(setIndex));

            body.appendChild(cardDiv);
        }

        body.appendChild(createFooter());
    }

    private Element createStoredStartingLineup4x4(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Starting line-up").addClass("spacing-before"));

        Element lineupsDiv = new Element("div");
        lineupsDiv.addClass("div-grid-h-g");
        lineupsDiv.appendChild(createLineupDiv4x4(TeamType.HOME, setIndex)).appendChild(createEmptyDiv()).appendChild(createLineupDiv4x4(TeamType.GUEST, setIndex));
        wrapperDiv.appendChild(lineupsDiv);

        return wrapperDiv;
    }

    private Element createLineupDiv4x4(TeamType teamType, int setIndex) {
        Element lineupDiv = new Element("div");
        lineupDiv.addClass("div-grid-lineup").addClass("border");

        if (game.isStartingLineupConfirmed(teamType, setIndex)) {
            Set.Court lineup = game.getStartingLineup(teamType, setIndex);

            lineupDiv.appendChild(createCellSpan("IV", false, false));
            lineupDiv.appendChild(createCellSpan("III", false, false));
            lineupDiv.appendChild(createCellSpan("II", false, false));

            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP4(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP3(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP2(), false));

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createCellSpan("I", false, false));
            lineupDiv.appendChild(createEmptyDiv());

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP1(), false));
            lineupDiv.appendChild(createEmptyDiv());
        } else {
            lineupDiv.appendChild(createCellSpan("IV", false, false));
            lineupDiv.appendChild(createCellSpan("III", false, false));
            lineupDiv.appendChild(createCellSpan("II", false, false));

            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createCellSpan("I", false, false));
            lineupDiv.appendChild(createEmptyDiv());

            lineupDiv.appendChild(createEmptyDiv());
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyDiv());
        }

        return lineupDiv;
    }

    private void createStoredBeachGame() {
        body.appendChild(createStoredGameHeader());
        body.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < game.getSets().size(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (game.getSets().size() > 2 && setIndex % 2 == 1) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            if (game.getRules().isTeamTimeouts()) {
                Element timeoutDiv = new Element("div");
                timeoutDiv.addClass("div-flex-row");
                timeoutDiv.appendChild(createStoredTimeouts(setIndex));
                cardDiv.appendChild(timeoutDiv);
            }

            cardDiv.appendChild(createStoredLadder(setIndex));

            body.appendChild(cardDiv);
        }

        body.appendChild(createFooter());
    }

    private void createStoredSnowGame() {
        body.appendChild(createStoredGameHeader());
        body.appendChild(createStoredTeams());

        for (int setIndex = 0; setIndex < game.getSets().size(); setIndex++) {
            Element cardDiv = new Element("div");
            cardDiv.addClass("div-card").addClass("spacing-before");
            if (game.getSets().size() > 2 && setIndex %2 == 1) {
                cardDiv.addClass("new-page-for-printers");
            }
            cardDiv.attr("id", String.format(Locale.getDefault(), "div-set-%d", (1 + setIndex)));
            cardDiv.appendChild(createStoredSetHeader(setIndex));

            Element line2Div = new Element("div");
            line2Div.addClass("div-flex-row");
            line2Div.appendChild(createStoredStartingLineupSnow(setIndex)).appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredSubstitutions(setIndex));
            if (game.getRules().isTeamTimeouts()) {
                line2Div.appendChild(createSpacingDiv()).appendChild(createSpacingDiv()).appendChild(createStoredTimeouts(setIndex));
            }
            cardDiv.appendChild(line2Div);

            cardDiv.appendChild(createStoredLadder(setIndex));

            body.appendChild(cardDiv);
        }

        body.appendChild(createFooter());
    }

    private Element createStoredStartingLineupSnow(int setIndex) {
        Element wrapperDiv = new Element("div");

        wrapperDiv.appendChild(createTitleDiv("Starting line-up").addClass("spacing-before"));

        Element lineupsDiv = new Element("div");
        lineupsDiv.addClass("div-grid-h-g");
        lineupsDiv.appendChild(createLineupDivSnow(TeamType.HOME, setIndex)).appendChild(createEmptyDiv()).appendChild(createLineupDivSnow(TeamType.GUEST, setIndex));
        wrapperDiv.appendChild(lineupsDiv);

        return wrapperDiv;
    }

    private Element createLineupDivSnow(TeamType teamType, int setIndex) {
        Element lineupDiv = new Element("div");
        lineupDiv.addClass("div-grid-lineup").addClass("border");

        lineupDiv.appendChild(createCellSpan("I", false, false));
        lineupDiv.appendChild(createCellSpan("II", false, false));
        lineupDiv.appendChild(createCellSpan("III", false, false));

        if (game.isStartingLineupConfirmed(teamType, setIndex)) {
            Set.Court lineup = game.getStartingLineup(teamType, setIndex);

            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP1(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP2(), false));
            lineupDiv.appendChild(createPlayerSpan(teamType, lineup.getP3(), false));
        } else {
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
            lineupDiv.appendChild(createEmptyPlayerSpan(teamType));
        }

        return lineupDiv;
    }

    private Element createSpacingDiv() {
        Element div = new Element("div");
        div.addClass("horizontal-spacing");
        return div;
    }

    private Element createEmptyDiv() {
        return new Element("div");
    }

    private String toBase64(String imageFilename) {
        String base64Image;

        try {
            byte[] imageData = getClass().getResourceAsStream(String.format("/images/%s", imageFilename)).readAllBytes();
            base64Image = Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            base64Image = "";
            log.error(String.format("Could not encode the %s into base 64", imageFilename), e);
        }

        return base64Image;
    }

    private String getTextColor(String backgroundColor) {
        Color color = Color.decode(backgroundColor);
        String textColor;

        double a = 1 - ( 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

        if (a < 0.5) {
            textColor = "#1f1f1f";
        } else {
            textColor = "#ffffff";
        }

        return textColor;
    }

    private String htmlSkeleton(String title) {
        String homeTeamBackgroundColor = game.getTeamColor(TeamType.HOME);
        String homeLiberoBackgroundColor = game.getLiberoColor(TeamType.HOME);
        String guestTeamBackgroundColor = game.getTeamColor(TeamType.GUEST);
        String guestLiberoBackgroundColor = game.getLiberoColor(TeamType.GUEST);

        if (homeTeamBackgroundColor.equals(guestTeamBackgroundColor)) {
            guestTeamBackgroundColor = "#d6d7d7";
        }

        String homeTeamColor = getTextColor(homeTeamBackgroundColor);
        String homeLiberoColor = getTextColor(homeLiberoBackgroundColor);

        String guestTeamColor = getTextColor(guestTeamBackgroundColor);
        String guestLiberoColor = getTextColor(guestLiberoBackgroundColor);

        return "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">" +
                "    <title>" + title + "</title>" +
                "    <link href=\"https://fonts.googleapis.com/css?family=Roboto:400,700\" rel=\"stylesheet\">" +
                "    <meta name=\"theme-color\" content=\"#1f4294\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "    <link rel=\"icon\" type=\"image/x-icon\" href=\"data:image/x-icon;base64,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\">\n" +
                "    <style>\n" +
                "    html * {\n" +
                "      font-family: 'Roboto', sans-serif;\n" +
                "      font-size: 12px !important;\n" +
                "    }\n" +
                "    .vbr-body {\n" +
                "      width: 28cm;\n" +
                "      max-width: 28cm;\n" +
                "      margin-left: auto;\n" +
                "      margin-right: auto;\n" +
                "    }\n" +
                "    .vbr-home-team {\n" +
                String.format("      color: %s;\n", homeTeamColor) +
                String.format("      background-color: %s;\n", homeTeamBackgroundColor) +
                "    }\n" +
                "    .vbr-home-captain {\n" +
                String.format("      color: %s;\n", homeTeamColor) +
                String.format("      background-color: %s;\n", homeTeamBackgroundColor) +
                "      text-decoration: underline;\n" +
                "    }\n" +
                "    .vbr-home-libero {\n" +
                String.format("      color: %s;\n", homeLiberoColor) +
                String.format("      background-color: %s;\n", homeLiberoBackgroundColor) +
                "    }\n" +
                "    .vbr-guest-team {\n" +
                String.format("      color: %s;\n", guestTeamColor) +
                String.format("      background-color: %s;\n", guestTeamBackgroundColor) +
                "    }\n" +
                "    .vbr-guest-captain {\n" +
                String.format("      color: %s;\n", guestTeamColor) +
                String.format("      background-color: %s;\n", guestTeamBackgroundColor) +
                "      text-decoration: underline;\n" +
                "    }\n" +
                "    .vbr-guest-libero {\n" +
                String.format("      color: %s;\n", guestLiberoColor) +
                String.format("      background-color: %s;\n", guestLiberoBackgroundColor) +
                "    }\n" +
                "    .div-card {\n" +
                "      background-color: #ecebec;\n" +
                "      padding: 6px;\n" +
                "      margin: 6px;\n" +
                "      box-shadow: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24);\n" +
                "    }\n" +
                "    .div-title {\n" +
                "      font-weight: 700;\n" +
                "      margin-bottom: 6px;\n" +
                "    }\n" +
                "    .div-grid-h-g {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 4fr 1fr 4fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: stretch;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-grid-game-header-info {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 40fr 20fr 20fr 8fr 12fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: stretch;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-grid-sets-info {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 40fr 5fr 5fr 5fr 5fr 5fr 5fr 10fr 20fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: stretch;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-flex-row {\n" +
                "      display: flex;\n" +
                "      flex-flow: row wrap;\n" +
                "      align-items: flex-start;\n" +
                "      align-content: flex-start;\n" +
                "      justify-content: flex-start;\n" +
                "    }\n" +
                "    .div-flex-column {\n" +
                "      display: flex;\n" +
                "      flex-flow: column wrap;\n" +
                "      align-items: flex-start;\n" +
                "      align-content: flex-start;\n" +
                "      justify-content: flex-start;\n" +
                "    }\n" +
                "    .div-grid-team {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 1fr 8fr 1fr 8fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: center;\n" +
                "      align-content: start;\n" +
                "      justify-content: start;\n" +
                "      justify-items: start;\n" +
                "    }\n" +
                "    .div-grid-set-header-info {\n" +
                "      min-width: 175px;\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 3fr 1fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: stretch;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-grid-set-header-time {\n" +
                "      min-width: 250px;\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 7fr 3fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: stretch;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .set-index-cell {\n" +
                "      grid-row: 1 / span 2;\n" +
                "      line-height:44px;\n" +
                "    }\n" +
                "    .div-grid-lineup {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 1fr 1fr 1fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: center;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-grid-substitution {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 24fr 16fr 24fr 34fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: center;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-grid-timeout {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 1fr 2fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: center;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-grid-sanction {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: 3fr 2fr 4fr;\n" +
                "      grid-auto-rows: 1fr;\n" +
                "      align-items: center;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .div-footer {\n" +
                "      font-size: 10px;\n" +
                "      position: fixed;\n" +
                "      display: flex;\n" +
                "      flex-flow: row wrap;\n" +
                "      align-items: center;\n" +
                "      align-content: center;\n" +
                "      justify-content: center;\n" +
                "      bottom: 12px;\n" +
                "      right: 12px;\n" +
                "    }\n" +
                "    .vbr-logo-image {\n" +
                "      background-image: url(\"data:image/png;base64,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\");\n" +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 16px;\n" +
                "      height: 16px;\n" +
                "      margin-left: 6px;\n" +
                "    }\n" +

                "    .cell {\n" +
                "      min-width: 22px;\n" +
                "      text-align: center;\n" +
                "      padding: 3px;\n" +
                "    }\n" +
                "    .bordered-cell {\n" +
                "      border: 1px solid #1f1f1f;\n" +
                "      min-width: 22px;\n" +
                "      text-align: center;\n" +
                "      padding: 3px;\n" +
                "      margin-right: -1px;\n" +
                "      margin-left: -1px;\n" +
                "    }\n" +
                "    .set-anchor {\n" +
                "      color: #1f1f1f;\n" +
                "    }\n" +
                "    .badge {\n" +
                "      min-width: 22px;\n" +
                "      text-align: center;\n" +
                "      padding: 3px;\n" +
                "      margin: 2px;\n" +
                "      border-radius: 5px;\n" +
                "    }\n" +
                "    .spacing-before {\n" +
                "      margin-top: 12px;\n" +
                "    }\n" +
                "    .ladder-spacing {\n" +
                "      margin-bottom: 10px;\n" +
                "    }\n" +
                "    .horizontal-spacing {\n" +
                "      min-width: 34px;\n" +
                "    }\n" +
                "    .border {\n" +
                "      border: 1px solid #1f1f1f;\n" +
                "      margin-right: -1px;\n" +
                "      margin-left: -1px;\n" +
                "    }\n" +
                "    .substitution-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_sub.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 30px;\n" +
                "      height: 20px;\n" +
                "    }\n" +
                "    .timeout-gray-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_timeout.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      min-width: 12px;\n" +
                "      height: 12px;\n" +
                "    }\n" +
                "    .timeout-white-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_timeout_white.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      min-width: 12px;\n" +
                "      height: 12px;\n" +
                "    }\n" +
                "    .yellow-card-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_yellow_card.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 48px;\n" +
                "      height: 24px;\n" +
                "    }\n" +
                "    .red-card-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_red_card.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 48px;\n" +
                "      height: 24px;\n" +
                "    }\n" +
                "    .expulsion-card-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_expulsion_card.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 48px;\n" +
                "      height: 24px;\n" +
                "    }\n" +
                "    .disqualification-card-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_disqualification_card.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 48px;\n" +
                "      height: 24px;\n" +
                "    }\n" +
                "    .delay-warning-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_delay_warning.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 48px;\n" +
                "      height: 24px;\n" +
                "    }\n" +
                "    .delay-penalty-image {\n" +
                String.format("      background-image: url(\"data:image/png;base64,%s\");\n", toBase64("ic_delay_penalty.png")) +
                "      background-repeat: no-repeat;\n" +
                "      background-size: 100% 100%;\n" +
                "      width: 48px;\n" +
                "      height: 24px;\n" +
                "    }\n" +
                "    .new-page-for-printers {\n" +
                "      break-before: page;\n" +
                "    }\n" +
                "    </style> \n" +
                "    <style type=\"text/css\" media=\"print\">\n" +
                "    body {\n" +
                "      -webkit-print-color-adjust: exact;\n" +
                "    }\n" +
                "    </style>" +
                "  </head>\n" +
                "  <body class=\"vbr-body\">\n" +
                "  </body>\n" +
                "</html>\n";
    }

}