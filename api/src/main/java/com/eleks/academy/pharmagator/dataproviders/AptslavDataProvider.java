package com.eleks.academy.pharmagator.dataproviders;

import com.eleks.academy.pharmagator.dataproviders.dto.MedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavMedicineDto;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavResponseBody;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.ResponseBodyIsNullException;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.converters.ApiDtoConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Qualifier("aptslavDataProvider")
public class AptslavDataProvider implements DataProvider {

    @Value("${pharmagator.data-providers.pharmacy-slavutych.medicines-uri}")
    private String medicinesFetchUri;

    @Value("${pharmagator.data-providers.pharmacy-slavutych.page-size}")
    private Integer pageSize;

    @Value("${pharmagator.data-providers.pharmacy-slavutych.api-calls-limit}")
    private Integer apiCallsLimit;

    @Qualifier("aptslavWebClient")
    private final WebClient aptslavWebClient;

    private final ApiDtoConverter<AptslavMedicineDto> apiDtoConverter;

    @Override
    public Stream<MedicineDto> loadData() {
        return fetchMedicines(apiCallsLimit);
    }

    /**
     * Here in this method we`re sending GET requests to the API until we`ve read all available data
     * While API`s 'take' parameter can`t be more than 100, we can`t fetch all available data in one
     * request
     * For now we`re taking only those products, which are in medicines category
     *
     * @return Stream<MedicineDto>
     */
    private Stream<MedicineDto> fetchMedicines(int callsLimit) {
        AptslavResponseBody<AptslavMedicineDto> initialResponse = sendGetMedicinesRequest(pageSize, 0);

        Stream<AptslavMedicineDto> restOfData = LongStream.rangeClosed(1, callsLimit)
                .boxed()
                .map(s -> sendGetMedicinesRequest(pageSize, (int) (s * pageSize)))
                .map(AptslavResponseBody::getData)
                .flatMap(Collection::stream);

        return Stream.concat(initialResponse.getData().stream(), restOfData)
                .map(apiDtoConverter::toMedicineDto);
    }

    /**
     * @param step - how many objects we can retrieve, represents API`s 'take' parameter.
     *             According to API, max value is 100, default value is 5
     * @param skip - how many objects we already have, represents API`s 'skip' parameter
     * @return AptslavResponseBody<AptslavMedicineDto>
     * @see AptslavResponseBody
     */
    private AptslavResponseBody<AptslavMedicineDto> sendGetMedicinesRequest(int step, int skip) {
        return aptslavWebClient.get().uri(uriBuilder -> uriBuilder.path(medicinesFetchUri)
                        .queryParam("fields", "id,externalId,name,created,manufacturer")
                        .queryParam("take", step)
                        .queryParam("skip", skip)
                        .queryParam("inStock", true)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<AptslavResponseBody<AptslavMedicineDto>>() {
                })
                .blockOptional()
                .orElseThrow(ResponseBodyIsNullException::new);
    }

}
