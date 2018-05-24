package com.tonkar.volleyballreferee.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tonkar.volleyballreferee.model.*;
import com.tonkar.volleyballreferee.model.Set;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class PdfGameWriter {

    static {
        FontFactory.register("/fonts/NotoSans-Regular.ttf", "Noto");
        FontFactory.register("/fonts/NotoSansCJKjp-Regular.otf", "Noto-Jp");
        FontFactory.register("/fonts/NotoSansCJKkr-Regular.otf", "Noto-Kr");
        FontFactory.register("/fonts/NotoSansCJKsc-Regular.otf", "Noto-Sc");
        FontFactory.register("/fonts/NotoSansDevanagari-Regular.ttf", "Noto-Deva");
        FontFactory.register("/fonts/NotoSansJavanese-Regular.ttf", "Noto-Java");
        FontFactory.register("/fonts/NotoSansTagalog-Regular.ttf", "Noto-Taga");
        FontFactory.register("/fonts/NotoSansThai-Regular.ttf", "Noto-Thai");
    }

    private Game     mGame;
    private Document mDocument;
    private Font     mDefaultFont;
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

    private Image mSubstitutionImage;
    private Image mTimeoutGrayImage;
    private Image mTimeoutWhiteImage;
    private Image mYellowCardImage;
    private Image mRedCardImage;
    private Image mExpulsionCardImage;
    private Image mDisqualificationCardImage;
    private Image mDelayWarningImage;
    private Image mDelayPenaltyImage;

    public static PdfGame writeGame(Game game) throws IOException, DocumentException {
        DateFormat formatter = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        String date = formatter.format(new Date(game.getSchedule()));

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
        mDefaultFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, 10);
    }

    private void init() throws IOException, BadElementException {
        mDocument.addAuthor("Volleyball Referee");
        mDocument.addCreator("Volleyball Referee");

        Color homeTeamColor = Color.decode(mGame.gethTeam().getColor());
        Color guestTeamColor = Color.decode(mGame.getgTeam().getColor());

        if (homeTeamColor.getRGB() == guestTeamColor.getRGB()) {
            guestTeamColor = Color.lightGray;
        }

        BaseColor homeTeamColorText = new BaseColor(getTextColor(homeTeamColor));
        BaseColor guestTeamColorText = new BaseColor(getTextColor(guestTeamColor));

        mHomeTeamFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), homeTeamColorText);
        mHomeCaptainFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), Font.UNDERLINE, homeTeamColorText);
        mHomeTeamColor = new BaseColor(homeTeamColor.getRGB());

        mHomeLiberoFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), new BaseColor(getTextColor(Color.decode(mGame.gethTeam().getLiberoColor()))));
        mHomeLiberoColor = new BaseColor(Color.decode(mGame.gethTeam().getLiberoColor()).getRGB());

        mGuestTeamFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), guestTeamColorText);
        mGuestCaptainFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), Font.UNDERLINE, guestTeamColorText);
        mGuestTeamColor = new BaseColor(guestTeamColor.getRGB());

        mGuestLiberoFont = FontFactory.getFont("Noto", BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), new BaseColor(getTextColor(Color.decode(mGame.getgTeam().getLiberoColor()))));
        mGuestLiberoColor = new BaseColor(Color.decode(mGame.getgTeam().getLiberoColor()).getRGB());

        mSubstitutionImage = Image.getInstance(getClass().getResource("/images/ic_sub.png"));
        mTimeoutGrayImage = Image.getInstance(getClass().getResource("/images/ic_timeout.png"));
        mTimeoutWhiteImage = Image.getInstance(getClass().getResource("/images/ic_timeout_white.png"));
        mYellowCardImage = Image.getInstance(getClass().getResource("/images/ic_yellow_card.png"));
        mRedCardImage = Image.getInstance(getClass().getResource("/images/ic_red_card.png"));
        mExpulsionCardImage = Image.getInstance(getClass().getResource("/images/ic_expulsion_card.png"));
        mDisqualificationCardImage = Image.getInstance(getClass().getResource("/images/ic_disqualification_card.png"));
        mDelayWarningImage = Image.getInstance(getClass().getResource("/images/ic_delay_warning.png"));
        mDelayPenaltyImage = Image.getInstance(getClass().getResource("/images/ic_delay_penalty.png"));

        mSubstitutionImage.scaleAbsolute(16, 12);
        mTimeoutGrayImage.scaleAbsolute(12, 12);
        mTimeoutWhiteImage.scaleAbsolute(12, 12);
        mYellowCardImage.scaleAbsolute(32, 16);
        mRedCardImage.scaleAbsolute(32, 16);
        mExpulsionCardImage.scaleAbsolute(32, 16);
        mDisqualificationCardImage.scaleAbsolute(32, 16);
        mDelayWarningImage.scaleAbsolute(32, 16);
        mDelayPenaltyImage.scaleAbsolute(32, 16);
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
            writeRecordedSanctions(setIndex);
            writeRecordedLadder(setIndex);
        }
    }

    private void writeRecordedGameHeader() throws DocumentException {
        float[] infoLineWidths = {0.4f, 0.15f, 0.30f, 0.15f};
        PdfPTable infoTable = new PdfPTable(infoLineWidths);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(5.f);

        Font leagueFont = FontFactory.getFont(getFontname(mGame.getLeague()), BaseFont.IDENTITY_H, true, mDefaultFont.getSize(), mDefaultFont.getStyle(), mDefaultFont.getColor());
        PdfPCell leagueCell = new PdfPCell(new Phrase(mGame.getLeague(), leagueFont));
        leagueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        leagueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoTable.addCell(leagueCell);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        PdfPCell dateCell = new PdfPCell(new Phrase(formatter.format(new Date(mGame.getDate())), mDefaultFont));
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoTable.addCell(dateCell);

        String referee = mGame.getReferee();
        PdfPCell refereeCell = new PdfPCell(new Phrase(referee, mDefaultFont));
        refereeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        refereeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (referee.isEmpty()) {
            refereeCell.setBorder(Rectangle.NO_BORDER);
        }
        infoTable.addCell(refereeCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(emptyCell);

        mDocument.add(infoTable);

        float[] columnWidths = {0.4f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.3f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10.f);

        Font homeTeamNameFont = FontFactory.getFont(getFontname(mGame.gethTeam().getName()), BaseFont.IDENTITY_H, true, mHomeTeamFont.getSize(), mHomeTeamFont.getStyle(), mHomeTeamFont.getColor());
        PdfPCell homeTeamNameCell = new PdfPCell(new Phrase(mGame.gethTeam().getName(), homeTeamNameFont));
        homeTeamNameCell.setBackgroundColor(mHomeTeamColor);
        homeTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamNameCell);

        PdfPCell homeTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mGame.gethSets()), mDefaultFont));
        homeTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        homeTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(homeTeamSetsCell);

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mGame.getSets().get(setIndex).gethPoints()), mDefaultFont));
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

        Font guestTeamNameFont = FontFactory.getFont(getFontname(mGame.getgTeam().getName()), BaseFont.IDENTITY_H, true, mGuestTeamFont.getSize(), mGuestTeamFont.getStyle(), mGuestTeamFont.getColor());
        PdfPCell guestTeamNameCell = new PdfPCell(new Phrase(mGame.getgTeam().getName(), guestTeamNameFont));
        guestTeamNameCell.setBackgroundColor(mGuestTeamColor);
        guestTeamNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamNameCell);

        PdfPCell guestTeamSetsCell = new PdfPCell(new Phrase(String.valueOf(mGame.getgSets()), mDefaultFont));
        guestTeamSetsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        guestTeamSetsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(guestTeamSetsCell);

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(mGame.getSets().get(setIndex).getgPoints()), mDefaultFont));
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

            PdfPCell titleCell = new PdfPCell(new Phrase("Players", mDefaultFont));
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
            PdfPCell cell = new PdfPCell(new Phrase(" ", mDefaultFont));
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

        PdfPCell hScoreCell = new PdfPCell(new Phrase(String.valueOf(set.gethPoints()), mDefaultFont));

        hScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScoreCell);

        PdfPCell hScore1Cell = new PdfPCell(new Phrase(String.valueOf(hScore1), mDefaultFont));
        hScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore1Cell);

        PdfPCell hScore2Cell = new PdfPCell(new Phrase(String.valueOf(hScore2), mDefaultFont));
        hScore2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore2Cell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setRowspan(2);
        table.addCell(emptyCell);

        int duration = (int) Math.ceil(set.getDuration() / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d min", duration), mDefaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell gScoreCell = new PdfPCell(new Phrase(String.valueOf(set.getgPoints()), mDefaultFont));
        gScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScoreCell);

        PdfPCell gScore1Cell = new PdfPCell(new Phrase(String.valueOf(gScore1), mDefaultFont));
        gScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore1Cell);

        PdfPCell gScore2Cell = new PdfPCell(new Phrase(String.valueOf(gScore2), mDefaultFont));
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

            PdfPCell titleCell = new PdfPCell(new Phrase("Starting line-up", mDefaultFont));
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
        int number = -1;

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

        PdfPCell pos4TitleCell = new PdfPCell(new Phrase("IV", mDefaultFont));
        pos4TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos4TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos4TitleCell);

        PdfPCell pos3TitleCell = new PdfPCell(new Phrase("III", mDefaultFont));
        pos3TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos3TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos3TitleCell);

        PdfPCell pos2TitleCell = new PdfPCell(new Phrase("II", mDefaultFont));
        pos2TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos2TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos2TitleCell);

        PdfPCell pos4Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 4), false);
        table.addCell(pos4Cell);

        PdfPCell pos3Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 3), false);
        table.addCell(pos3Cell);

        PdfPCell pos2Cell = createPlayerCell(teamType, getPlayerNumberAtPositionInStartingLineup(startingLineup, 2), false);
        table.addCell(pos2Cell);

        PdfPCell pos5TitleCell = new PdfPCell(new Phrase("V", mDefaultFont));
        pos5TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos5TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos5TitleCell);

        PdfPCell pos6TitleCell = new PdfPCell(new Phrase("VI", mDefaultFont));
        pos6TitleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pos6TitleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(pos6TitleCell);

        PdfPCell pos1TitleCell = new PdfPCell(new Phrase("I", mDefaultFont));
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

            PdfPCell titleCell = new PdfPCell(new Phrase("Substitutions", mDefaultFont));
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
        float[] columnWidths = {0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Substitution> substitutions = TeamType.HOME.equals(teamType) ? mGame.getSets().get(setIndex).gethSubstitutions() : mGame.getSets().get(setIndex).getgSubstitutions();

        for (int index = 0; index < (2 * columnWidths.length); index++) {
            PdfPCell cell;
            if (index < substitutions.size()) {
                cell = new PdfPCell(createSubstitutionTable(teamType, substitutions.get(index)));
                cell.setBorder(Rectangle.NO_BORDER);
            } else if (index < columnWidths.length) {
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

        PdfPCell imageCell = new PdfPCell(mSubstitutionImage);
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imageCell.setPadding(1.f);
        table.addCell(imageCell);

        table.addCell(createPlayerCell(teamType, substitution.getpOut(), false));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = substitution.gethPoints() + "-" + substitution.getgPoints();
        } else {
            score = substitution.getgPoints() + "-" + substitution.gethPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, mDefaultFont));
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

            PdfPCell titleCell = new PdfPCell(new Phrase("Timeouts", mDefaultFont));
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
        float[] columnWidths = {0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.3336f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Timeout> hTimeouts = mGame.getSets().get(setIndex).gethCalledTimeouts();
        List<Timeout> gTimeouts = mGame.getSets().get(setIndex).getgCalledTimeouts();

        for (Timeout timeout : hTimeouts) {
            PdfPCell cell = new PdfPCell(createTimeoutTable(TeamType.HOME, timeout));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (Timeout timeout : gTimeouts) {
            PdfPCell cell = new PdfPCell(createTimeoutTable(TeamType.GUEST, timeout));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (int index = hTimeouts.size() + gTimeouts.size(); index < columnWidths.length; index++) {
            PdfPCell cell = new PdfPCell(new Phrase(" "));
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createTimeoutTable(TeamType teamType, Timeout timeout) {
        float[] columnWidths = {0.38f, 0.62f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        table.addCell(createTimeoutCell(teamType));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = timeout.gethPoints() + "-" + timeout.getgPoints();
        } else {
            score = timeout.getgPoints() + "-" + timeout.gethPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, mDefaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private void writeRecordedSanctions(int setIndex) throws DocumentException {
        if ("NORMAL".equals(mGame.getUsage())) {
            float[] columnWidths = {0.15f, 0.85f};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);

            PdfPCell titleCell = new PdfPCell(new Phrase("Sanctions", mDefaultFont));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(titleCell);

            PdfPCell sanctionsTable = new PdfPCell(createSanctionsTable(setIndex));
            sanctionsTable.setBorder(Rectangle.NO_BORDER);
            table.addCell(sanctionsTable);

            mDocument.add(table);
        }
    }

    private PdfPTable createSanctionsTable(int setIndex) {
        float[] columnWidths = {0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f, 0.1666f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        List<Sanction> hSanctions = new ArrayList<>();
        List<Sanction> gSanctions = new ArrayList<>();

        for (Sanction sanction: mGame.gethCards()) {
            if (sanction.getSet() == setIndex) {
                hSanctions.add(sanction);
            }
        }

        for (Sanction sanction: mGame.getgCards()) {
            if (sanction.getSet() == setIndex) {
                gSanctions.add(sanction);
            }
        }

        for (Sanction sanction: hSanctions) {
            PdfPCell cell = new PdfPCell(createSanctionTable(TeamType.HOME, sanction));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (Sanction sanction: gSanctions) {
            PdfPCell cell = new PdfPCell(createSanctionTable(TeamType.GUEST, sanction));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        for (int index = hSanctions.size() + gSanctions.size(); index < (2 * columnWidths.length); index++) {
            PdfPCell cell;

            if (index < columnWidths.length) {
                cell = new PdfPCell(new Phrase(" "));
            } else {
                cell = new PdfPCell();
            }
            table.addCell(cell);
        }

        return table;
    }

    private PdfPTable createSanctionTable(TeamType teamType, Sanction sanction) {
        float[] columnWidths = {0.38f, 0.22f, 0.4f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        PdfPCell imageCell = new PdfPCell(getSanctionImage(sanction.getCard()));
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imageCell.setPadding(1.f);
        table.addCell(imageCell);

        int player = sanction.getNum();
        List<Integer> liberos = TeamType.HOME.equals(teamType) ? mGame.gethTeam().getLiberos() : mGame.getgTeam().getLiberos();
        table.addCell(createPlayerCell(teamType, player, liberos.contains(player)));

        String score;
        if (TeamType.HOME.equals(teamType)) {
            score = sanction.gethPoints() + "-" + sanction.getgPoints();
        } else {
            score = sanction.getgPoints() + "-" + sanction.gethPoints();
        }

        PdfPCell scoreCell = new PdfPCell(new Phrase(score, mDefaultFont));
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(scoreCell);

        return table;
    }

    private Image getSanctionImage(String sanctionType) {
        Image image;

        switch (sanctionType) {
            case "Y":
                image = mYellowCardImage;
                break;
            case "R":
                image = mRedCardImage;
                break;
            case "RE":
                image = mExpulsionCardImage;
                break;
            case "RD":
                image = mDisqualificationCardImage;
                break;
            case "DW":
                image = mDelayWarningImage;
                break;
            case "DP":
            default:
                image = mDelayPenaltyImage;
                break;
        }

        return image;
    }

    private void writeRecordedLadder(int setIndex) throws DocumentException {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell(new Phrase("Points", mDefaultFont));
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
            PdfPCell cell = new PdfPCell(new Phrase(" ", mDefaultFont));
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
        String playerStr = String.valueOf(player);

        if (player < 0) {
            playerStr = " ";
        } else if (player == 0) {
            playerStr = "C.";
        }

        PdfPCell cell = new PdfPCell(new Phrase(playerStr, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5.f);
        cell.setBackgroundColor(color);
        return cell;
    }

    private PdfPCell createTimeoutCell(TeamType teamType) {
        BaseColor backgroundColor;

        if (TeamType.HOME.equals(teamType)) {
            backgroundColor = mHomeTeamColor;
        } else {
            backgroundColor = mGuestTeamColor;
        }

        PdfPCell cell = new PdfPCell(getTimeoutImage(backgroundColor));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2.f);
        cell.setBackgroundColor(backgroundColor);
        return cell;
    }

    private Image getTimeoutImage(BaseColor backgroundColor) {
        Image image;

        double a = 1 - ( 0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255;

        if (a < 0.5) {
            image = mTimeoutGrayImage;
        } else {
            image = mTimeoutWhiteImage;
        }

        return image;
    }

    private void writeRecordedBeachGame() throws DocumentException {
        writeRecordedGameHeader();

        for (int setIndex = 0; setIndex < mGame.getSets().size(); setIndex++) {
            writeRecordedBeachSetHeader(setIndex);
            writeRecordedTimeouts(setIndex);
            writeRecordedSanctions(setIndex);
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

        PdfPCell hScoreCell = new PdfPCell(new Phrase(String.valueOf(set.gethPoints()), mDefaultFont));

        hScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScoreCell);

        PdfPCell hScore1Cell = new PdfPCell(new Phrase(String.valueOf(hScore1), mDefaultFont));
        hScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(hScore1Cell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setRowspan(2);
        table.addCell(emptyCell);

        int duration = (int) Math.ceil(set.getDuration() / 60000.0);
        PdfPCell durationCell = new PdfPCell(new Phrase(String.format(Locale.getDefault(), "%d min", duration), mDefaultFont));
        durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(durationCell);

        PdfPCell gScoreCell = new PdfPCell(new Phrase(String.valueOf(set.getgPoints()), mDefaultFont));
        gScoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScoreCell);

        PdfPCell gScore1Cell = new PdfPCell(new Phrase(String.valueOf(gScore1), mDefaultFont));
        gScore1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        gScore1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(gScore1Cell);

        mDocument.add(table);
    }

    private String getFontname(String text) {
        String fontname = "Noto";

        if (!text.isEmpty()) {
            int codePoint = Character.codePointAt(text, 0);

            switch (Character.UnicodeScript.of(codePoint)) {
                case HAN:
                    fontname = "Noto-Sc";
                    break;
                case THAI:
                    fontname = "Noto-Thai";
                    break;
                case KATAKANA:
                case HIRAGANA:
                    fontname = "Noto-Jp";
                    break;
                case TAGALOG:
                    fontname = "Noto-Taga";
                    break;
                case JAVANESE:
                    fontname = "Noto-Java";
                    break;
                case DEVANAGARI:
                    fontname = "Noto-Deva";
                    break;
                case HANGUL:
                    fontname = "Noto-Kr";
                    break;
                default:
                    break;
            }
        }

        return fontname;
    }
}