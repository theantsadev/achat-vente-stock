package com.gestion.achat_vente_stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AchatVenteStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(AchatVenteStockApplication.class, args);
	}

}
