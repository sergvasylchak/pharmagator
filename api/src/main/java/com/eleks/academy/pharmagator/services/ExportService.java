package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.entities.Pharmacy;
import com.eleks.academy.pharmagator.projections.MedicinePrice;
import com.eleks.academy.pharmagator.repositories.PharmacyRepository;
import com.eleks.academy.pharmagator.repositories.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ExportService {
    private final PriceRepository priceRepository;
    private final PharmacyRepository pharmacyRepository;

    public XSSFWorkbook getExportData() {
        Map<String, Map<Long, BigDecimal>> prices;

        prices = getMapPricesFromDatabase();

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet();

        XSSFCellStyle headerStyle = createHeaderStyle(workbook);
        XSSFCellStyle firstColumnStyle = createFirstColumnStyle(workbook);

        sheet.setColumnWidth(0, 20000);

        XSSFRow header = sheet.createRow(0);

        AtomicInteger cellIndex = new AtomicInteger(1);
        AtomicInteger rowIndex = new AtomicInteger(1);

        List<Pharmacy> pharmacies = pharmacyRepository.findAll();

        HashMap<Long, Integer> pharmacyColumnMapping = new HashMap<>(pharmacies.size());

        pharmacies.forEach(pharmacy -> buildHeaderCell(sheet, headerStyle, header, cellIndex, pharmacyColumnMapping, pharmacy));

        prices.forEach((medicineTitle, phs) -> buildMedicineRow(sheet, firstColumnStyle, rowIndex, pharmacyColumnMapping, medicineTitle, phs));

        setupConditionalFormatting(prices, sheet);

        return workbook;
    }

    public Map<String, Map<Long, BigDecimal>> getMapPricesFromDatabase() {
        return priceRepository.findAllMedicinesPrices()
                .stream()
                .collect(Collectors.groupingBy(MedicinePrice::getTitle,
                        Collectors.toMap(MedicinePrice::getPharmacyId, MedicinePrice::getPrice)));
    }

    private void buildMedicineRow(XSSFSheet sheet, XSSFCellStyle firstColumnStyle, AtomicInteger rowIndex, HashMap<Long, Integer> pharmacyColumnMapping, String medicineTitle, Map<Long, BigDecimal> phs) {
        XSSFRow row = sheet.createRow(rowIndex.getAndIncrement());

        XSSFCell medicineCell = row.createCell(0);
        medicineCell.setCellValue(medicineTitle);

        medicineCell.setCellStyle(firstColumnStyle);

        phs.forEach((pharmacyId, price) -> {
            XSSFCell cell = row.createCell(pharmacyColumnMapping.get(pharmacyId));
            cell.setCellValue(price.doubleValue());
        });
    }

    private void buildHeaderCell(XSSFSheet sheet, XSSFCellStyle headerStyle, XSSFRow header, AtomicInteger cellIndex, HashMap<Long, Integer> pharmacyColumnMapping, Pharmacy pharmacy) {
        int index = cellIndex.getAndIncrement();

        pharmacyColumnMapping.put(pharmacy.getId(), index);

        XSSFCell cell = header.createCell(index);

        sheet.setColumnWidth(index, 5000);

        cell.setCellValue(pharmacy.getName());

        cell.setCellStyle(headerStyle);
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    private XSSFCellStyle createFirstColumnStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    private void setupConditionalFormatting(Map<String, Map<Long, BigDecimal>> prices, XSSFSheet sheet) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule("AND(ISNUMBER(B2), B2=MIN($B2:$Z2))");
        PatternFormatting fill = rule.createPatternFormatting();
        fill.setFillBackgroundColor(IndexedColors.RED.index);
        fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        FontFormatting fontFormatting = rule.createFontFormatting();
        fontFormatting.setFontColorIndex(IndexedColors.WHITE.index);

        ConditionalFormattingRule[] cfRules = new ConditionalFormattingRule[]{rule};

        CellRangeAddress[] regions = new CellRangeAddress[]{CellRangeAddress.valueOf("B2:Z" + (prices.size() + 1))};

        sheetCF.addConditionalFormatting(regions, cfRules);
    }
}
