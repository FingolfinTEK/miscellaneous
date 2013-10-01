package com.fingy.fragrancex.scrape;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class AbstractExcelWorksheetBasedTaskProducer {

	private static final int INDEX_OF_ID_COLUMN = 1;
	private static final int INDEX_OF_SIZE_COLUMN = 3;

	private int startFromRow;
	private Workbook workbook;

	public AbstractExcelWorksheetBasedTaskProducer() {
		startFromRow = 0;
		workbook = new XSSFWorkbook();
	}

	public AbstractExcelWorksheetBasedTaskProducer forWorkbook(Workbook workbook) throws InvalidFormatException,
			IOException {
		this.workbook = workbook;
		return this;
	}

	public AbstractExcelWorksheetBasedTaskProducer startingFromRowNumber(int startFromRow) {
		this.startFromRow = startFromRow;
		return this;
	}

	public void produce() {
		Sheet sheet = workbook.getSheetAt(0);

		for (int rowIndex = startFromRow; rowIndex < sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);

			String id = readIdFromRow(row);
			String sizeOrType = readSizeOrTypeFromRow(row);

			if (id != null && !"".equals(id))
				doWithTask(createTaskFromRowData(rowIndex, id, sizeOrType));
		}

	}

	private String readIdFromRow(Row row) {
		Cell idCell = row.getCell(INDEX_OF_ID_COLUMN);

		switch (idCell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			return Long.toString(Math.round(idCell.getNumericCellValue()));
		default:
			return idCell.getStringCellValue();
		}
	}

	private String readSizeOrTypeFromRow(Row row) {
		Cell sizeOrTypeCell = row.getCell(INDEX_OF_SIZE_COLUMN);
		return sizeOrTypeCell.getStringCellValue().replaceAll("--", "").replaceAll(" +", " ").trim();
	}

	private FragrancexTask createTaskFromRowData(int i, String id, String sizeOrType) {
		return new FragrancexTask(i, id, sizeOrType);
	}

	public abstract void doWithTask(FragrancexTask createTaskFromRowData);

}
