package com.eleks.academy.pharmagator.dataproviders.dto.ds;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.UnaryOperator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DSMedicinesResponse {

    private Long total;

    private Long pages;

    private Long page;

    private List<DSMedicineDto> products;

    public static UnaryOperator<DSMedicinesResponse> withPage(Long page) {
        return response -> {
            response.setPage(page);

            return response;
        };
    }
}
