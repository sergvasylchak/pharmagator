package com.eleks.academy.pharmagator.controllers.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AggregationOperation {
    MIN("MIN(p.price)"), MAX("MAX(p.price)"), AVG("AVG(p.price)");

    private final String sqlOperation;

}
