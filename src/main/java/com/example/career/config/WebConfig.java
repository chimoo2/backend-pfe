package com.example.career.config;

//Cette classe est une configuration de l’application
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
//Spring doit charger cette classe au démarrage
//Cette classe contient des paramètres de configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
        //Ces routes seront accessibles depuis le frontend.
                .allowedOrigins("http://localhost:5173")
                //Ces sites peuvent appeler ton backend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
