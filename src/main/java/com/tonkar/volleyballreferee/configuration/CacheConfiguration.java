package com.tonkar.volleyballreferee.configuration;

import org.ehcache.config.builders.*;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.*;

import javax.cache.Caching;
import java.time.Duration;
import java.util.*;

@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String GLOBAL_STATISTICS_CACHE = "globalStatisticsCache";
    public static final String USER_STATISTICS_CACHE   = "userStatisticsCache";

    @Bean
    public CacheManager cacheManager() {
        Map<String, org.ehcache.config.CacheConfiguration<?, ?>> caches = new HashMap<>();

        caches.put(GLOBAL_STATISTICS_CACHE, createCacheConfiguration(1, Duration.ofMinutes(30)));
        caches.put(USER_STATISTICS_CACHE, createCacheConfiguration(20, Duration.ofMinutes(5)));

        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching.getCachingProvider();
        DefaultConfiguration configuration = new DefaultConfiguration(caches, this.getClass().getClassLoader());
        return new JCacheCacheManager(provider.getCacheManager(provider.getDefaultURI(), configuration));
    }

    private org.ehcache.config.CacheConfiguration<Object, Object> createCacheConfiguration(long numberOfEntries, Duration duration) {
        return CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Object.class, Object.class,
                                              ResourcePoolsBuilder.newResourcePoolsBuilder().heap(numberOfEntries, EntryUnit.ENTRIES))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(duration))
                .build();
    }
}