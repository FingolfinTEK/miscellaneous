package com.fingy.adultwholesale;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AdultItemToExcelBuilder {

	private final Collection<AdultItem> adultItems;

	private int currentRowNumber;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	
	public AdultItemToExcelBuilder(Collection<AdultItem> items) {
		adultItems = new ArrayList<AdultItem>(items);
	}

	public XSSFWorkbook buildExcel() {
		initBuilder();
		
		addHeaderRow();
		for (AdultItem item : adultItems) {
			addItemToSheet(item);
		}
		
		return workbook;
	}

	private void initBuilder() {
		currentRowNumber = 0;
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Items");
	}

	private void addHeaderRow() {
		Row row = sheet.createRow(currentRowNumber++);
		Cell categoryCell = row.createCell(0);
		categoryCell.setCellValue("Category");
		
		Cell itemIdCell = row.createCell(1);
		itemIdCell.setCellValue("Item #");
		
		Cell titleCell = row.createCell(2);
		titleCell.setCellValue("Title");
		
		Cell priceCell = row.createCell(3);
		priceCell.setCellValue("Price");
		
		Cell stockStatusCell = row.createCell(4);
		stockStatusCell.setCellValue("Stock status");

		Cell descriptionCell = row.createCell(5);
		descriptionCell.setCellValue("Description");
		
		Cell imageUrlCell = row.createCell(4);
		imageUrlCell.setCellValue("Image URL");

		Cell productUrlCell = row.createCell(5);
		productUrlCell.setCellValue("Product URL");
	}

	private void addItemToSheet(AdultItem adultItem) { 
		Row row = sheet.createRow(currentRowNumber++);
		Cell categoryCell = row.createCell(0);
		categoryCell.setCellValue(adultItem.getCategory());
		
		Cell itemIdCell = row.createCell(1);
		itemIdCell.setCellValue(adultItem.getId());
		
		Cell titleCell = row.createCell(2);
		titleCell.setCellValue(adultItem.getTitle());
		
		Cell priceCell = row.createCell(3);
		priceCell.setCellValue(adultItem.getPrice());
		
		Cell stockStatusCell = row.createCell(4);
		stockStatusCell.setCellValue(adultItem.getStockStatus());

		Cell descriptionCell = row.createCell(5);
		descriptionCell.setCellValue(adultItem.getDescription());
		
		Cell imageUrlCell = row.createCell(4);
		imageUrlCell.setCellValue(adultItem.getImageUrl());

		Cell productUrlCell = row.createCell(5);
		productUrlCell.setCellValue(adultItem.getProductUrl());
	}
}
