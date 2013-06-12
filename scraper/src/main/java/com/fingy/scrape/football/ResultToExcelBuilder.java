package com.fingy.scrape.football;

import java.util.Collection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ResultToExcelBuilder {

	private final Collection<MatchesByDay> matchesByDay;

	private int currentRowNumber;
	private XSSFWorkbook workbook;
	
	public ResultToExcelBuilder(FootballScrapingResult result) {
		matchesByDay = result.getMatchesByDay();
	}

	public XSSFWorkbook buildExcel() {
		initBuilder();
		
		for (MatchesByDay matches : matchesByDay) {
			createSheetFromMatchesByDay(matches);
		}
		
		return workbook;
	}

	private void initBuilder() {
		currentRowNumber = 0;
		workbook = new XSSFWorkbook();
	}

	private void createSheetFromMatchesByDay(MatchesByDay matchesByDay) {
		final String date = matchesByDay.getDate();
		final Collection<MatchesByCompetition> matchesByCompetition = matchesByDay.getMatchesByCompetition();
		
		for (MatchesByCompetition matches : matchesByCompetition) {
			final XSSFSheet sheet = workbook.createSheet("Matches for " + date);
			
			addEmptyRowToSheet(sheet);
			fillSheetFromMatchesByCompetition(sheet, matches);
		}
	}

	private void addEmptyRowToSheet(XSSFSheet sheet) {
		sheet.createRow(currentRowNumber++);
	}

	private void fillSheetFromMatchesByCompetition(XSSFSheet sheet, MatchesByCompetition matches) {
		addHeaderRow(sheet);
		addEmptyRowToSheet(sheet);
	}

	private void addHeaderRow(XSSFSheet sheet) {
		Row row = sheet.createRow(currentRowNumber++);
		Cell match = row.createCell(0);
		match.setCellValue("Match");
		
		Cell time = row.createCell(1);
		time.setCellValue("Time");
		
		Cell one = row.createCell(2);
		one.setCellValue("1");
		
		Cell x = row.createCell(3);
		x.setCellValue("x");
		
		Cell two = row.createCell(4);
		two.setCellValue("2");
	}
}
