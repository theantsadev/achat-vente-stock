package com.gestion.achat_vente_stock.referentiel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
     * DTO pour éviter les problèmes de sérialisation avec les relations JPA
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public  class ArticleDTO {
        private Long id;
        private String code;
        private String designation;
        private java.math.BigDecimal prixAchatMoyen;
        private java.math.BigDecimal prixVentePublic;


    }