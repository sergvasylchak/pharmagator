package com.eleks.academy.pharmagator.controllers.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAggregationRequest {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    private Set<String> pharmacyNot;

    private String medicine;

    @NotNull
    private AggregationOperation agg;

    private SortOrder order = SortOrder.ASC;

    @JsonSetter
    public void setAgg(String raw) {
        this.agg = Arrays.stream(AggregationOperation.values())
                .filter(operation -> operation.name().equalsIgnoreCase(raw))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    @JsonSetter
    public void setOrder(String raw) {
        this.order = Arrays.stream(SortOrder.values())
                .filter(order -> order.name().equalsIgnoreCase(raw))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

}
