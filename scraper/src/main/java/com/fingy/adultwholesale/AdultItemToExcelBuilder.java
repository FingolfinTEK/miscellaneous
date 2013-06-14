package com.fingy.adultwholesale;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AdultItemToExcelBuilder {

	private int currentRowNumber;

	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	private Collection<AdultItem> adultItems;

	public AdultItemToExcelBuilder writeToFile(String fileName) throws FileNotFoundException, IOException {
		workbook.write(new FileOutputStream(fileName));
		return this;
	}

	public AdultItemToExcelBuilder buildExcel(Collection<AdultItem> items) {
		initBuilder(items);

		addHeaderRow();

		for (AdultItem item : adultItems) {
			addItemRowToSheet(item);
		}

		autoSizeColumns();
		return this;
	}

	private void autoSizeColumns() {
		for (int i = 0; i < 5; i++) {
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
		createAndFillRow("Category", "Item #", "Title", "Price", "Stock status", "Description", "Image URL", "Product URL");
	}

	private void createAndFillRow(String category, String id, String title, String price, String stockStatus, String description, String imageUrl,
			String productUrl) {
		Row row = sheet.createRow(currentRowNumber++);

		Cell categoryCell = row.createCell(0);
		categoryCell.setCellValue(category);

		Cell itemIdCell = row.createCell(1);
		itemIdCell.setCellValue(id);

		Cell titleCell = row.createCell(2);
		titleCell.setCellValue(title);

		Cell priceCell = row.createCell(3);
		priceCell.setCellValue(price);

		Cell stockStatusCell = row.createCell(4);
		stockStatusCell.setCellValue(stockStatus);

		Cell descriptionCell = row.createCell(5);
		descriptionCell.setCellValue(description);

		Cell imageUrlCell = row.createCell(6);
		imageUrlCell.setCellValue(imageUrl);

		Cell productUrlCell = row.createCell(7);
		productUrlCell.setCellValue(productUrl);
	}

	private void addItemRowToSheet(AdultItem adultItem) {
		createAndFillRow(adultItem.getCategory(), adultItem.getId(), adultItem.getTitle(), adultItem.getPrice(), adultItem.getStockStatus(),
				adultItem.getDescription(), adultItem.getImageUrl(), adultItem.getProductUrl());
	}
}
