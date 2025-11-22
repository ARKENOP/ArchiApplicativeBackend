package epsi.archiapp.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service de gestion du cache.
 * Permet de vider manuellement les caches si nécessaire (utile pour les tests ou l'administration).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheManagementService {

    private final CacheManager cacheManager;

    /**
     * Vide tous les caches de l'application.
     */
    public void clearAllCaches() {
        log.info("Vidage de tous les caches");
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.debug("Cache vidé : {}", cacheName);
                    }
                });
        log.info("Tous les caches ont été vidés");
    }

    /**
     * Vide un cache spécifique.
     *
     * @param cacheName Le nom du cache à vider
     */
    public void clearCache(String cacheName) {
        log.info("Vidage du cache : {}", cacheName);
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cache vidé : {}", cacheName);
        } else {
            log.warn("Cache non trouvé : {}", cacheName);
        }
    }

    /**
     * Récupère les informations sur les caches disponibles.
     *
     * @return Les noms des caches configurés
     */
    public java.util.Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    /**
     * Récupère les statistiques détaillées de tous les caches.
     *
     * @return Map avec les statistiques par cache
     */
    public Map<String, CacheStatisticsInfo> getCacheStatistics() {
        Map<String, CacheStatisticsInfo> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats cacheStats = nativeCache.stats();

                stats.put(cacheName, new CacheStatisticsInfo(
                    cacheName,
                    nativeCache.estimatedSize(),
                    cacheStats.hitCount(),
                    cacheStats.missCount(),
                    cacheStats.hitRate(),
                    cacheStats.evictionCount()
                ));
            }
        });

        return stats;
    }

    /**
     * DTO pour les statistiques du cache
     */
    public record CacheStatisticsInfo(
        String name,
        long size,
        long hitCount,
        long missCount,
        double hitRate,
        long evictionCount
    ) {}
}

