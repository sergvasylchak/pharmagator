package com.eleks.academy.pharmagator.utils;

import com.eleks.academy.pharmagator.controllers.dto.PriceAggregationRequest;
import lombok.RequiredArgsConstructor;

import java.util.function.BinaryOperator;
import java.util.function.Function;

@RequiredArgsConstructor
public enum PriceAggregationQueryBuilder {
    AGGREGATION(r -> r.getAgg().getSqlOperation(), (query, propery) -> query.replaceAll("\\$agg", propery));


    private final Function<PriceAggregationRequest, String> propertyExtractor;
    private final BinaryOperator<String> queryBuilder;
}


