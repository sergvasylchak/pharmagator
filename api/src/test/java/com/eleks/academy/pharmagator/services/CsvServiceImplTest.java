package com.eleks.academy.pharmagator.services;


import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.entities.Pharmacy;
import com.eleks.academy.pharmagator.exceptions.ExportExceptions;
import com.eleks.academy.pharmagator.exceptions.UploadExceptions;
import com.eleks.academy.pharmagator.projections.MedicinePrice;
import com.eleks.academy.pharmagator.repositories.PharmacyRepository;
import com.eleks.academy.pharmagator.repositories.PriceRepository;
import com.eleks.academy.pharmagator.services.projections.MedicinePriceImpl;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class CsvServiceImplTest {

    @InjectMocks
    private CsvServiceImpl csvService;

    @Mock
    private ImportServiceImpl importService;

    private static final String TYPE = "text/csv";

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private BeanListProcessor<MedicineDto> rowProcessor;

    @Mock
    private CsvParser csvParser;

    private List<Pharmacy> pharmacies;
    private List<MedicinePrice> prices;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        pharmacies = Arrays.asList(
                new Pharmacy(1L, "ANC", "Link"),
                new Pharmacy(2L, "3I", "Link"),
                new Pharmacy(3L, "Liki24", "Link")
        );
        prices = Arrays.asList(
                new MedicinePriceImpl("Aspirin", 1L, BigDecimal.valueOf(25.25)),
                new MedicinePriceImpl("Aspirin", 2L, BigDecimal.valueOf(35.25)),
                new MedicinePriceImpl("Aspirin", 3L, BigDecimal.valueOf(45.25)),
                new MedicinePriceImpl("Fervex", 1L, BigDecimal.valueOf(22.25)),
                new MedicinePriceImpl("Fervex", 2L, BigDecimal.valueOf(32.25)),
                new MedicinePriceImpl("Fervex", 3L, BigDecimal.valueOf(42.25))
        );
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "prices", "pharmacies", "medicines");
    }

    @Test
    @SneakyThrows
    void dataImport_isInvalidFormat() {
        String filepath = "src/test/resources/import/import.csv";

        InputStream inputStream = new FileInputStream(filepath);
        MultipartFile multipartFile = new MockMultipartFile(filepath, "", "pdf", inputStream);

        UploadExceptions exception = assertThrows(UploadExceptions.class, () -> csvService.parseAndSave(multipartFile));

        assertEquals(UploadExceptions.Error.INVALID_FILE_FORMAT, exception.getError());
    }

    @Test
    @SneakyThrows
    void dataImport_throwIOException() {
        MockMultipartFile multipartFile = Mockito.mock(MockMultipartFile.class);

        when(multipartFile.getContentType()).thenReturn(TYPE);
        when(multipartFile.getInputStream()).thenThrow(new IOException());

        UploadExceptions exception = assertThrows(UploadExceptions.class, () -> csvService.parseAndSave(multipartFile));

        assertEquals(UploadExceptions.Error.SAVE_WAS_NOT_SUCCESSFUL, exception.getError());
    }

    @Test
    @SneakyThrows
    void dataExport_isOk() {
        String filepath = "src/test/resources/export/export.csv";
        File file = new File(filepath);

        when(pharmacyRepository.findAll()).thenReturn(pharmacies);
        pharmacies.forEach(pharmacy ->
                when(pharmacyRepository.findById(pharmacy.getId())).thenReturn(Optional.of(pharmacy)));
        when(priceRepository.findAllMedicinesPrices()).thenReturn(prices);

        byte[] bytes = csvService.export();

        FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
        fileOutputStream.write(bytes);

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        CsvParser parser = new CsvParser(settings);
        List<Record> records = parser.parseAllRecords(file);

        assertEquals("title", records.get(0).getString(0));

        records.forEach(record -> prices.stream()
                .filter(medicinePrice ->
                        medicinePrice.getTitle().equals(record.getString(0)) &&
                                pharmacyRepository.findById(medicinePrice.getPharmacyId())
                                        .map(Pharmacy::getName).equals(Optional.of(record.getString(2))))
                .findFirst()
                .ifPresent(medicinePrice -> assertEquals(medicinePrice.getPrice(), record.getBigDecimal(1))));
    }

    @Test
    @SneakyThrows
    void dataExport_throwsIOException() {
        ByteArrayOutputStream byteArrayOutputStream = Mockito.mock(ByteArrayOutputStream.class);

        FieldUtils.writeField(csvService, "byteArrayOutputStream", byteArrayOutputStream, true);
        doThrow(new IOException()).when(byteArrayOutputStream).write(any(byte[].class));

        ExportExceptions exception = assertThrows(ExportExceptions.class, () -> csvService.export());

        assertEquals(ExportExceptions.Error.WRITE_TO_CSV_IS_BAD, exception.getError());
    }

}
