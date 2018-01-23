package com.tonkar.volleyballreferee.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tonkar.volleyballreferee.model.*;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PdfGameWriter {

    private static Font defaultFont = new Font(Font.FontFamily.HELVETICA, 10);

    private Game     mGame;
    private Document mDocument;
    private Font     mHomeTeamFont;
    private Font     mHomeCaptainFont;
    private Font     mGuestTeamFont;
    private Font     mGuestCaptainFont;
    private Font     mHomeLiberoFont;
    private Font     mGuestLiberoFont;

    private BaseColor mHomeTeamColor;
    private BaseColor mGuestTeamColor;
    private BaseColor mHomeLiberoColor;
    private BaseColor mGuestLiberoColor;

    public static PdfGame writeGame(Game game) throws IOException, DocumentException {
        DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        String date = formatter.format(new Date(game.getDate()));

        String homeTeam = game.gethTeam().getName();
        String guestTeam = game.getgTeam().getName();
        String filename = String.format(Locale.getDefault(), "%s_%s_%s.pdf", homeTeam, guestTeam, date);

        PdfGame pdfGame = new PdfGame(filename);

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        PdfGameWriter pdfGameWriter = new PdfGameWriter(game, document);
        pdfGameWriter.init();

        if ("INDOOR".equals(game.getKind())) {
            pdfGameWriter.writeRecordedIndoorGame();
        } else {
            pdfGameWriter.writeRecordedBeachGame();
        }
        document.close();

        pdfGame.setData(outputStream.toByteArray());
        return pdfGame;
    }

    private PdfGameWriter(Game game, Document document) {
        mGame = game;
        mDocument = document;
    }

    private void init() {
        mDocument.addAuthor("Volleyball Referee");
        mDocument.addCreator("Volleyball Referee");

        Color homeTeamColor = Color.decode(mGame.gethTeam().getColor());
        Color guestTeamColor = Color.decode(mGame.getgTeam().getColor());

        if (homeTeamColor.getRGB() == guestTeamColor.getRGB()) {
            guestTeamColor = Color.lightGray;
        }

        mHomeTeamFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(getTextColor(homeTeamColor)));
        mHomeCaptainFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.UNDERLINE, new BaseColor(getTextColor(homeTeamColor)));
        mHomeTeamColor = new BaseColor(homeTeamColor.getRGB());

        mHomeLiberoFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(getTextColor(Color.decode(mGame.gethTeam().getLiberoColor()))));
        mHomeLiberoColor = new BaseColor(Color.decode(mGame.gethTeam().getLiberoColor()).getRGB());

        mGuestTeamFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(getTextColor(guestTeamColor)));
        mGuestCaptainFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), Font.UNDERLINE, new BaseColor(getTextColor(guestTeamColor)));
        mGuestTeamColor = new BaseColor(guestTeamColor.getRGB());

        mGuestLiberoFont = new Font(defaultFont.getFamily(), defaultFont.getSize(), defaultFont.getStyle(), new BaseColor(getTextColor(Color.decode(mGame.getgTeam().getLiberoColor()))));
        mGuestLiberoColor = new BaseColor(Color.decode(mGame.getgTeam().getLiberoColor()).getRGB());
    }

    private int getTextColor(Color backgroundColor) {
        int textColor;

        double a = 1 - ( 0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255;

        if (a < 0.5) {
            textColor = Color.decode("#1f1f1f").getRGB();
        } else {
            textColor = Color.white.getRGB();
        }

        return textColor;
    }

    private void writeRecordedIndoorGame() throws DocumentException {
        writeRecordedGameHeader();
        writeRecordedIndoorTeams();

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            if (setIndex % 2 == 1) {
                mDocument.newPage();
            }
            writeRecordedIndoorSetHeader(setIndex);
            writeRecordedStartingLineup(setIndex);
            writeRecordedSubstitutions(setIndex);
            writeRecordedTimeouts(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

    private void writeRecordedGameHeader() throws DocumentException {
        float[] dateAndLeagueWidths = {0.4f, 0.15f, 0.45f};
        PdfPTable dateAndLeagueTable = new PdfPTable(dateAndLeagueWidths);
        dateAndLeagueTable.setWidthPercentage(100);
        dateAndLeagueTable.setSpacingBefore(5.f);

        PdfPCell leagueCell = new PdfPCell(new Phrase(mGame.getLeague(), defaultFont));
        leagueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        leagueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateAndLeagueTable.addCell(leagueCell);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        PdfPCell dateCell = new PdfPCell(new Phrase(formatter.format(new Date(mGame.getDate())), defaultFont));
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateAndLeagueTable.addCell(dateCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        dateAndLeagueTable.addCell(emptyCell);

        mDocument.add(dateAndLeagueTable);

        float[] columnWidths = {0.4f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.3f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10.f);

        PdfPCell homeTeamNameCell = new PdfPCell(new Phrase(mGame.gethTeam().getName(), mHomeTeamFont));
        homeTeamNameCell.setBackgroundColor(mHomeTeamColor);
        homeTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamNameCell);

        PdfPCell homeTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mGame.gethSets()), defaultFont));
        homeTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamSetsCell);

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mGame.getSets().get(setIndex).gethPoints()), defaultFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        int startIndex = mGame.getSets().size();

        for (int setIndex = startIndex; setIndex < 6; setIndex++) {
            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        PdfPCell guestTeamNameCell = new PdfPCell(new Phrase(mGame.getgTeam().getName(), mGuestTeamFont));
        guestTeamNameCell.setBackgroundColor(mGuestTeamColor);
        guestTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamNameCell);

        PdfPCell guestTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mGame.getgSets()), defaultFont));
        guestTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamSetsCell);

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mGame.getSets().get(setIndex).getgPoints()), defaultFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        startIndex = mGame.getSets().size();

        for (int setIndex = startIndex; setIndex < 6; setIndex++) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        mDocument.add(table);
    }

    private void writeRecordedIndoorTeams() throws DocumentException {
        if ("NORMAL".equals(mGame.getUsage())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            table.setSpacingAfter(10.f);

            PdfPCell titleCell = new PdfPCell(new Phrase("Players", defaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setRowspan(2);
            table.addCell(titleCell);

            PdfPCell homeTeamTable = new PdfPCell(createTeamTable(TeamType.HOME));
            homeTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(homeTeamTable);

            PdfPCell guestTeamTable = new PdfPCell(createTeamTable(TeamType.GUEST));
            guestTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(guestTeamTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createTeamTable(TeamType teamType) {
        Team team = TeamType.HOME.equals(teamType) ? mGame.gethTeam() : mGame.getgTeam();

        int numColumns = 20;
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(100);

        for (int player : team.getPlayers()) {
            table.addCell(createPlayerCell(teamType, player, false));
        }
        for (int player : team.getLiberos()) {
            table.addCell(createPlayerCell(teamType, player, true));
        }

        int startIndex = team.getPlayers().size() + team.getLiberos().size();

        for (int index = startIndex; (index % numColumns != 0); index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" ", defaultFont));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        return table;
    }

    private void writeRecordedIndoorSetHeader(int setIndex) throws DocumentException {
        float[] columnWidths = {0.15f, 0.05f, 0.05f, 0.05f, 0.7f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20.f);

        Font font;
        BaseColor color;
        Set set = mGame.getSets().get(setIndex);

        if (set.gethPoints() > set.getgPoints()) {
            font = mHomeTeamFont;
            color = mHomeTeamColor;
        } else {
            font = mGuestTeamFont;
            color = mGuestTeamColor;
        }

        List<String> ladder = set.getLadder();
        int hScore1 = 0, hScore2 = 0, gScore1 = 0, gScore2 = 0;
        int hScore = 0, gScore = 0;
        boolean partial1Reached = false, partial2Reached = false;

        for (String teamType : ladder) {
            if ("H".equals(teamType)) {
                hScore++;
            } else {
                gScore++;
            }

            if ((hScore == 8 && !partial1Reached) || (gScore == 8 && !partial1Reached)) {
                hScore1 = hScore;
                gScore1 = gScore;
                partial1Reached = true;
            } else if ((hScore == 16 && !partial2Reached) || (gScore == 16 && !partial2Reached)) {
                hScore2 = hScore;
                gScore2 = gScore;
                partial2Reached = true;
            }
        }

        PdfPCell indexCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "Set %d", (setIndex + 1)), font));
        indexCell.setBackgroundColor(color);
        indexCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        indexCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(indexCell);

        PdfPCell hScoreCell = new PdfPCell(new Phrase(String.valueOf(set.gethPoints()), defaultFont));

        hScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScoreCell);

        PdfPCell hScore1Cell = new PdfPCell(new Phrase(String.valueOf(hScore1), defaultFont));
        hScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore1Cell);

        PdfPCell hScore2Cell = new PdfPCell(new Phrase(String.valueOf(hScore2), defaultFont));
        hScore2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore2Cell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setRowspan(2);
        table.addCell(emptyCell);

        int duration = (int) Math.ceil(set.getDuration() / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d min", duration), defaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell gScoreCell = new PdfPCell(new Phrase(String.valueOf(set.getgPoints()), defaultFont));
        gScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScoreCell);

        PdfPCell gScore1Cell = new PdfPCell(new Phrase(String.valueOf(gScore1), defaultFont));
        gScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore1Cell);

        PdfPCell gScore2Cell = new PdfPCell(new Phrase(String.valueOf(gScore2), defaultFont));
        gScore2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore2Cell);

        mDocument.add(table);
    }

    private void writeRecordedStartingLineup(int setIndex) throws DocumentException {
        if ("NORMAL".equals(mGame.getUsage())) {
            float[] columnWidths = {0.15f, 0.15f, 0.7f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase("Starting line-up", defaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setColspan(2);
            table.addCell(titleCell);

            PdfPCell ladderCell = new PdfPCell();
            ladderCell.setRowspan(2);
            ladderCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(ladderCell);

            PdfPCell homeTeamTable = new PdfPCell(createLineupTable(TeamType.HOME, setIndex));
            homeTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(homeTeamTable);

            PdfPCell guestTeamTable = new PdfPCell(createLineupTable(TeamType.GUEST, setIndex));
            guestTeamTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(guestTeamTable);

            mDocument.add(table);
        }
    }

    private int getPlayerNumberAtPositionInStartingLineup(List<Player> startingLineup, int position) {
        int number = 0;

        for (Player player: startingLineup) {
            if (player.getPos() == position) {
                number = player.getNum();
            }
        }

        return number;
    }

    private PdfPTable createLineupTable(TeamType teamType, int setIndex) {
        List<Player> startingLineup = TeamType.HOME.equals(teamType) ? mGame.getSets().get(setIndex).gethStartingPlayers() : mGame.getSets().get(setIndex).getgStartingPlayers();

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        PdfPCell pos4TitleCell = new PdfPCell(new Phrase("IV", defaultFont));
        pos4TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos4TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos4TitleCell);

        PdfPCell pos3TitleCell = new PdfPCell(new Phrase("III", defaultFont));
        pos3TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos3TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos3TitleCell);

        PdfPCell pos2TitleCell = new PdfPCell(new Phrase("II", defaultFont));
        pos2TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos2TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos2TitleCell);

        PdfPCell pos4Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 4), false);
        table.addCell(pos4Cell);

        PdfPCell pos3Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 3), false);
        table.addCell(pos3Cell);

        PdfPCell pos2Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 2), false);
        table.addCell(pos2Cell);

        PdfPCell pos5TitleCell = new PdfPCell(new Phrase("V", defaultFont));
        pos5TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos5TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos5TitleCell);

        PdfPCell pos6TitleCell = new PdfPCell(new Phrase("VI", defaultFont));
        pos6TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos6TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos6TitleCell);

        PdfPCell pos1TitleCell = new PdfPCell(new Phrase("I", defaultFont));
        pos1TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos1TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos1TitleCell);

        PdfPCell pos5Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 5), false);
        table.addCell(pos5Cell);

        PdfPCell pos6Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 6), false);
        table.addCell(pos6Cell);

        PdfPCell pos1Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 1), false);
        table.addCell(pos1Cell);

        return table;
    }

    private void writeRecordedSubstitutions(int setIndex) throws DocumentException {
        if ("NORMAL".equals(mGame.getUsage())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase("Substitutions", defaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setRowspan(2);
            table.addCell(titleCell);

            PdfPCell homeSubstitutionsTable = new PdfPCell(createSubstitutionsTable(TeamType.HOME, setIndex));
            homeSubstitutionsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(homeSubstitutionsTable);

            PdfPCell guestSubstitutionsTable = new PdfPCell(createSubstitutionsTable(TeamType.GUEST, setIndex));
            guestSubstitutionsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(guestSubstitutionsTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createSubstitutionsTable(TeamType teamType, int setIndex) {
        float[] columnWidths = {0.166f, 0.166f, 0.166f, 0.166f, 0.166f, 0.166f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Substitution> substitutions = TeamType.HOME.equals(teamType) ? mGame.getSets().get(setIndex).gethSubstitutions() : mGame.getSets().get(setIndex).getgSubstitutions();

        for (int index = 0; index < 12; index++) {
            PdfPCell cell;
            if (index < substitutions.size()) {
                cell = new PdfPCell(createSubstitutionTable(teamType, substitutions.get(index)));
                cell.setBorder(Rectangle.NO_BORDER);
            } else if (index < 6) {
                cell = new PdfPCell(new Phrase(" "));
            } else {
                cell = new PdfPCell();
            }
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createSubstitutionTable(TeamType teamType, Substitution substitution) {
        float[] columnWidths = {0.22f, 0.15f, 0.22f, 0.39f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        table.addCell(createPlayerCell(teamType, substitution.getpIn(), false));

        PdfPCell arrowCell = new PdfPCell(new Phrase("=>", defaultFont));
        arrowCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        arrowCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(arrowCell);

        table.addCell(createPlayerCell(teamType, substitution.getpOut(), false));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = substitution.gethPoints() + "-" + substitution.getgPoints();
        } else {
            score = substitution.getgPoints() + "-" + substitution.gethPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, defaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private void writeRecordedTimeouts(int setIndex) throws DocumentException {
        if ("NORMAL".equals(mGame.getUsage()) || "POINTS_SCOREBOARD".equals(mGame.getUsage())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase("Timeouts", defaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(titleCell);

            PdfPCell timeoutsTable = new PdfPCell(createTimeoutsTable(setIndex));
            timeoutsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(timeoutsTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createTimeoutsTable(int setIndex) {
        float[] columnWidths = {0.10f, 0.10f, 0.10f, 0.10f, 0.6f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Timeout> hTimeouts = mGame.getSets().get(setIndex).gethCalledTimeouts();
        List<Timeout> gTimeouts = mGame.getSets().get(setIndex).getgCalledTimeouts();

        for (Timeout timeout : hTimeouts) {
            PdfPCell cell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d-%d", timeout.gethPoints(), timeout.getgPoints()), mHomeTeamFont));
            cell.setBackgroundColor(mHomeTeamColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        for (Timeout timeout : gTimeouts) {
            PdfPCell cell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d-%d", timeout.getgPoints(), timeout.gethPoints()), mGuestTeamFont));
            cell.setBackgroundColor(mGuestTeamColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }

        for (int index = hTimeouts.size() + gTimeouts.size(); index < 4; index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" "));
            table.addCell(cell);
        }

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(emptyCell);

        return table;
    }

    private void writeRecordedLadder(int setIndex) throws DocumentException {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase("Points", defaultFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleTable.addCell(titleCell);

        mDocument.add(titleTable);

        int numColumns = 35;
        PdfPTable table = new PdfPTable(numColumns);
        table.setWidthPercentage(100);

        int homeScore = 0;
        int guestScore = 0;

        List<String> ladder = mGame.getSets().get(setIndex).getLadder();

        for (String teamType : ladder) {
            PdfPCell ladderCell;
            if ("H".equals(teamType)) {
                homeScore++;
                ladderCell = new PdfPCell(createLadderCell(TeamType.HOME, homeScore));
            } else {
                guestScore++;
                ladderCell = new PdfPCell(createLadderCell(TeamType.GUEST, guestScore));
            }

            ladderCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(ladderCell);
        }

        int startIndex = ladder.size();

        for (int index = startIndex; (index % numColumns != 0); index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" ", defaultFont));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        mDocument.add(table);
    }

    private PdfPTable createLadderCell(TeamType teamType, int score) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell homeCell;
        PdfPCell guestCell;

        if (TeamType.HOME.equals(teamType)) {
            homeCell = new PdfPCell(createPlayerCell(score, mHomeTeamFont, mHomeTeamColor));
            guestCell = new PdfPCell(new Phrase(" ", mGuestTeamFont));
        } else {
            homeCell = new PdfPCell(new Phrase(" ", mHomeTeamFont));
            guestCell = new PdfPCell(createPlayerCell(score, mGuestTeamFont, mGuestTeamColor));
        }

        table.addCell(homeCell);
        table.addCell(guestCell);

        return table;
    }

    private PdfPCell createPlayerCell(TeamType teamType, int player, boolean isLibero) {
        Font font;
        BaseColor color;

        if (isLibero) {
            font = TeamType.HOME.equals(teamType) ? mHomeLiberoFont : mGuestLiberoFont;
            color = TeamType.HOME.equals(teamType) ? mHomeLiberoColor : mGuestLiberoColor;
        } else {
            int captain = TeamType.HOME.equals(teamType) ? mGame.gethTeam().getCaptain() : mGame.getgTeam().getCaptain();

            if (captain == player) {
                font = TeamType.HOME.equals(teamType) ? mHomeCaptainFont : mGuestCaptainFont;
            } else {
                font = TeamType.HOME.equals(teamType) ? mHomeTeamFont : mGuestTeamFont;
            }
            color = TeamType.HOME.equals(teamType) ? mHomeTeamColor : mGuestTeamColor;
        }
        return createPlayerCell(player, font, color);
    }

    private PdfPCell createPlayerCell(int player, Font font, BaseColor color) {
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(player), font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5.f);
        cell.setBackgroundColor(color);
        return cell;
    }

    private void writeRecordedBeachGame() throws DocumentException {
        writeRecordedGameHeader();

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            writeRecordedBeachSetHeader(setIndex);
            writeRecordedTimeouts(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

    private void writeRecordedBeachSetHeader(int setIndex) throws DocumentException {
        float[] columnWidths = {0.15f, 0.05f, 0.05f, 0.75f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20.f);

        Font font;
        BaseColor color;
        Set set = mGame.getSets().get(setIndex);

        if (set.gethPoints() > set.getgPoints()) {
            font = mHomeTeamFont;
            color = mHomeTeamColor;
        } else {
            font = mGuestTeamFont;
            color = mGuestTeamColor;
        }

        List<String> ladder = set.getLadder();
        int hScore1 = 0, gScore1 = 0;
        int hScore = 0, gScore = 0;

        for (String teamType : ladder) {
            if ("H".equals(teamType)) {
                hScore++;
            } else {
                gScore++;
            }

            if (hScore + gScore == 21) {
                hScore1 = hScore;
                gScore1 = gScore;
            }
        }

        PdfPCell indexCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "Set %d", (setIndex + 1)), font));
        indexCell.setBackgroundColor(color);
        indexCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        indexCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(indexCell);

        PdfPCell hScoreCell = new PdfPCell(new Phrase(String.valueOf(set.gethPoints()), defaultFont));

        hScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScoreCell);

        PdfPCell hScore1Cell = new PdfPCell(new Phrase(String.valueOf(hScore1), defaultFont));
        hScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore1Cell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setRowspan(2);
        table.addCell(emptyCell);

        int duration = (int) Math.ceil(set.getDuration() / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d min", duration), defaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell gScoreCell = new PdfPCell(new Phrase(String.valueOf(set.getgPoints()), defaultFont));
        gScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScoreCell);

        PdfPCell gScore1Cell = new PdfPCell(new Phrase(String.valueOf(gScore1), defaultFont));
        gScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore1Cell);

        mDocument.add(table);
    }
}