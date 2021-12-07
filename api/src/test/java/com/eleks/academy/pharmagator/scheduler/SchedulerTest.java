package com.eleks.academy.pharmagator.scheduler;

import com.eleks.academy.pharmagator.dataproviders.DataProvider;
import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.services.AdvancedSearchService;
import com.eleks.academy.pharmagator.services.AdvancedSearchServiceImpl;
import com.eleks.academy.pharmagator.services.ImportService;
import com.eleks.academy.pharmagator.services.ImportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class SchedulerTest {

    private Scheduler subject;

    private DataProvider dataProviderMock;

    private ImportService importServiceMock;

    private AdvancedSearchService advancedSearchServiceMock;

    @BeforeEach
    public void setUp(){
        importServiceMock = mock(ImportServiceImpl.class);

        advancedSearchServiceMock = mock(AdvancedSearchServiceImpl.class);

        dataProviderMock = mock(DataProvider.class);

        subject = new Scheduler(List.of(dataProviderMock), importServiceMock, advancedSearchServiceMock);

        MedicineDto medicineDto = MedicineDto.builder().build();

        when(dataProviderMock.loadData()).thenReturn(Stream.of(medicineDto));

    }

    @Test
    void schedule_ok(){
        subject.schedule();

        InOrder order = inOrder(dataProviderMock, importServiceMock, advancedSearchServiceMock);

        order.verify(dataProviderMock, times(1)).loadData();

        order.verify(importServiceMock, times(1)).storeToDatabase(any(MedicineDto.class));

        order.verify(advancedSearchServiceMock, times(1)).refreshView();
    }

}
