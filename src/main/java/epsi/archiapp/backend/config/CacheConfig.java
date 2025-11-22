package epsi.archiapp.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration du cache Caffeine pour optimiser les performances de l'application.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure le gestionnaire de cache avec Caffeine.
     *
     * Paramètres :
     * - expireAfterWrite : Le cache expire après 5 minutes
     * - maximumSize : Maximum 100 entrées en mémoire (évite la surcharge mémoire)
     * - recordStats : Active les statistiques de cache (pour le monitoring)
     *
     * @return Le gestionnaire de cache configuré
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("spectacles", "reservations", "statistics");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Construit la configuration Caffeine.
     *
     * @return Le builder Caffeine configuré
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Expire après 5 minutes
                .maximumSize(100)                        // Maximum 100 entrées
                .recordStats();                          // Enregistre les statistiques
    }
}

