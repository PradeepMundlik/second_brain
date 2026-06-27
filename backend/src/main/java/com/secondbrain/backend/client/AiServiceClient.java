package com.secondbrain.backend.client;

import com.secondbrain.backend.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
@Slf4j
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public AiServiceClient(
            RestTemplateBuilder builder,
            @Value("${ai.service.url}") String aiServiceUrl) {
        // Force the RestTemplate to use the JDK HttpURLConnection (HTTP/1.1)
        this.restTemplate = new org.springframework.web.client.RestTemplate(new SimpleClientHttpRequestFactory());
        this.baseUrl = aiServiceUrl;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    public EmbedResponse embed(EmbedRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String json = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            return restTemplate.postForObject(baseUrl + "/embed", entity, EmbedResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize embed request", e);
            throw new RuntimeException(e);
        }
    }

    public AiSearchResponse search(AiSearchRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String json = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            return restTemplate.postForObject(baseUrl + "/search", entity, AiSearchResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize search request", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteEmbeddings(Long noteId) {
        restTemplate.delete(baseUrl + "/notes/{noteId}/embeddings", noteId);
    }

    public AiSummarizeResponse summarize(AiSummarizeRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String json = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            return restTemplate.postForObject(baseUrl + "/summarize", entity, AiSummarizeResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize summarize request", e);
            throw new RuntimeException(e);
        }
    }
}
