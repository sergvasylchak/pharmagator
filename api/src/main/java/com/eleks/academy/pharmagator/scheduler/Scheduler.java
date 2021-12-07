package com.eleks.academy.pharmagator.scheduler;

import com.eleks.academy.pharmagator.dataproviders.DataProvider;
import com.eleks.academy.pharmagator.services.AdvancedSearchService;
import com.eleks.academy.pharmagator.services.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class Scheduler {

    private final List<DataProvider> dataProviderList;

    private final ImportService importService;

    private final AdvancedSearchService advancedSearchService;

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MINUTES)
    public void schedule() {
        log.info("Scheduler started at {}", Instant.now());
        dataProviderList.parallelStream()
                .flatMap(DataProvider::loadData)
                .forEach(importService::storeToDatabase);
        log.info("Scheduler finished at {}", Instant.now());

        advancedSearchService.refreshView();
    }

}
