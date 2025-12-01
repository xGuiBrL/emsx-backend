package com.app.emsx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Orígenes permitidos
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3002",
                "https://d633d01cb1db.ngrok-free.app",
                "http://localhost:3001", // frontend React (Vite alternativo)
                "http://localhost:9090", // Vite dev server alternativo
                "http://127.0.0.1:3000",
                "http://127.0.0.1:3002"
        ));

        // ✅ Métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Headers permitidos
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // ✅ Permitir credenciales (si usás cookies o headers con token)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
