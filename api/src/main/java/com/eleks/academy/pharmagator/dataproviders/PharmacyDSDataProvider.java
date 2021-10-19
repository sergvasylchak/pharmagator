package com.eleks.academy.pharmagator.dataproviders;

import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.ds.CategoryDto;
import com.eleks.academy.pharmagator.dataproviders.dto.ds.DSMedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.ds.DSMedicinesResponse;
import com.eleks.academy.pharmagator.dataproviders.dto.ds.FilterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service("pharmacyDSDataProvider")
public class PharmacyDSDataProvider implements DataProvider {

    private final WebClient dsClient;

    @Value("${pharmagator.data-providers.apteka-ds.page-size}")
    private Long pageSize;

    @Value("${pharmagator.data-providers.apteka-ds.root-category}")
    private String rootCategorySlug;

    @Value("${pharmagator.data-providers.apteka-ds.category-fetch-url}")
    private String categoriesFetchUrl;

    @Value("${pharmagator.data-providers.apteka-ds.category-path}")
    private String categoryPath;

    @Override
    public Stream<MedicineDto> loadData() {
        return this.fetchCategories()
                .stream()
                .filter(categoryDto -> categoryDto.getSlug().equals(this.rootCategorySlug))
                .map(CategoryDto::getChildren)
                .flatMap(Collection::stream)
                .map(CategoryDto::getSlug)
                .flatMap(this::fetchMedicinesByCategory);
    }

    private List<CategoryDto> fetchCategories() {
        return this.dsClient.get().uri(this.categoriesFetchUrl)
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<CategoryDto>>() {
                }).block();
    }

    private Stream<MedicineDto> fetchMedicinesByCategory(String category) {
        Function<FilterRequest, DSMedicinesResponse> fetchPage = filter ->
                this.dsClient.post()
                        .uri(categoryPath + "/" + category)
                        .body(Mono.just(filter), FilterRequest.class)
                        .retrieve()
                        .bodyToMono(DSMedicinesResponse.class)
                        .filter(Objects::nonNull)
                        .map(DSMedicinesResponse.withPage(filter.getPage()))
                        .block();

        Predicate<DSMedicinesResponse> isNext = response ->
                (response.getPage()) * this.pageSize < response.getTotal();

        return Stream.iterate(FilterRequest.start(this.pageSize), FilterRequest::next)
                .map(fetchPage)
                .peek(res -> log.info("PAGE {}, TOTAL {}, isNext {}", res.getPage(), res.getTotal(), (res.getPage() - 1) * this.pageSize < res.getTotal()))
                .takeWhile(isNext)
                .map(DSMedicinesResponse::getProducts)
                .flatMap(Collection::stream)
                .map(this::mapToMedicineDto);
    }

    private MedicineDto mapToMedicineDto(DSMedicineDto dsMedicineDto) {
        return MedicineDto.builder()
                .externalId(dsMedicineDto.getId())
                .price(dsMedicineDto.getPrice())
                .title(dsMedicineDto.getName())
                .build();
    }

}
