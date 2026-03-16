package com.example.career.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;

@Service
public class AiExtractionService {

    private final WebClient webClient;

    // Constructeur : récupère l'URL depuis application.properties
    public AiExtractionService(@Value("${ai.service.url}") String url) {

        this.webClient = WebClient.builder()
                .baseUrl(url)
                .build();
    }

    /**
     * Envoie le fichier CV au microservice Python
     * et récupère la réponse JSON sous forme de String
     */
    public String extractSkills(File file) {

        try {

            return webClient.post()
                    .uri("/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(
                            "file", new FileSystemResource(file)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30)) // Timeout sécurité
                    .block(); // On attend la réponse

        } catch (Exception e) {

            System.err.println("Erreur appel microservice IA : " + e.getMessage());

            return null; // Ou tu peux lancer une exception
        }
    }
}