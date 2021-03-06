package com.fingy.adultwholesale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AdultItemToExcelBuilder {

	private int currentRowNumber;

	private Workbook workbook;
	private Sheet sheet;

	private Collection<AdultItem> adultItems;

	public AdultItemToExcelBuilder writeToFile(String fileName) throws FileNotFoundException, IOException {
		FileOutputStream fileStream = new FileOutputStream(fileName);
		workbook.write(fileStream);
		return this;
	}

	public AdultItemToExcelBuilder openExcel(String fileName) {
		resetBuilder();
		loadExcel(fileName);
		return this;
	}

	private void resetBuilder() {
		currentRowNumber = 0;
		adultItems = new ArrayList<AdultItem>();
	}

	private void loadExcel(String fileName) {
		try {
			File excelFile = new File(fileName);

			if (!excelFile.exists()) {
				buildExcel(Collections.<AdultItem> emptyList());
				writeToFile(fileName);
			}

			workbook = WorkbookFactory.create(excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AdultItemToExcelBuilder appendToExcel(Collection<AdultItem> items) {
		adultItems = items;
		sheet = workbook.getSheetAt(0);

		addHeaderRowIfNeeded();

		for (AdultItem item : adultItems) {
			appendItemRowToSheet(item);
		}

		autoSizeColumns();
		return this;
	}

	private void addHeaderRowIfNeeded() {
		if (sheet.getLastRowNum() == 0) {
			addHeaderRow();
		}
	}

	private void appendItemRowToSheet(AdultItem adultItem) {
		createAndFillRowAt(
			sheet.getLastRowNum() + 1,
			adultItem.getCategory(),
			adultItem.getId(),
			adultItem.getTitle(),
			adultItem.getPrice(),
			adultItem.getUpc(),
			adultItem.getStockStatus(),
			adultItem.getDescription(),
			adultItem.getImageUrl(),
			adultItem.getProductUrl());
	}

	public AdultItemToExcelBuilder buildExcel(Collection<AdultItem> items) {
		initBuilder(items);

		addHeaderRow();

		for (AdultItem item : adultItems) {
			addItemRowToSheet(item);
		}

		return this;
	}

	private void createAndFillRowAt(int rowNumber, String... values) {
		Row row = sheet.createRow(rowNumber);

		for (int cellIndex = 0; cellIndex < values.length; cellIndex++) {
			String value = values[cellIndex];
			row.createCell(cellIndex).setCellValue(value);
		}
	}

	private void autoSizeColumns() {
		for (int i = 0; i < 6; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void initBuilder(Collection<? extends AdultItem> items) {
		currentRowNumber = 0;
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Items");
		adultItems = new ArrayList<AdultItem>(items);
	}

	private void addHeaderRow() {
		createAndFillRow(
			"Category",
			"Item #",
			"Title",
			"Price",
			"UPC",
			"Stock status",
			"Description",
			"Image URL",
			"Product URL");
	}

	private void createAndFillRow(String category, String id, String title, String price, String upc,
			String stockStatus, String description, String imageUrl, String productUrl) {
		createAndFillRowAt(
			currentRowNumber++,
			category,
			id,
			title,
			price,
			upc,
			stockStatus,
			description,
			imageUrl,
			productUrl);
	}

	private void addItemRowToSheet(AdultItem adultItem) {
		createAndFillRow(
			adultItem.getCategory(),
			adultItem.getId(),
			adultItem.getTitle(),
			adultItem.getPrice(),
			adultItem.getUpc(),
			adultItem.getStockStatus(),
			adultItem.getDescription(),
			adultItem.getImageUrl(),
			adultItem.getProductUrl());
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new AdultItemToExcelBuilder().openExcel("wholesale.xlsx")
				.appendToExcel(Arrays.asList(new AdultItem("asd", "", "", "", "", "", "", "", "")))
				.writeToFile("wholesale2.xlsx");
	}
}
