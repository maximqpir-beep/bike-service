package ru.mirea.bikeservice.fx.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.bikeservice.fx.model.RequestRow;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public final class ExcelExporter {
    private static final String[] HEADERS = {
            "ID", "Клиент", "Телефон", "Велосипед", "Тип", "Описание",
            "Статус", "Стоимость", "Дата создания", "Дата завершения"
    };

    public void export(Path target, List<RequestRow> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Заявки");
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.length; index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(HEADERS[index]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (RequestRow value : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(String.valueOf(value.id()));
                row.createCell(1).setCellValue(value.clientName());
                row.createCell(2).setCellValue(value.phone());
                row.createCell(3).setCellValue(value.bike());
                row.createCell(4).setCellValue(value.request().getBikeType().toString());
                row.createCell(5).setCellValue(value.request().getProblemDescription());
                row.createCell(6).setCellValue(value.request().getStatus().toString());
                if (value.request().getEstimatedCost() != null) {
                    Cell cost = row.createCell(7);
                    cost.setCellValue(value.request().getEstimatedCost().doubleValue());
                    cost.setCellStyle(moneyStyle);
                }
                writeDate(row.createCell(8), value.request().getCreatedAt(), dateStyle);
                writeDate(row.createCell(9), value.request().getCompletedAt(), dateStyle);
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, HEADERS.length - 1));
            for (int index = 0; index < HEADERS.length; index++) {
                sheet.setColumnWidth(index, (index == 5 ? 46 : 20) * 256);
            }
            try (OutputStream output = Files.newOutputStream(target)) {
                workbook.write(output);
            }
        }
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void writeDate(Cell cell, LocalDateTime value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }
}
