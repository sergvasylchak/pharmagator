package com.eleks.academy.pharmagator.dataproviders.dto.aptslav.converters;

import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavMedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavPriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
public class ApiMedicineDtoConverter implements ApiDtoConverter<AptslavMedicineDto> {

    @Value("${pharmagator.data-providers.pharmacy-slavutych.pharmacy-name}")
    private String pharmacyTitle;

    @Override
    public MedicineDto toMedicineDto(@NotNull AptslavMedicineDto apiDto) {
        checkArgument(apiDto);

        String title = apiDto.getName();

        AptslavPriceDto aptslavPriceDto = apiDto.getPrice();

        long externalId = apiDto.getId();

        return MedicineDto.builder()
                .externalId(String.valueOf(externalId))
                .title(title)
                .price(aptslavPriceDto.getMin())
                .pharmacyName(pharmacyTitle)
                .build();
    }

    private void checkArgument(AptslavMedicineDto arg) {
        String title = arg.getName();

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("AptslavMedicineDto has null or blank 'name' ");
        }

        AptslavPriceDto price = arg.getPrice();

        if (price == null) {
            throw new IllegalArgumentException("AptslavMedicineDto has null 'price'");
        }

        if (price.getMin().intValue() < 0 || price.getMax().intValue() < 0) {
            throw new IllegalArgumentException("Min and max price values should be greater than 0");
        }

        long externalId = arg.getExternalId();

        if (externalId <= 0) {
            throw new IllegalArgumentException("Invalid 'externalId' value");
        }
    }

}

