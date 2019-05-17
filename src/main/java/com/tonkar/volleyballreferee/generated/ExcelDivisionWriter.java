package com.tonkar.volleyballreferee.generated;

import com.tonkar.volleyballreferee.dto.Ranking;
import com.tonkar.volleyballreferee.entity.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExcelDivisionWriter {

    private String        divisionName;
    private XSSFWorkbook  workbook;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle matchStyle;
    private XSSFCellStyle setStyle;
    private XSSFCellStyle pointStyle;
    private XSSFCellStyle homeDefaultStyle;
    private XSSFCellStyle guestDefaultStyle;
    private XSSFCellStyle homeSetStyle;
    private XSSFCellStyle guestSetStyle;
    private XSSFCellStyle homePointStyle;
    private XSSFCellStyle guestPointStyle;

    private ExcelDivisionWriter(String divisionName, XSSFWorkbook workbook) {
        this.divisionName = divisionName;
        this.workbook = workbook;
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

    public static FileWrapper writeExcelDivision(String divisionName, List<Game> games) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        ExcelDivisionWriter excelDivisionWriter = new ExcelDivisionWriter(divisionName, workbook);
        excelDivisionWriter.createMatchesExcelSheet(games);
        excelDivisionWriter.createRankingsExcelSheet(games);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);

        String filename = String.format(Locale.getDefault(), "%s.xlsx", divisionName);
        return new FileWrapper(filename, byteArrayOutputStream.toByteArray());
    }

    private void createMatchesExcelSheet(List<Game> games) {
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        String[] gamesHeader = { "Date", "Team", "Total Sets", "Set 1", "Set 2", "Set 3", "Set 4", "Set 5", "Total Points" };

        XSSFSheet sheet = workbook.createSheet(divisionName + " - Matches");
        XSSFRow row = sheet.createRow(0);

        for (int index = 0; index < gamesHeader.length; index++) {
            XSSFCell cell = row.createCell(index);
            cell.setCellValue(gamesHeader[index]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;

        for (Game game : games) {
            row = sheet.createRow(rowIndex);

            XSSFCell cell = row.createCell(0);
            cell.setCellValue(formatter.format(game.getScheduledAt()));
            cell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex+1, 0, 0));

            createMatchExcelRow(row, TeamType.HOME, game);
            rowIndex++;

            row = sheet.createRow(rowIndex);

            createMatchExcelRow(row, TeamType.GUEST, game);
            rowIndex++;
        }
    }

    private void createMatchExcelRow(XSSFRow row, TeamType teamType, Game game) {
        XSSFCell cell = row.createCell(1);
        cell.setCellValue(game.getTeam(teamType).getName().trim());
        cell.setCellStyle(createExcelTeamStyle(teamType, game.getTeam(teamType).getColor()));

        cell = row.createCell(2);
        cell.setCellValue(TeamType.HOME.equals(teamType) ? game.getHomeSets() : game.getGuestSets());
        cell.setCellStyle(TeamType.HOME.equals(teamType) ? homeSetStyle : guestSetStyle);

        createSetsExcelRow(row, teamType, game);
    }

    private void createSetsExcelRow(XSSFRow row, TeamType teamType, Game game) {
        int columnIndex = 3;

        int total = 0;

        for (Set set : game.getSets()) {
            int points = set.getPoints(teamType);
            total += points;
            XSSFCell cell = row.createCell(columnIndex);
            cell.setCellValue(points);
            cell.setCellStyle(TeamType.HOME.equals(teamType) ? homeDefaultStyle : guestDefaultStyle);
            columnIndex++;
        }

        while (columnIndex < 8) {
            XSSFCell cell = row.createCell(columnIndex);
            cell.setCellValue(0);
            cell.setCellStyle(TeamType.HOME.equals(teamType) ? homeDefaultStyle : guestDefaultStyle);
            columnIndex++;
        }

        XSSFCell cell = row.createCell(columnIndex);
        cell.setCellValue(total);
        cell.setCellStyle(TeamType.HOME.equals(teamType) ? homePointStyle : guestPointStyle);
    }

    private void createRankingsExcelSheet(List<Game> games) {
        String[] rankingsHeader = { "Team", "Matches Played", "Matches Won", "Matches Lost", "Matches Diff", "Sets Won", "Sets Lost", "Sets Diff", "Points Won", "Points Lost", "Points Diff" };

        XSSFSheet sheet = workbook.createSheet(divisionName + " - Rankings");
        XSSFRow row = sheet.createRow(0);

        for (int index = 0; index < rankingsHeader.length; index++) {
            XSSFCell cell = row.createCell(index);
            cell.setCellValue(rankingsHeader[index]);
            cell.setCellStyle(headerStyle);
        }

        Rankings rankings = new Rankings();
        games.forEach(rankings::addGame);

        int rowIndex = 1;

        for (Ranking ranking : rankings.list()) {
            row = sheet.createRow(rowIndex);
            int columnIndex = 0;

            XSSFCell cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getTeamName());
            cell.setCellStyle(createExcelTeamStyle(ranking.getTeamColor()));

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getMatchesFor() + ranking.getMatchesAgainst());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getMatchesFor());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getMatchesAgainst());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getMatchesDiff());
            cell.setCellStyle(matchStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getSetsFor());
            cell.setCellStyle(setStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getSetsAgainst());
            cell.setCellStyle(setStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getSetsDiff());
            cell.setCellStyle(setStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getPointsFor());
            cell.setCellStyle(pointStyle);

            cell = row.createCell(columnIndex++);
            cell.setCellValue(ranking.getPointsAgainst());
            cell.setCellStyle(pointStyle);

            cell = row.createCell(columnIndex++);
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

        double a = 1 - ( 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

        if (a < 0.5) {
            textColor = IndexedColors.BLACK.getIndex();
        } else {
            textColor = IndexedColors.WHITE.getIndex();
        }

        return textColor;
    }
}
