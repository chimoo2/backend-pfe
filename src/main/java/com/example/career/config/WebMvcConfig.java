package com.example.career.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cv.upload.dir:uploads/cvs/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath.replaceAll("\\\\", "/"))
                .setCachePeriod(3600); // Cache 1 heure
    }
}
