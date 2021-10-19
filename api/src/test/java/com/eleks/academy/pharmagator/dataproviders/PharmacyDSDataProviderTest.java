package com.eleks.academy.pharmagator.dataproviders;

import com.eleks.academy.pharmagator.config.DataProvidersConfig;
import com.eleks.academy.pharmagator.dataproviders.dto.ds.CategoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(classes = {PharmacyDSDataProvider.class, DataProvidersConfig.class})
public class PharmacyDSDataProviderTest {

    @Value("${pharmagator.data-providers.apteka-ds.root-category}")
    private String rootCategory;

    @Value("${pharmagator.data-providers.apteka-ds.page-size}")
    private Long pageSize;

    @SpyBean
    private WebClient webClient;

    @Autowired
    private PharmacyDSDataProvider pharmacyDSDataProvider;

    @Test
    public void loadData_noMedicinesFound() {
        ReflectionTestUtils.setField(this.pharmacyDSDataProvider, "rootCategorySlug", "something-unimportant");

        final var data = this.pharmacyDSDataProvider.loadData().collect(Collectors.toList());

        verify(this.webClient, never()).post();
        assertThat(data).matches(List::isEmpty);

        ReflectionTestUtils.setField(this.pharmacyDSDataProvider, "rootCategorySlug", this.rootCategory);
    }

    @Test
    public void loadData_paging() {
        this.mockCategories(
                List.of(
                        new CategoryDto("Медикаменти", this.rootCategory,
                                List.of(
                                        new CategoryDto("Препарати від печії",
                                                "preparaty-vid-pechiyi", List.of())))));

        final var data = this.pharmacyDSDataProvider.loadData().collect(Collectors.toList());

        final var expectedPages = data.size() % this.pageSize == 0
                ? data.size() / this.pageSize : (data.size() / this.pageSize) + 1;

        verify(this.webClient, times((int) expectedPages)).post();
    }

    @SuppressWarnings("unchecked")
    public void mockCategories(List<CategoryDto> categories) {
        final var requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        final var requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        final var responseSpec = mock(WebClient.ResponseSpec.class);

        given(this.webClient.get()).willReturn(requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(anyString())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).willReturn(Mono.just(categories));
    }
}
