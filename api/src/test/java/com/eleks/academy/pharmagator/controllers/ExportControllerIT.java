package com.eleks.academy.pharmagator.controllers;

import com.eleks.academy.pharmagator.exceptions.ExportExceptions;
import com.eleks.academy.pharmagator.exceptions.GlobalExceptionHandler;
import com.eleks.academy.pharmagator.services.CsvServiceImpl;
import com.eleks.academy.pharmagator.services.PDFExportService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportControllerIT {

    @InjectMocks
    private ExportController exportController;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PDFExportService pdfExportService;

    @Mock
    private CsvServiceImpl csvService;

    private final String PDF_URI = "/export/pdf";
    private final String CSV_URI = "/export/csv";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "prices", "pharmacies", "medicines");
    }

    @Test
    @SneakyThrows
    void exportToPdf_isOk() {
        byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        when(pdfExportService.export()).thenReturn(bytes);

        this.mockMvc.perform(MockMvcRequestBuilders.get(PDF_URI))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        then(pdfExportService).should().export();
    }

    @Test
    @SneakyThrows
    void exportToPdf_getPdfWriterIsBad() {
        when(pdfExportService.export()).thenThrow(new ExportExceptions(ExportExceptions.Error.GET_PDF_WRITER_IS_BAD));

        this.mockMvc.perform(MockMvcRequestBuilders.get(PDF_URI))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        then(pdfExportService).should().export();
    }

    @Test
    @SneakyThrows
    void exportToPdf_createFontIsInvalid() {
        when(pdfExportService.export()).thenThrow(new ExportExceptions(ExportExceptions.Error.INVALID_FONT));

        this.mockMvc.perform(MockMvcRequestBuilders.get(PDF_URI))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        then(pdfExportService).should().export();
    }

    @Test
    @SneakyThrows
    void exportToCsv_isOk() {
        byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        when(csvService.export()).thenReturn(bytes);

        this.mockMvc.perform(MockMvcRequestBuilders.get(CSV_URI))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        then(csvService).should().export();
    }

    @Test
    @SneakyThrows
    void exportToCsv_writeToCsvIsBad() {
        when(csvService.export()).thenThrow(new ExportExceptions(ExportExceptions.Error.WRITE_TO_CSV_IS_BAD));

        this.mockMvc.perform(MockMvcRequestBuilders.get(CSV_URI))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        then(csvService).should().export();
    }

}
