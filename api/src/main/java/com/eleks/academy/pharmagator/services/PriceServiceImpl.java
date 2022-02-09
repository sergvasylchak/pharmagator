package com.eleks.academy.pharmagator.services;

import com.eleks.academy.pharmagator.controllers.dto.PriceAggregationRequest;
import com.eleks.academy.pharmagator.dataproviders.dto.input.PriceDto;
import com.eleks.academy.pharmagator.entities.Price;
import com.eleks.academy.pharmagator.entities.PriceId;
import com.eleks.academy.pharmagator.projections.dto.PriceAggregationItem;
import com.eleks.academy.pharmagator.repositories.PriceJdbcRepository;
import com.eleks.academy.pharmagator.repositories.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;
    private final PriceJdbcRepository priceJdbcRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<Price> findAll() {
        return priceRepository.findAll();
    }

    @Override
    public Optional<Price> findById(Long pharmacyId, Long medicineId) {
        PriceId priceId = new PriceId(pharmacyId, medicineId);
        return this.priceRepository.findById(priceId);
    }

    @Override
    public Optional<Price> update(Long pharmacyId, Long medicineId, PriceDto priceDto) {

        PriceId priceId = new PriceId(pharmacyId, medicineId);

        return this.priceRepository.findById(priceId)
                .map(source -> {
                    Price price = modelMapper.map(priceDto, Price.class);
                    price.setPharmacyId(pharmacyId);
                    price.setMedicineId(medicineId);
                    return priceRepository.save(price);
                });
    }

    @Override
    public void deleteById(Long pharmacyId, Long medicineId) {
        PriceId priceId = new PriceId(pharmacyId, medicineId);
        try {
            priceRepository.deleteById(priceId);
        } catch (EmptyResultDataAccessException exception) {
            log.info(exception.getMessage(), exception);
        }
    }

    @Override
    public List<PriceAggregationItem<String>> findAllAggregated(PriceAggregationRequest request) {
        return this.priceJdbcRepository.findAllAggregated(request);
    }

}
