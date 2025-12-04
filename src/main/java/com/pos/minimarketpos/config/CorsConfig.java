package com.pos.minimarketpos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Terapkan untuk semua endpoint URL
                        .allowedOrigins("http://localhost:5173") // Izinkan hanya dari Frontend Vue
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Method yang diizinkan
                        .allowedHeaders("*") // Izinkan semua header (termasuk Authorization)
                        .allowCredentials(true) // Izinkan kredensial (Cookies/Auth Header)
                        .maxAge(3600); // Cache setting CORS selama 1 jam
            }
        };
    }
}