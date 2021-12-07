package com.eleks.academy.pharmagator.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class DataProvidersConfig {

    @Value("${pharmagator.data-providers.pharmacy-ds.url}")
    private String pharmacyDSBaseUrl;

    @Value("${pharmagator.data-providers.pharmacy-anc.url}")
    private String pharmacyANCBaseUrl;

    @Value("${pharmagator.data-providers.pharmacy-liki24.url}")
    private String pharmacyLiki24BaseUrl;

    @Value("${pharmagator.data-providers.pharmacy-rozetka.url}")
    private String pharmacyRozetkaBaseUrl;

    @Value("${pharmagator.data-providers.pharmacy-slavutych.base-url}")
    private String aptslavBaseUrl;

    @Bean(name = "pharmacyDSWebClient")
    public WebClient pharmacyDSWebClient() {
        return getWebClientWithDefaultHeadersSetup(pharmacyDSBaseUrl);
    }

    @Bean(name = "pharmacyANCWebClient")
    public WebClient pharmacyANCWebClient() {
        return getWebClientWithDefaultHeadersSetup(pharmacyANCBaseUrl);
    }

    @Bean(name = "pharmacyLiki24WebClient")
    public WebClient pharmacyLiki24WebClient() {
        return getWebClientWithDefaultHeadersSetup(pharmacyLiki24BaseUrl);
    }

    @Bean(name = "pharmacyRozetkaWebClient")
    public WebClient pharmacyRozetkaWebClient() {
        return getWebClientWithDefaultHeadersSetup(pharmacyRozetkaBaseUrl);
    }

    @SneakyThrows
    @Bean(name = "aptslavWebClient")
    public WebClient aptslavWebClient() {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient
                .create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(connector)
                .baseUrl(aptslavBaseUrl)
                .build();
    }

    private WebClient getWebClientWithDefaultHeadersSetup(String baseUrl) {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-lang", "uk")
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public ProjectionFactory projectionFactory() {
        return new SpelAwareProxyProjectionFactory();
    }

}
