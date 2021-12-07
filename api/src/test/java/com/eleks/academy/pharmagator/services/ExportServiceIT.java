package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.entities.Medicine;
import com.eleks.academy.pharmagator.entities.Pharmacy;
import com.eleks.academy.pharmagator.entities.Price;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ExportServiceIT {

    private DatabaseDataSourceConnection dataSourceConnection;
    private ExportService mockService;

    @Autowired
    public void setComponents(final DataSource dataSource,
                              final ExportService service) throws SQLException {
        this.dataSourceConnection = new DatabaseDataSourceConnection(dataSource);
        this.mockService = service;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Medicine med01;
    private static Medicine med02;

    private static Pharmacy pharm01;
    private static Pharmacy pharm02;

    private static Price price01;
    private static Price price02;

    @BeforeAll
    static void setUp() {
        med01 = new Medicine(2021112901L, "ExportServiceIT_title1");
        med02 = new Medicine(2021112902L, "ExportServiceIT_title2");

        pharm01 = new Pharmacy(2021112903L, "ExportServiceIT_name1", "https://linktemplate.com/");
        pharm02 = new Pharmacy(2021112904L, "ExportServiceIT_name2", "https://linktemplate.com/");

        price01 = new Price(pharm01.getId(), med01.getId(), new BigDecimal("10.08"), "E1X2T3R4N5L6I7D801", Instant.now());
        price02 = new Price(pharm02.getId(), med02.getId(), new BigDecimal("10.09"), "E1X2T3R4N5L6I7D802", Instant.now());
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "prices", "pharmacies", "medicines");
    }


    @Test
    void getMapPricesFromDatabase_ok() throws Exception {
        try {
            DatabaseOperation.REFRESH.execute(this.dataSourceConnection, readDataset());
            Map<String, Map<Long, BigDecimal>> prices;

            prices = mockService.getMapPricesFromDatabase();

            assertEquals(prices.get(med01.getTitle()).get(pharm01.getId()), price01.getPrice());
            assertEquals(prices.get(med02.getTitle()).get(pharm02.getId()), price02.getPrice());
        } finally {
            this.dataSourceConnection.close();
        }
    }

    private IDataSet readDataset() throws DataSetException, IOException {
        try (var resource = getClass()
                .getResourceAsStream("ExportServiceIT_dataset.xml")) {
            return new FlatXmlDataSetBuilder()
                    .build(resource);
        }
    }

}
