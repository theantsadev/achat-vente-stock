package com.gestion.achat_vente_stock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Initialisation des données au démarrage
 * Met à jour les utilisateurs existants avec un mot de passe par défaut
 * S'exécute AVANT Hibernate pour préparer la base de données
 */
@Configuration
@Slf4j
public class DataInitializer {

    // BCrypt hash pour 'password123'
    private static final String DEFAULT_PASSWORD = "password123";

    public String getCrypteString(String input) {
        // Simple BCrypt hashing (in real scenarios, use a PasswordEncoder bean)
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(input);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Object passwordColumnInitializer(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // Vérifier si la colonne password existe
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'utilisateur' AND column_name = 'password'",
                    Integer.class);

            if (count == null || count == 0) {
                log.info("Ajout de la colonne password à la table utilisateur...");

                // Ajouter la colonne avec une valeur par défaut
                jdbcTemplate.execute("ALTER TABLE utilisateur ADD COLUMN password TEXT DEFAULT '"
                        + getCrypteString(DEFAULT_PASSWORD) + "'");

                // Mettre à jour les valeurs NULL si existantes
                jdbcTemplate.update("UPDATE utilisateur SET password = ? WHERE password IS NULL",
                        getCrypteString(DEFAULT_PASSWORD));

                // Rendre NOT NULL
                jdbcTemplate.execute("ALTER TABLE utilisateur ALTER COLUMN password SET NOT NULL");

                log.info("Colonne password ajoutée avec succès");
            } else {
                // Mettre à jour les mots de passe NULL
                int updated = jdbcTemplate.update(
                        "UPDATE utilisateur SET password = ? WHERE password IS NULL OR password = ''",
                        getCrypteString(DEFAULT_PASSWORD));
                if (updated > 0) {
                    log.info("{} utilisateurs mis à jour avec un mot de passe par défaut", updated);
                }
            }
        } catch (Exception e) {
            log.warn("Erreur lors de l'initialisation des mots de passe: {}", e.getMessage());
        }

        return new Object();
    }
}
