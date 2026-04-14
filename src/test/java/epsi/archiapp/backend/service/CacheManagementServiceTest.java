package epsi.archiapp.backend.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheManagementServiceTest {

    @Mock
    private CacheManager cacheManager;

    private CacheManagementService cacheManagementService;

    @BeforeEach
    void setUp() {
        cacheManagementService = new CacheManagementService(cacheManager);
    }

    @Test
    void clearAllCaches_shouldClearEveryConfiguredCache() {
        Cache cacheA = mock(Cache.class);
        Cache cacheB = mock(Cache.class);

        when(cacheManager.getCacheNames()).thenReturn(List.of("spectacles", "reservations"));
        when(cacheManager.getCache("spectacles")).thenReturn(cacheA);
        when(cacheManager.getCache("reservations")).thenReturn(cacheB);

        cacheManagementService.clearAllCaches();

        verify(cacheA).clear();
        verify(cacheB).clear();
    }

    @Test
    void clearCache_shouldClearRequestedCacheWhenItExists() {
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("spectacles")).thenReturn(cache);

        cacheManagementService.clearCache("spectacles");

        verify(cache).clear();
    }

    @Test
    void getCacheNames_shouldDelegateToCacheManager() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("a", "b"));

        assertThat(cacheManagementService.getCacheNames()).containsExactly("a", "b");
    }

    @Test
    void getCacheStatistics_shouldReturnStatsForCaffeineCachesOnly() {
        CaffeineCache caffeineCache = new CaffeineCache("spectacles",
                Caffeine.newBuilder().recordStats().build());

        caffeineCache.getNativeCache().put("k1", "v1");
        Object hit = caffeineCache.getNativeCache().getIfPresent("k1");
        Object miss = caffeineCache.getNativeCache().getIfPresent("missing");
        assertThat(hit).isEqualTo("v1");
        assertThat(miss).isNull();

        when(cacheManager.getCacheNames()).thenReturn(List.of("spectacles", "other"));
        when(cacheManager.getCache("spectacles")).thenReturn(caffeineCache);
        when(cacheManager.getCache("other")).thenReturn(mock(Cache.class));

        Map<String, CacheManagementService.CacheStatisticsInfo> stats = cacheManagementService.getCacheStatistics();

        assertThat(stats).containsKey("spectacles");
        CacheManagementService.CacheStatisticsInfo info = stats.get("spectacles");
        assertThat(info.name()).isEqualTo("spectacles");
        assertThat(info.size()).isGreaterThanOrEqualTo(1);
        assertThat(info.hitCount()).isGreaterThanOrEqualTo(1);
        assertThat(info.missCount()).isGreaterThanOrEqualTo(1);
        assertThat(stats).doesNotContainKey("other");
    }
}


