package com.eleks.academy.pharmagator.controllers.ui;

import com.eleks.academy.pharmagator.entities.Medicine;
import com.eleks.academy.pharmagator.entities.Pharmacy;
import com.eleks.academy.pharmagator.services.ExportService;
import com.eleks.academy.pharmagator.services.MedicineService;
import com.eleks.academy.pharmagator.services.PharmacyService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(NavigationController.class)
class NavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URI = "/ui";

    @MockBean
    private PharmacyService pharmacyService;

    @MockBean
    private MedicineService medicineService;

    @MockBean
    private ExportService exportService;

    private static List<Medicine> meds;
    private static List<Pharmacy> pharmacies;
    private static Map<String, Map<Long, BigDecimal>> prices;

    @BeforeAll
    public static void setUp() {
        meds = List.of(
                new Medicine(201102L, "medicine1"),
                new Medicine(202202L, "medicine2")
        );

        pharmacies = List.of(
                new Pharmacy(101L, "tolya's hub", "https://linktemplate.com/"),
                new Pharmacy(202L, "medcad", "https://linktemplate.com/")
        );

        prices = new HashMap<>();
        Map<Long, BigDecimal> priceByPharmacies = new HashMap<>();
        priceByPharmacies.put(103L, new BigDecimal("12.23"));
        priceByPharmacies.put(104L, new BigDecimal("24.46"));
        prices.put("medicine3", priceByPharmacies);
    }

    @Test
    void getHomePage_ok() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andDo(print());
    }

    @Test
    void getMedicines_ok() throws Exception {
        doReturn(meds).when(medicineService).findAll();

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/medicine").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("medicine"))
                .andExpect(model().attribute("meds", meds))
                .andDo(print());
    }

    @Test
    void getPrices_ok() throws Exception {
        doReturn(pharmacies).when(pharmacyService).findAll();
        doReturn(prices).when(exportService).getMapPricesFromDatabase();

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/price"))
                .andExpect(status().isOk())
                .andExpect(view().name("price"))
                .andExpect(model().attribute("pricesMap", prices))
                .andExpect(model().attribute("pharmacies", pharmacies))
                .andDo(print());
    }

}
