package com.tonkar.volleyballreferee.export;

import com.tonkar.volleyballreferee.dto.*;
import com.tonkar.volleyballreferee.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.Color;
import java.io.*;
import java.text.DateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelDivisionWriter {

    private final List<GameScoreDto> games;
    private final XSSFWorkbook       workbook;
    private final XSSFCellStyle      headerStyle;
    private final XSSFCellStyle      matchStyle;
    private final XSSFCellStyle      setStyle;
    private final XSSFCellStyle      pointStyle;
    private final XSSFCellStyle      homeDefaultStyle;
    private final XSSFCellStyle      guestDefaultStyle;
    private final XSSFCellStyle      homeSetStyle;
    private final XSSFCellStyle      guestSetStyle;
    private final XSSFCellStyle      homePointStyle;
    private final XSSFCellStyle      guestPointStyle;

    private ExcelDivisionWriter(List<GameScoreDto> games) {
        this.games = games;
        this.workbook = new XSSFWorkbook();
        this.headerStyle = createExcelBorderedStyle("#e4e4e4");
        this.matchStyle = createExcelBorderedStyle("#F0E68C");
        this.setStyle = createExcelBorderedStyle("#ADD8E6");
        this.pointStyle = createExcelBorderedStyle("#E9967A");
        this.homeDefaultStyle = createExcelStyle(TeamType.HOME);
        this.guestDefaultStyle = createExcelStyle(TeamType.GUEST);
        this.homeSetStyle = createExcelBorderedStyle("#ADD8E6", TeamType.HOME);
        this.guestSetStyle = createExcelBorderedStyle("#ADD8E6", TeamType.GUEST);
        this.homePointStyle = createExcelBorderedStyle("#E9967A", TeamType.HOME);
        this.guestPointStyle = createExcelBorderedStyle("#E9967A", TeamType.GUEST);
    }

    private XSSFWorkbook getWorkbook() {
        return workbook;
    }

    public static FileWrapper writeExcelDivision(String divisionName, List<GameScoreDto> games) throws IOException {
        ExcelDivisionWriter excelDivisionWriter = new ExcelDivisionWriter(games);
        excelDivisionWriter.createMatchesExcelSheet();
        excelDivisionWriter.createRankingsExcelSheet();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        excelDivisionWriter.getWorkbook().write(byteArrayOutputStream);

        String filename = String.format(Locale.getDefault(), "%s.xlsx", divisionName);
        filename = filename.replaceAll("[\\s|\\?\\*<:>\\+\\[\\]/\\']", "_");
        return new FileWrapper(filename, byteArrayOutputStream.toByteArray());
    }

    private void createMatchesExcelSheet() {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        String[] gamesHeader = { "Date", "Team", "Total Sets", "Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Total Points" };

        XSSFSheet sheet = workbook.createSheet("Matches");
        XSSFRow row = sheet.createRow(0);

        for (int index = 0; index < gamesHeader.length; index++) {
            XSSFCell cell = row.createCell(index);
            cell.setCellValue(gamesHeader[index]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;

        for (GameScoreDto game : games) {
            row = sheet.createRow(rowIndex);

            XSSFCell cell = row.createCell(0);
            cell.setCellValue(formatter.format(game.getScheduledAt()));
            cell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 1, 0, 0));

            createMatchExcelRow(row, TeamType.HOME, game);
            rowIndex++;

            row = sheet.createRow(rowIndex);

            createMatchExcelRow(row, TeamType.GUEST, game);
            rowIndex++;
        }
    }

    private void createMatchExcelRow(XSSFRow row, TeamType teamType, GameScoreDto game) {
        XSSFCell cell = row.createCell(1);
        cell.setCellValue(game.getTeamName(teamType).trim());
        cell.setCellStyle(createExcelTeamStyle(teamType, game.getTeamColor(teamType)));

        cell = row.createCell(2);
        cell.setCellValue(TeamType.HOME.equals(teamType) ? game.getHomeSets() : game.getGuestSets());
        cell.setCellStyle(TeamType.HOME.equals(teamType) ? homeSetStyle : guestSetStyle);

        createSetsExcelRow(row, teamType, game);
    }

    private void createSetsExcelRow(XSSFRow row, TeamType teamType, GameScoreDto game) {
        int columnIndex = 3;

        int total = 0;

        XSSFCellStyle style = TeamType.HOME.equals(teamType) ? homeDefaultStyle : guestDefaultStyle;

        for (SetSummaryDto set : game.getSets()) {
            int points = set.getPoints(teamType);
            total += points;
            XSSFCell cell = row.createCell(columnIndex);
            cell.setCellValue(points);
            cell.setCellStyle(style);
            columnIndex++;
        }

        while (columnIndex < 8) {
            XSSFCell cell = row.createCell(columnIndex);
            cell.setCellValue(0);
            cell.setCellStyle(style);
            columnIndex++;
        }

        XSSFCell cell = row.createCell(columnIndex);
        cell.setCellValue(total);
        cell.setCellStyle(TeamType.HOME.equals(teamType) ? homePointStyle : guestPointStyle);
    }

    private void createRankingsExcelSheet() {
        String[] rankingsHeader = { "Team",
                                    "Matches Played",
                                    "Matches Won",
                                    "Matches Lost",
                                    "Matches Diff",
                                    "Sets Won",
                                    "Sets Lost",
                                    "Sets Diff",
                                    "Points Won",
                                    "Points Lost",
                                    "Points Diff"
        };

        XSSFSheet sheet = workbook.createSheet("Rankings");
        XSSFRow row = sheet.createRow(0);

        for (int index = 0; index < rankingsHeader.length; index++) {
            XSSFCell cell = row.createCell(index);
            cell.setCellValue(rankingsHeader[index]);
            cell.setCellStyle(headerStyle);
        }

        Rankings rankings = new Rankings();
        games.forEach(rankings::addGame);

        int rowIndex = 1;

        for (RankingDto ranking : rankings.list()) {
            row = sheet.createRow(rowIndex);
            AtomicInteger columnIndex = new AtomicInteger(0);

            XSSFCell cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getTeamName());
            cell.setCellStyle(createExcelTeamStyle(ranking.getTeamColor()));

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getMatchesFor() + ranking.getMatchesAgainst());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getMatchesFor());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getMatchesAgainst());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getMatchesDiff());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getSetsFor());
            cell.setCellStyle(setStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getSetsAgainst());
            cell.setCellStyle(setStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getSetsDiff());
            cell.setCellStyle(setStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getPointsFor());
            cell.setCellStyle(pointStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getPointsAgainst());
            cell.setCellStyle(pointStyle);

            cell = row.createCell(columnIndex.getAndIncrement());
            cell.setCellValue(ranking.getPointsDiff());
            cell.setCellStyle(pointStyle);

            rowIndex++;
        }
    }

    private XSSFCellStyle createExcelTeamStyle(String color) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(Color.decode(color), new DefaultIndexedColorMap()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setFontHeight((short) 200);
        font.setColor(getTextColor(color));
        style.setFont(font);

        return style;
    }

    private XSSFCellStyle createExcelTeamStyle(TeamType teamType, String color) {
        XSSFCellStyle style = createExcelTeamStyle(color);
        style.setBorderTop(TeamType.HOME.equals(teamType) ? BorderStyle.THIN : BorderStyle.NONE);
        style.setBorderBottom(TeamType.HOME.equals(teamType) ? BorderStyle.NONE : BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private XSSFCellStyle createExcelBorderedStyle(String color, TeamType teamType) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(Color.decode(color), new DefaultIndexedColorMap()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(TeamType.HOME.equals(teamType) ? BorderStyle.THIN : BorderStyle.NONE);
        style.setBorderBottom(TeamType.HOME.equals(teamType) ? BorderStyle.NONE : BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createExcelStyle(TeamType teamType) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(TeamType.HOME.equals(teamType) ? BorderStyle.THIN : BorderStyle.NONE);
        style.setBorderBottom(TeamType.HOME.equals(teamType) ? BorderStyle.NONE : BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createExcelBorderedStyle(String color) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(Color.decode(color), new DefaultIndexedColorMap()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private short getTextColor(String backgroundColor) {
        Color color = Color.decode(backgroundColor);
        short textColor;

        double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

        if (a < 0.5) {
            textColor = IndexedColors.BLACK.getIndex();
        } else {
            textColor = IndexedColors.WHITE.getIndex();
        }

        return textColor;
    }
}
