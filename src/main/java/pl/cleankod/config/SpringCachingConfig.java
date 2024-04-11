package pl.cleankod.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
public class SpringCachingConfig {

    public static final String EXCHANGE_RATE_CACHE_KEY = "rates";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(EXCHANGE_RATE_CACHE_KEY);
    }

    @CacheEvict(value = EXCHANGE_RATE_CACHE_KEY, allEntries = true)
    @Scheduled(cron = "@midnight")
    public void emptyExchangeRatesCache() {

    }
}
