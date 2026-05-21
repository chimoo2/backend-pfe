package com.example.career.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Bridge between the React frontend and the Career Recommendation microservice.
 *
 * The frontend never calls the Python service directly. It hits this Spring
 * controller (JWT-protected, audited) which forwards the payload to the
 * microservice through the API Gateway (port 9000 -> 8002).
 */
@RestController
@RequestMapping("/api/career")
public class CareerRecommendationController {

    @Value("${career.service.url:http://localhost:9000/api/recommend}")
    private String careerServiceUrl;

    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, Object> payload) {
        String url = careerServiceUrl.replaceAll("/+$", "") + "/career/recommend";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Object> upstream = rest.exchange(url, HttpMethod.POST, request, Object.class);
            return ResponseEntity.status(upstream.getStatusCode()).body(upstream.getBody());
        } catch (RestClientException ex) {
            return ResponseEntity.status(502)
                    .body(Map.of("error", "Career recommendation service unavailable",
                                 "detail", ex.getMessage()));
        }
    }
}
