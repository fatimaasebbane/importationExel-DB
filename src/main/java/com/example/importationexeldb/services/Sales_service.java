package com.example.importationexeldb.services;

import com.example.importationexeldb.DAO.RepositirySales;
import com.example.importationexeldb.models.Sales;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static java.sql.JDBCType.NUMERIC;
import static javax.management.openmbean.SimpleType.STRING;

@Service
public class Sales_service {
    @Autowired
    RepositirySales repositorySales;

    public void processExcelFile(InputStream fileStream) {

        try (Workbook workbook = WorkbookFactory.create(fileStream)) {
            Workbook errorWorkbook = new XSSFWorkbook();
            Sheet errorSheet = errorWorkbook.createSheet("Error_Log");
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Row originalHeaderRow = sheet.getRow(0);
            Row errorHeaderRow = errorSheet.createRow(0);
            copyRow(originalHeaderRow, errorHeaderRow, true);


            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    continue;
                }
                Sales sale = new Sales();

                try {
                    String id = getStringCellValue(row.getCell(0));
                    if (id != null && !repositorySales.existsById(id)) {
                        sale.setId(getStringCellValue(row.getCell(0)));
                    } else {
                        addErrorToSheet(errorSheet, row, "This ID value already exists in the database");
                        continue;
                    }
                    sale.setFournisseur(getStringCellValue(row.getCell(1)));
                    sale.setArticle(getStringCellValue(row.getCell(2)));
                    Integer quantity = getIntCellValue(row.getCell(3));
                    if (quantity != null) {
                        sale.setQuentite(quantity);
                    } else {
                        addErrorToSheet(errorSheet, row, "Units should be an integer");
                        continue;
                    }
                    Float pu = getFloatCellValue(row.getCell(4));
                    if (pu != null) {
                        sale.setPrix_unitaire(pu);
                    } else {
                        addErrorToSheet(errorSheet, row, "Unit Cost should be of type float");
                        continue;
                    }
                    Float total = getFloatCellValue(row.getCell(5));
                    if (total != null) {
                        sale.setTotal(total);
                    } else {
                        addErrorToSheet(errorSheet, row, "The total should be of type float");
                        continue;
                    }
                    Cell dateCell = row.getCell(6);
                    if (dateCell != null) {
                        if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                            sale.setOrderDate(dateCell.getDateCellValue());
                        } else if (dateCell.getCellType() == CellType.STRING) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
                            try {
                                Date parsedDate = dateFormat.parse(dateCell.getStringCellValue());
                                sale.setOrderDate(parsedDate);
                            } catch (Exception ex) {
                                addErrorToSheet(errorSheet, row, "Invalid date format , it should be : yyyy-mm-dd");
                                continue;
                            }
                        }
                    }
                    boolean hasEmptyColumn = false;
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        if (row.getCell(i) == null || row.getCell(i).getCellType() == CellType.BLANK) {
                            addErrorToSheet(errorSheet, row, "All fields are necessary");
                            hasEmptyColumn = true;
                        }
                    }
                    if (hasEmptyColumn) {
                        continue;
                    }

                    repositorySales.save(sale);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream("error_log.xlsx")) {
                errorWorkbook.write(fileOut);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addErrorToSheet(Sheet errorSheet, Row row, String errorMessage) {
        int lastCellNum = row.getLastCellNum() >= 0 ? row.getLastCellNum() : 0;
        Row errorRow = errorSheet.createRow(errorSheet.getLastRowNum() + 1);
        copyRow(row, errorRow, false);
        errorRow.createCell(lastCellNum).setCellValue(errorMessage);
    }

    private void copyRow(Row sourceRow, Row targetRow, boolean includeNotesColumn) {
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = targetRow.createCell(i);
            if (sourceCell != null) {
                switch (sourceCell.getCellType()) {
                    case STRING:
                        targetCell.setCellValue(sourceCell.getStringCellValue());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(sourceCell)) {
                            targetCell.setCellValue(sourceCell.getDateCellValue());
                        } else {
                            targetCell.setCellValue(sourceCell.getNumericCellValue());
                        }
                        break;
                    case BOOLEAN:
                        targetCell.setCellValue(sourceCell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        targetCell.setCellFormula(sourceCell.getCellFormula());
                        break;
                    default:
                        break;
                }
                targetCell.setCellType(sourceCell.getCellType());
            }
        }
        if (includeNotesColumn) {
            targetRow.createCell(targetRow.getLastCellNum(), CellType.STRING).setCellValue("Notes");
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell != null) {
            return cell.getStringCellValue();
        }
        return null;
    }

    private Integer getIntCellValue(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().matches("\\d+")) {
                return Integer.valueOf(cell.getStringCellValue());
            }
        }
        return null;
    }

    private Float getFloatCellValue(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (float) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().matches("\\d+(\\.\\d+)?")) {
                return Float.valueOf(cell.getStringCellValue());
            }
        }
        return null;
    }
}
