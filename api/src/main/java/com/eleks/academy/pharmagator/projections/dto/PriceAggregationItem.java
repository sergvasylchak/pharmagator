package com.eleks.academy.pharmagator.projections.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PriceAggregationItem<T> {

    private T key;

    private BigDecimal value;

}
