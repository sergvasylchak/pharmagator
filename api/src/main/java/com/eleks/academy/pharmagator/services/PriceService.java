package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.controllers.dto.PriceAggregationRequest;
import com.eleks.academy.pharmagator.dataproviders.dto.input.PriceDto;
import com.eleks.academy.pharmagator.entities.Price;
import com.eleks.academy.pharmagator.projections.dto.PriceAggregationItem;

import java.util.List;
import java.util.Optional;

public interface PriceService {

    List<Price> findAll();

    Optional<Price> findById(Long pharmacyId, Long medicineId);

    Optional<Price> update(Long pharmacyId, Long medicineId, PriceDto priceDto);

    void deleteById(Long pharmacyId, Long medicineId);

    List<PriceAggregationItem<String>> findAllAggregated(PriceAggregationRequest aggregationRequest);

}
