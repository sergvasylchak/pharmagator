package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.entities.PriceId;
import com.eleks.academy.pharmagator.repositories.MedicineRepository;
import com.eleks.academy.pharmagator.repositories.PharmacyRepository;
import com.eleks.academy.pharmagator.repositories.PriceRepository;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class CsvServiceIT {

    @Autowired
    private CsvServiceImpl csvService;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    private static final String TYPE = "text/csv";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "prices", "pharmacies", "medicines");
    }

    @Test
    @SneakyThrows
    void dataImport_isOk() {
        String filepath = "src/test/resources/import/import.csv";
        File file = new File(filepath);

        InputStream inputStream = new FileInputStream(filepath);
        MultipartFile multipartFile = new MockMultipartFile(filepath, "", TYPE, inputStream);

        String expectedMessage = csvService.parseAndSave(multipartFile);

        assertEquals(expectedMessage, "Save of file " + multipartFile.getOriginalFilename() + " was successful");

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvParser parser = new CsvParser(settings);
        List<Record> records = parser.parseAllRecords(file);

        records.forEach(record -> medicineRepository.findByTitle(record.getString(0))
                .flatMap(medicine -> pharmacyRepository.findByName(record.getString(3))
                        .flatMap(pharmacy -> priceRepository.findById(new PriceId(pharmacy.getId(), medicine.getId()))))
                .ifPresent(price -> {
                    assertEquals(price.getPrice(), record.getBigDecimal(1));
                    assertEquals(price.getExternalId(), record.getString(2));
                }));
    }

}
