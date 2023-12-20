package com.ehcache.cacheinstanceone.ehcacheinstanceone.configuration;

import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.URI;
import java.time.Duration;
import java.util.function.Supplier;

import static com.ehcache.cacheinstanceone.ehcacheinstanceone.constants.Constants.CLUSTERED_EHCACHE_NAME;


@Configuration
@EnableCaching
public class ClusteredCacheConfiguration
{
    @Bean
    @Qualifier("ClusteredCacheManager")
    @Scope("singleton")
    public CacheManager clusteredCacheManager() {
        CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder =
                CacheManagerBuilder.newCacheManagerBuilder()
                        .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost/my-cache-manager-name"))
                                .autoCreateOnReconnect(server -> server.defaultServerResource("main")))
                        .withCache(CLUSTERED_EHCACHE_NAME,
                                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                        //.heap(1, EntryUnit.ENTRIES)
                                                       // .offheap(1, MemoryUnit.MB)
                                                        .with(ClusteredResourcePoolBuilder.clusteredDedicated("main", 256, MemoryUnit.MB)))

                                        .build());;
        PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(true);
        //cacheManager.close();
        return cacheManager;
    }



   // @Bean
   // @Qualifier("ClusteredCacheManager")
    public CacheManager sophisticatedClusteredCacheManager()
    {
        CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder =
                CacheManagerBuilder.newCacheManagerBuilder()
                        .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost/my-application")).autoCreateOnReconnect(server -> server
                                .defaultServerResource("primary-server-resource")
                                .resourcePool("resource-pool-a", 8, MemoryUnit.MB, "secondary-server-resource")
                                .resourcePool("resource-pool-b", 10, MemoryUnit.MB)))
                        .withCache("clustered-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 8, MemoryUnit.MB))))
                        .withCache("shared-cache-1", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .with(ClusteredResourcePoolBuilder.clusteredShared("resource-pool-a"))))
                        .withCache("shared-cache-2", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .with(ClusteredResourcePoolBuilder.clusteredShared("resource-pool-a"))));
        PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(true);
        return cacheManager;
        //cacheManager.close();
    }

    private static ExpiryPolicy customExpirtyPolicy=new ExpiryPolicy<String,String>(){

        @Override
        public Duration getExpiryForCreation(String s, String s2) {
            if("LOGOUT".equalsIgnoreCase(s2))
            return Duration.ofDays(3);
            else
            return null;
        }

        @Override
        public Duration getExpiryForAccess(String s, Supplier<? extends String> supplier) {
            return null;
        }

        @Override
        public Duration getExpiryForUpdate(String s, Supplier<? extends String> supplier, String s2) {
            return null;
        }

    };


}
