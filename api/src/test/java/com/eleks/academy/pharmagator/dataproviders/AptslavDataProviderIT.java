package com.eleks.academy.pharmagator.dataproviders;

import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavPriceDto;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavResponseBody;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.AptslavResponseEntity;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.ResponseBodyIsNullException;
import com.eleks.academy.pharmagator.dataproviders.dto.aptslav.converters.ApiMedicineDtoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AptslavDataProviderIT {

    private static MockWebServer mockWebServer;

    private AptslavDataProvider subject;

    private final String BASE_URL = "/aptslav.com.ua:3000/api/v1/categories/87/products";

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();

        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @Autowired
    public void setSubject(ApiMedicineDtoConverter apiMedicineDtoConverter) {
        HttpUrl url = mockWebServer.url(BASE_URL);

        WebClient webClient = WebClient.create(url.toString());

        subject = new AptslavDataProvider(webClient, apiMedicineDtoConverter);

        ReflectionTestUtils.setField(subject, "pageSize", 100);

        ReflectionTestUtils.setField(subject, "apiCallsLimit", 3);
    }

    @SuppressWarnings("ConstantConditions")
    private String getQueryParameter(RecordedRequest request, String paramName) {
        return request.getRequestUrl().queryParameter(paramName);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void setMedicineRequest_ok() throws InterruptedException, JsonProcessingException {
        AptslavResponseEntity responseEntity = AptslavResponseEntity.builder()
                .externalId("123")
                .id(125L)
                .name("Medicine_1")
                .created("2021-05-07T18:38:00.722Z")
                .price(new AptslavPriceDto(BigDecimal.valueOf(10), BigDecimal.valueOf(20)))
                .manufacturer("Manufacturer")
                .build();

        AptslavResponseEntity anotherResponseEntity = AptslavResponseEntity.builder()
                .externalId("65513")
                .id(532L)
                .name("Medicine_2")
                .created("2021-05-07T18:38:00.722Z")
                .price(new AptslavPriceDto(BigDecimal.valueOf(40), BigDecimal.valueOf(60)))
                .manufacturer("Manufacturer_2")
                .build();

        AptslavResponseBody<Object> responseBody = AptslavResponseBody.builder()
                .data(List.of(responseEntity, anotherResponseEntity))
                .count(2L)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(responseBody)));

        ReflectionTestUtils.invokeMethod(subject, "sendGetMedicinesRequest", 100, 10);

        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("GET", request.getMethod());

        assertTrue(request.getPath().startsWith(BASE_URL));

        assertEquals("id,externalId,name,created,manufacturer", getQueryParameter(request, "fields"));

        assertEquals("100", getQueryParameter(request, "take"));

        assertEquals("10", getQueryParameter(request, "skip"));

        assertEquals("true", getQueryParameter(request, "inStock"));
    }

    @Test
    void sendGetMedicinesRequest_bodyIsNull_ResponseBodyIsNullException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String exceptionMessage = assertThrows(ResponseBodyIsNullException.class, () -> ReflectionTestUtils.invokeMethod(subject, "sendGetMedicinesRequest", 100, 10)).getMessage();

        assertEquals("Response body is null", exceptionMessage);
    }

    @Test
    void fetchMedicines_ok() throws JsonProcessingException, InterruptedException {
        AptslavResponseEntity responseEntity = AptslavResponseEntity.builder()
                .externalId("123")
                .id(125L)
                .name("Medicine_1")
                .created("2021-05-07T18:38:00.722Z")
                .price(new AptslavPriceDto(BigDecimal.valueOf(10), BigDecimal.valueOf(20)))
                .manufacturer("Manufacturer")
                .build();

        AptslavResponseEntity anotherResponseEntity = AptslavResponseEntity.builder()
                .externalId("65513")
                .id(532L)
                .name("Medicine_2")
                .created("2021-05-07T18:38:00.722Z")
                .price(new AptslavPriceDto(BigDecimal.valueOf(40), BigDecimal.valueOf(60)))
                .manufacturer("Manufacturer_2")
                .build();

        AptslavResponseBody<Object> responseBody = AptslavResponseBody.builder()
                .data(List.of(responseEntity, anotherResponseEntity))
                .count(2L)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(responseBody)));

        subject.loadData();

        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("GET", request.getMethod());

        assertTrue(request.getPath().startsWith(BASE_URL));

        assertEquals("id,externalId,name,created,manufacturer", getQueryParameter(request, "fields"));

        assertEquals("100", getQueryParameter(request, "take"));

        assertEquals("0", getQueryParameter(request, "skip"));

        assertEquals("true", getQueryParameter(request, "inStock"));
    }

    @Test
    void badRequestToApi_WebClientResponseException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        WebClientResponseException exception = assertThrows(WebClientResponseException.class, () -> subject.loadData());

        String message = exception.getMessage();

        assertTrue(message.startsWith("500 Internal Server Error from GET"));
    }

}
