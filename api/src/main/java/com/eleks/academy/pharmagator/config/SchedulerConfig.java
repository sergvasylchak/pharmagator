package com.eleks.academy.pharmagator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.ErrorHandler;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SchedulerConfig {

    private final ErrorHandler errorHandler;

    @Bean
    public TaskScheduler taskScheduler() {
        ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler();

        taskScheduler.setErrorHandler(errorHandler);

        return taskScheduler;
    }

}
