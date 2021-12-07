package com.eleks.academy.pharmagator.dataproviders;


import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.rozetka.RozetkaMedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.rozetka.RozetkaMedicineResponse;
import com.eleks.academy.pharmagator.dataproviders.dto.rozetka.RozetkaProductIdsResponse;
import com.eleks.academy.pharmagator.dataproviders.dto.rozetka.RozetkaProductIdsResponseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Qualifier("pharmacyRozetkaDataProvider")
public class PharmacyRozetkaDataProvider implements DataProvider {

    @Qualifier("pharmacyRozetkaWebClient")
    private final WebClient rozetkaClient;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.product-ids-fetch-url}")
    private String productIdsFetchUrl;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.category-id}")
    private String categoryId;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.medicament-category-id}")
    private String medicamentCategoryId;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.sell-status}")
    private String sellStatus;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.products-fetch-url}")
    private String productsPath;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.pharmacy-name}")
    private String pharmacyName;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.page-limit}")
    private Long pageLimit;

    @Override
    public Stream<MedicineDto> loadData() {
        return Stream.iterate(1, page -> page + 1)
                .limit(pageLimit)
                .map(this::fetchProductIds)
                .flatMap(Optional::stream)
                .takeWhile(response -> response.getShowNext() != 0)
                .map(RozetkaProductIdsResponseData::getIds)
                .flatMap(this::fetchProducts);
    }

    private Optional<RozetkaProductIdsResponseData> fetchProductIds(int page) {
        RozetkaProductIdsResponse productIds = this.rozetkaClient.get().uri(u -> u
                        .path(productIdsFetchUrl)
                        .queryParam("category_id", categoryId)
                        .queryParam("sell_status", sellStatus)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RozetkaProductIdsResponse>() {
                })
                .block();
        return Optional.ofNullable(productIds)
                .map(RozetkaProductIdsResponse::getData);
    }

    private Stream<MedicineDto> fetchProducts(List<Long> productIdsList) {
        if(productIdsList.isEmpty()) {
            return Stream.empty();
        }
        String productIds = productIdsList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        RozetkaMedicineResponse rozetkaMedicineResponse = this.rozetkaClient.get().uri(u -> u
                        .path(productsPath)
                        .queryParam("product_ids", productIds)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RozetkaMedicineResponse>() {
                })
                .block();
        return Optional.ofNullable(rozetkaMedicineResponse).map(RozetkaMedicineResponse::getData).stream()
                .flatMap(Collection::stream)
                .filter(rozetkaMedicineDto -> Objects.nonNull(rozetkaMedicineDto.getId())
                        && Objects.nonNull(rozetkaMedicineDto.getMpath())
                        && rozetkaMedicineDto.getMpath().contains(medicamentCategoryId))
                .map(this::mapToMedicineDto);

    }

    private MedicineDto mapToMedicineDto(RozetkaMedicineDto rozetkaMedicineDto) {
        return MedicineDto.builder()
                .externalId(rozetkaMedicineDto.getId().toString())
                .price(rozetkaMedicineDto.getPrice())
                .title(rozetkaMedicineDto.getTitle())
                .pharmacyName(pharmacyName)
                .build();
    }

}
