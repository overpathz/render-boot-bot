package com.example.rendertestbot.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private static final String API_URL = "https://api.tinyurl.com/create";

    @Value("${tiny.url.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public String shortenUrl(String longUrl) {
        UrlShortenRequest requestPayload = new UrlShortenRequest(
                longUrl,
                "tinyurl.com",
                "undefined"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<UrlShortenRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

        ResponseEntity<UrlShortenResponse> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                requestEntity,
                UrlShortenResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().data.tinyUrl;
        } else {
            throw new RuntimeException("Failed to shorten URL. Response: " + response.getStatusCode());
        }
    }

    public record UrlShortenRequest(
            @JsonProperty("url") String url,
            @JsonProperty("domain") String domain,
            @JsonProperty("description") String description
    ) {}

    public record UrlShortenResponse(
            @JsonProperty("data") Data data
    ) {
        public record Data(
                @JsonProperty("tiny_url") String tinyUrl
        ) {}
    }

}
