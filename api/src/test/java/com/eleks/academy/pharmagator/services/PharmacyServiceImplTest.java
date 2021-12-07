package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.dataproviders.dto.input.PharmacyDto;
import com.eleks.academy.pharmagator.entities.Pharmacy;
import com.eleks.academy.pharmagator.repositories.PharmacyRepository;
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
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PharmacyServiceImplTest {

    @Mock
    private PharmacyRepository repository;

    @InjectMocks
    private PharmacyServiceImpl pharmacyService;

    @Mock
    private static ModelMapper modelMapper;

    private static Pharmacy pharmacy1;
    private static Pharmacy pharmacy2;
    private static List<Pharmacy> pharmacyList;
    private static PharmacyDto pharmacyDto;

    @BeforeAll
    public static void setup() {
        pharmacy1 = new Pharmacy(8L, "My pharma", "linkk.com");
        pharmacy2 = new Pharmacy(9L, "Second pharma", "secongPharma.com");
        pharmacyDto = new PharmacyDto("My pharma", "linkk.com");
        pharmacyList = Arrays.asList(pharmacy1, pharmacy2);
    }

    @Test
    void getAllPharmacies_isOk() {
        when(repository.findAll()).thenReturn(pharmacyList);

        List<Pharmacy> pharmacyList = pharmacyService.findAll();

        assertEquals(2, pharmacyList.size());
    }

    @Test
    void savePharmacy_pharmacyIsCreated() {
        when(repository.save(any())).thenReturn(pharmacy1);
        when(modelMapper.map(eq(pharmacyDto), any())).thenReturn(pharmacy1);

        assertEquals(pharmacy1, pharmacyService.save(pharmacyDto));

        verify(repository, times(1)).save(any());
    }

    @Test
    void findPharmacyById_shouldReturnRespectivePharmacy() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(pharmacy1));

        Pharmacy pharmacy = pharmacyService.findById(pharmacy1.getId()).get();

        verify(repository, times(1)).findById(anyLong());

        assertEquals(pharmacy1.getName(), pharmacy.getName());
        assertEquals(pharmacy1.getMedicineLinkTemplate(), pharmacy.getMedicineLinkTemplate());
    }

    @Test
    void updatePharmacyById_pharmacyIsUpdated() {
        when(modelMapper.map(eq(pharmacyDto), any())).thenReturn(pharmacy1);
        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(pharmacy1));
        when(repository.save(any(Pharmacy.class))).thenReturn(pharmacy1);

        assertEquals(pharmacy1, pharmacyService.update(anyLong(), pharmacyDto).get());
    }

    @Test
    void deleteById_isOk() {
        doNothing().when(repository).deleteById(anyLong());

        pharmacyService.deleteById(pharmacy1.getId());

        verify(repository, times(1)).deleteById(pharmacy1.getId());
    }

    @Test
    void deleteById_nonExistingId_throwsEmptyResultDataAccessException() {
        Long wrongPharmacyId = 1000002L;
        String exceptionMessage = String.format("No class %s entity with id %d exists!", Pharmacy.class.getCanonicalName(), wrongPharmacyId);

        LogCaptor logCaptor = LogCaptor.forClass(PharmacyServiceImpl.class);
        logCaptor.setLogLevelToInfo();

        doThrow(new EmptyResultDataAccessException(exceptionMessage, 0)).when(repository).deleteById(wrongPharmacyId);

        pharmacyService.deleteById(wrongPharmacyId);

        assertThat(logCaptor.getInfoLogs()).containsExactly(exceptionMessage);

        Exception exception = assertThrows(EmptyResultDataAccessException.class, () -> {
            repository.deleteById(wrongPharmacyId);
        });

        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(exceptionMessage));
    }

}
