package com.gestion.achat_vente_stock.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security
 * Gère l'authentification, les autorisations et la sécurité de l'application
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Gestionnaire d'authentification
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour simplifier (à activer en production)
            .csrf(csrf -> csrf.disable())
            
            // Configuration des autorisations par URL
            .authorizeHttpRequests(auth -> auth
                // Pages publiques (login, ressources statiques)
                .requestMatchers("/login", "/logout", "/error", "/error/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // Administration : réservé aux administrateurs
                .requestMatchers("/admin/**").hasAuthority("ROLE-ADMIN")
                
                // Toutes les autres pages nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Configuration du formulaire de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .usernameParameter("login")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Configuration du logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Gestion de l'accès refusé
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )
            
            // Configuration de la session
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }
}
