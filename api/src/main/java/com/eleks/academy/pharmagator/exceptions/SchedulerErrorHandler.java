package com.eleks.academy.pharmagator.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
public class SchedulerErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable t) {
        log.error(t.getMessage());
    }

}
