package com.fingy.ehentai;

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

public class MangaInfoToExcelBuilder {

    private int                   currentRowNumber;

    private Workbook              workbook;
    private Sheet                 sheet;

    private Collection<MangaInfo> MangaInfos;

    public MangaInfoToExcelBuilder writeToFile(String fileName) throws FileNotFoundException, IOException {
        FileOutputStream fileStream = new FileOutputStream(fileName);
        workbook.write(fileStream);
        return this;
    }

    public MangaInfoToExcelBuilder openExcel(String fileName) {
        resetBuilder();
        loadExcel(fileName);
        return this;
    }

    private void resetBuilder() {
        currentRowNumber = 0;
        MangaInfos = new ArrayList<MangaInfo>();
    }

    private void loadExcel(String fileName) {
        try {
            File excelFile = new File(fileName);

            if (!excelFile.exists()) {
                buildExcel(Collections.<MangaInfo> emptyList());
                writeToFile(fileName);
            }

            workbook = WorkbookFactory.create(excelFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MangaInfoToExcelBuilder buildExcel(Collection<MangaInfo> items) {
        initBuilder(items);

        addHeaderRow();

        for (MangaInfo item : MangaInfos) {
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

    private void initBuilder(Collection<? extends MangaInfo> items) {
        currentRowNumber = 0;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Items");
        MangaInfos = new ArrayList<MangaInfo>(items);
    }

    private void addHeaderRow() {
        createAndFillRow("Name", "Phone number");
    }

    private void createAndFillRow(String... data) {
        createAndFillRowAt(currentRowNumber++, data);
    }

    private void addItemRowToSheet(MangaInfo mangaInfo) {
        createAndFillRow(mangaInfo.getTitle(), mangaInfo.getUrl(), mangaInfo.getImages(), mangaInfo.getCoverImageUrl(), mangaInfo.getTags());
    }

}
