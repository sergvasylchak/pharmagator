package com.eleks.academy.pharmagator.dataproviders;

import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.liki24.Liki24MedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.liki24.Liki24MedicinesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("pharmacyLiki24DataProvider")
public class PharmacyLiki24DataProvider implements DataProvider {

    @Qualifier("pharmacyLiki24WebClient")
    private final WebClient webClient;

    @Value("${pharmagator.data-providers.pharmacy-liki24.initial-page-index}")
    private Long initialPageIndex;

    @Value("${pharmagator.data-providers.pharmacy-liki24.pharmacy-name}")
    private String pharmacyName;

    @Value("${pharmagator.data-providers.pharmacy-liki24.page-limit}")
    private Long pageLimit;

    @Override
    public Stream<MedicineDto> loadData() {

        BiConsumer<Long, List<Liki24MedicinesResponse>> fillListByMedicineResponse = (page, medicinesResponseList1) -> {
            Liki24MedicinesResponse medicinesResponse = getLiki24MedicinesResponse(page);
            medicinesResponseList1.add(medicinesResponse);
        };

        Liki24MedicinesResponse liki24MedicinesResponse = getLiki24MedicinesResponse(initialPageIndex);

        if (liki24MedicinesResponse != null) {
            log.info("Start fetching: " + LocalDateTime.now());

            Long totalPages = liki24MedicinesResponse.getTotalPages();
            List<Liki24MedicinesResponse> medicinesResponseList = new ArrayList<>();
            medicinesResponseList.add(liki24MedicinesResponse);

            long startFetchPage = initialPageIndex + 1;

            totalPages = totalPages > pageLimit ? pageLimit : totalPages;

            LongStream.rangeClosed(startFetchPage, totalPages)
                    .parallel()
                    .forEach(pageNumber -> fillListByMedicineResponse.accept(pageNumber, medicinesResponseList));

            log.info("End Fetching: " + LocalDateTime.now());
            return medicinesResponseList.stream()
                    .filter(Objects::nonNull)
                    .map(Liki24MedicinesResponse::getItems)
                    .flatMap(Collection::stream)
                    .map(this::mapToDataProviderMedicineDto);
        }
        return Stream.of();
    }

    private Liki24MedicinesResponse getLiki24MedicinesResponse(Long page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("page", page)
                        .build())
                .retrieve().bodyToMono(Liki24MedicinesResponse.class)
                .block();
    }

    private MedicineDto mapToDataProviderMedicineDto(Liki24MedicineDto liki24MedicineDto) {
        BigDecimal price = liki24MedicineDto.getPrice() == null ? BigDecimal.ZERO : liki24MedicineDto.getPrice();
        return MedicineDto.builder()
                .externalId(liki24MedicineDto.getProductId())
                .title(liki24MedicineDto.getName())
                .price(price)
                .pharmacyName(pharmacyName)
                .build();
    }

}
