package com.fingy.ehentai;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MangaInfoToExcelBuilder {

    private int currentRowNumber;

    private OPCPackage xlsxPackage;
    private Workbook workbook;
    private Sheet sheet;

    private Collection<MangaInfo> mangaInfos;

    public void close() throws IOException {
        xlsxPackage.close();
    }

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
        mangaInfos = new ArrayList<MangaInfo>();
    }

    private void loadExcel(String fileName) {
        try {
            File excelFile = new File(fileName);

            if (!excelFile.exists()) {
                buildExcel(Collections.<MangaInfo> emptyList());
                writeToFile(fileName);
            }

            xlsxPackage = OPCPackage.open(excelFile, PackageAccess.READ_WRITE);
            workbook = new XSSFWorkbook(xlsxPackage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MangaInfoToExcelBuilder buildExcel(Collection<MangaInfo> items) {
        initBuilder(items);

        for (MangaInfo item : mangaInfos) {
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
        mangaInfos = new ArrayList<MangaInfo>(items);
    }

    private void createAndFillRow(String... data) {
        createAndFillRowAt(currentRowNumber++, data);
    }

    private void addItemRowToSheet(MangaInfo mangaInfo) {
        createAndFillRow(mangaInfo.getTitle(), mangaInfo.getUrl(), mangaInfo.getImages(), mangaInfo.getCoverImageUrl(), mangaInfo.getTags());
    }

    public MangaInfoToExcelBuilder appendToExcel(Collection<MangaInfo> items) {
        mangaInfos = items;
        sheet = workbook.getSheetAt(0);

        for (MangaInfo item : mangaInfos) {
            appendItemRowToSheet(item);
        }

        autoSizeColumns();
        return this;
    }

    private void appendItemRowToSheet(MangaInfo mangaInfo) {
        createAndFillRowAt(sheet.getLastRowNum() + 1, mangaInfo.getTitle(), mangaInfo.getUrl(), mangaInfo.getImages(),
                           mangaInfo.getCoverImageUrl(), mangaInfo.getTags());
    }

    private void autoSizeColumns() {
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

}
