package com.gestion.achat_vente_stock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Jackson pour la sérialisation JSON
 * Nécessaire pour convertir les DTOs en JSON dans le contrôleur
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Support des types Java 8+ (LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Désactiver l'écriture des dates en timestamp
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignorer les propriétés nulles (optionnel)
        // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}