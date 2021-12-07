package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.dataproviders.dto.input.MedicineDto;
import com.eleks.academy.pharmagator.entities.Medicine;
import com.eleks.academy.pharmagator.repositories.MedicineRepository;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MedicineServiceImplTest {

    @Mock
    private MedicineRepository repository;

    @InjectMocks
    private MedicineServiceImpl medicineService;

    @Mock
    private static ModelMapper modelMapper;

    private static Medicine medicine1;
    private static Medicine medicine2;
    private static List<Medicine> medicines;
    private static MedicineDto medicineDto;

    @BeforeAll
    public static void setup() {
        medicine1 = new Medicine(1L, "Ibuprofen");
        medicine2 = new Medicine(3L, "Vitamin C");
        medicines = Arrays.asList(medicine1, medicine2);
        medicineDto = new MedicineDto("Ibuprofen");
    }

    @Test
    void getAllMedicines_ok() {
        when(repository.findAll()).thenReturn(medicines);

        List<Medicine> medicineList = medicineService.findAll();

        assertEquals(2, medicineList.size());
    }

    @Test
    void saveMedicine_medicineIsCreated() {
        when(repository.save(any())).thenReturn(medicine1);
        when(modelMapper.map(eq(medicineDto), any())).thenReturn(medicine1);

        assertEquals(medicine1, medicineService.save(medicineDto));

        verify(repository, times(1)).save(any(Medicine.class));
    }

    @Test
    void findMedicineById_shouldReturnRespectiveMedicine() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(medicine1));

        Medicine medicine2 = medicineService.findById(medicine1.getId()).get();

        verify(repository, times(1)).findById(anyLong());

        assertEquals(medicine1.getId(), medicine2.getId());
        assertEquals(medicine1.getTitle(), medicine2.getTitle());
    }

    @Test
    void updateMedicineById_medicineIsUpdated() {
        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(medicine1));
        when(repository.save(any(Medicine.class))).thenReturn(medicine1);
        when(modelMapper.map(eq(medicineDto), any())).thenReturn(medicine1);

        assertEquals(medicine1, medicineService.update(anyLong(), medicineDto).get());
    }

    @Test
    void deleteById_isOk() {
        doNothing().when(repository).deleteById(anyLong());

        medicineService.deleteById(medicine1.getId());

        verify(repository, times(1)).deleteById(medicine1.getId());
    }

    @Test
    void deleteById_nonExistingId_throwsEmptyResultDataAccessException() {
        Long wrongMedicineId = 1000002L;
        String exceptionMessage = String.format("No class %s entity with id %d exists!", Medicine.class.getCanonicalName(), wrongMedicineId);

        LogCaptor logCaptor = LogCaptor.forClass(MedicineServiceImpl.class);
        logCaptor.setLogLevelToInfo();

        doThrow(new EmptyResultDataAccessException(exceptionMessage, 0)).when(repository).deleteById(wrongMedicineId);

        medicineService.deleteById(wrongMedicineId);

        assertThat(logCaptor.getInfoLogs()).containsExactly(exceptionMessage);

        Exception exception = assertThrows(EmptyResultDataAccessException.class, () -> {
            repository.deleteById(wrongMedicineId);
        });

        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(exceptionMessage));
    }

}
