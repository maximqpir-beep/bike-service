package ru.mirea.bikeservice.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.bikeservice.model.*;
public class ExcelExporter {
    public void export(Path path, List<Client> clients, List<ServiceRequest> requests) throws IOException {
        if (path.toAbsolutePath().getParent() != null) Files.createDirectories(path.toAbsolutePath().getParent());
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle header = wb.createCellStyle(); Font font = wb.createFont(); font.setBold(true); header.setFont(font);
            header.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()); header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle money = wb.createCellStyle(); money.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
            CellStyle date = wb.createCellStyle(); date.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
            Sheet cs = sheet(wb, "Клиенты", new String[]{"ID", "ФИО", "Телефон", "Email"}, header);
            Map<Long,Client> byId = new HashMap<>();
            for (Client c : clients) { byId.put(c.getId(),c); row(cs,new Object[]{c.getId(),c.getFullName(),c.getPhone(),c.getEmail()},money,date); }
            Sheet rs = sheet(wb, "Заявки", new String[]{"ID", "ID клиента", "Клиент", "Телефон", "Марка", "Модель", "Тип", "Описание", "Статус", "Стоимость, руб.", "Создана", "Завершена"},header);
            for (ServiceRequest r : requests) {
                Client c = byId.get(r.getClientId());
                row(rs,new Object[]{r.getId(),r.getClientId(),c == null ? "" : c.getFullName(),c == null ? "" : c.getPhone(),r.getBikeBrand(),r.getBikeModel(),r.getBikeType().name(),r.getProblemDescription(),r.getStatus().name(),r.getEstimatedCost(),r.getCreatedAt(),r.getCompletedAt()},money,date);
            }
            for (Sheet s : wb) { s.createFreezePane(0,1); s.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0,s.getLastRowNum(),0,s.getRow(0).getLastCellNum()-1)); }
            // CREATE_NEW защищает уже существующий экспорт от случайной перезаписи.
            try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) { wb.write(out); }
        }
    }
    private Sheet sheet(Workbook wb, String name, String[] headings, CellStyle style) {
        Sheet sheet = wb.createSheet(name); Row row = sheet.createRow(0);
        for (int i=0;i<headings.length;i++) { Cell cell = row.createCell(i); cell.setCellValue(headings[i]); cell.setCellStyle(style); sheet.setColumnWidth(i, (i == 7 ? 45 : 22)*256); }
        return sheet;
    }
    private void row(Sheet sheet, Object[] values, CellStyle money, CellStyle date) {
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        for (int i=0;i<values.length;i++) {
            Cell cell = row.createCell(i); Object v = values[i];
            if (v == null) continue;
            if (v instanceof BigDecimal amount) { cell.setCellValue(amount.doubleValue()); cell.setCellStyle(money); }
            else if (v instanceof LocalDateTime time) { cell.setCellValue(time); cell.setCellStyle(date); }
            else if (v instanceof Long) cell.setCellValue(v.toString()); // ID сохраняется точно, без ограничения точности Excel.
            else cell.setCellValue(v.toString()); // Текст, включая начальный =, не исполняется как формула.
        }
    }
}
