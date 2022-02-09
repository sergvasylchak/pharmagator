package com.eleks.academy.pharmagator.controllers;

import com.eleks.academy.pharmagator.controllers.dto.PriceAggregationRequest;
import com.eleks.academy.pharmagator.dataproviders.dto.input.PriceDto;
import com.eleks.academy.pharmagator.exceptions.InvalidIdentifierException;
import com.eleks.academy.pharmagator.projections.dto.PriceAggregationItem;
import com.eleks.academy.pharmagator.services.PriceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;
    private final ModelMapper modelMapper;

    @GetMapping
    public List<PriceDto> getAll() {
        return priceService.findAll().stream()
                .map(price -> modelMapper.map(price, PriceDto.class))
                .toList();
    }

    @GetMapping("/pharmacyId/{pharmacyId:[\\d]+}/medicineId/{medicineId:[\\d]+}")
    public ResponseEntity<PriceDto> getById(
            @PathVariable Long pharmacyId,
            @PathVariable Long medicineId) {

        return priceService.findById(pharmacyId, medicineId)
                .map(price -> ResponseEntity.ok(modelMapper.map(price, PriceDto.class)))
                .orElseThrow(() -> new InvalidIdentifierException(pharmacyId, medicineId));
    }

    @GetMapping("/aggregations")
    public List<PriceAggregationItem<String>> findAggregated(@Valid PriceAggregationRequest request) {
        return this.priceService.findAllAggregated(request);
    }

    @PutMapping("/pharmacyId/{pharmacyId:[\\d]+}/medicineId/{medicineId:[\\d]+}")
    public ResponseEntity<PriceDto> update(
            @Valid @RequestBody PriceDto priceDto,
            @PathVariable Long pharmacyId,
            @PathVariable Long medicineId) {

        return priceService.update(pharmacyId, medicineId, priceDto)
                .map(price -> ResponseEntity.ok(modelMapper.map(price, PriceDto.class)))
                .orElseThrow(() -> new InvalidIdentifierException(pharmacyId, medicineId));
    }

    @DeleteMapping("/pharmacyId/{pharmacyId:[\\d]+}/medicineId/{medicineId:[\\d]+}")
    public ResponseEntity<PriceDto> delete(
            @PathVariable Long pharmacyId,
            @PathVariable Long medicineId) {

        priceService.deleteById(pharmacyId, medicineId);
        return ResponseEntity.noContent().build();
    }

}
