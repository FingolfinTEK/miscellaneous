package com.fingy.aprod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ContactToExcelBuilder {

	private int currentRowNumber;

	private Workbook workbook;
	private Sheet sheet;

	private Collection<Contact> contacts;

	public ContactToExcelBuilder writeToFile(String fileName) throws FileNotFoundException, IOException {
		FileOutputStream fileStream = new FileOutputStream(fileName);
		workbook.write(fileStream);
		return this;
	}

	public ContactToExcelBuilder openExcel(String fileName) {
		resetBuilder();
		loadExcel(fileName);
		return this;
	}

	private void resetBuilder() {
		currentRowNumber = 0;
		contacts = new ArrayList<Contact>();
	}

	private void loadExcel(String fileName) {
		try {
			File excelFile = new File(fileName);

			if (!excelFile.exists()) {
				buildExcel(Collections.<Contact> emptyList());
				writeToFile(fileName);
			}

			workbook = WorkbookFactory.create(excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ContactToExcelBuilder appendToExcel(Collection<Contact> items) {
		contacts = items;
		sheet = workbook.getSheetAt(0);

		addHeaderRowIfNeeded();

		for (Contact item : contacts) {
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

	private void appendItemRowToSheet(Contact contact) {
		createAndFillRowAt(
			sheet.getLastRowNum() + 1,contact.getName(), contact.getPhoneNumber());
	}

	public ContactToExcelBuilder buildExcel(Collection<Contact> items) {
		initBuilder(items);

		addHeaderRow();

		for (Contact item : contacts) {
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

	private void initBuilder(Collection<? extends Contact> items) {
		currentRowNumber = 0;
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Items");
		contacts = new ArrayList<Contact>(items);
	}

	private void addHeaderRow() {
		createAndFillRow(
			"Name",
			"Phone number");
	}

	private void createAndFillRow(String... data) {
		createAndFillRowAt(
			currentRowNumber++,
			data);
	}

	private void addItemRowToSheet(Contact contact) {
		createAndFillRow(contact.getName(), contact.getPhoneNumber());
	}

}
