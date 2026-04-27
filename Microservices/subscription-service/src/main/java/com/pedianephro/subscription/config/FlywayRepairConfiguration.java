package com.pedianephro.subscription.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Appelle {@link Flyway#repair()} avant {@link Flyway#migrate()} pour débloquer l'historique
 * après une migration échouée (ex. V3) — sans variable d'environnement à la main.
 * Désactiver si besoin : {@code subscription.flyway.repair-before-migrate=false}.
 */
@Configuration
public class FlywayRepairConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(
            @Value("${subscription.flyway.repair-before-migrate:true}") boolean repairBeforeMigrate) {
        return (Flyway flyway) -> {
            if (repairBeforeMigrate) {
                flyway.repair();
            }
            flyway.migrate();
        };
    }
}
