package com.ehcache.cacheinstanceone.ehcacheinstanceone;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;

import static com.ehcache.cacheinstanceone.ehcacheinstanceone.constants.Constants.CLUSTERED_EHCACHE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class EhCacheTestService
{
    //@Autowired(required = false)
    //@Qualifier("ClusteredCacheManager")
    PersistentCacheManager clusteredEhCacheManager;

   // @BeforeEach
    public void buildTheCacheManager()
    {
        if(null==clusteredEhCacheManager )
        {
            CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder =
                    CacheManagerBuilder.newCacheManagerBuilder() // <1>
                            .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost/my-application")) // <2>
                                    .expecting(c -> c))
                            .withCache(CLUSTERED_EHCACHE_NAME,
                                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                            .heap(10, EntryUnit.ENTRIES))
                                            //.offheap(1, MemoryUnit.MB)
                                            //.with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 1024, MemoryUnit.MB)))

                                            .build());; // <3>
            clusteredEhCacheManager = clusteredCacheManagerBuilder.build(true); // <4>
        }
    }

    @Test
    public void doClusterCacheTest()
    {
        try(PersistentCacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost:9410/my-cache-manager-name"))
                        .autoCreateOnReconnect(server -> server.defaultServerResource("main")))

                .withCache("cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .with(ClusteredResourcePoolBuilder.clusteredDedicated("main", 8, MemoryUnit.MB))))

                .build(true)) {

            Cache<Long, String> cache = cacheManager.getCache("cache", Long.class, String.class);

            cache.put(1L, "one");
            assertThat(cache.get(1L), equalTo("one"));
        }
    }
    @Test
    public void clusteredCacheManagerExample() throws Exception {
        // tag::clusteredCacheManagerExample[]
    //clusteredEhCacheManager.init();
        CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder =
                CacheManagerBuilder.newCacheManagerBuilder() // <1>
                        .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost/my-application")) // <2>
                                .expecting(c -> c))
                        .withCache(CLUSTERED_EHCACHE_NAME,
                                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                        .heap(10, EntryUnit.ENTRIES))
                                        //.offheap(1, MemoryUnit.MB)
                                        //.with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 1024, MemoryUnit.MB)))

                                        .build());; // <3>
        clusteredEhCacheManager = clusteredCacheManagerBuilder.build(true); // <4>
        Cache<String,String> theCache=clusteredEhCacheManager.getCache(CLUSTERED_EHCACHE_NAME, String.class,String.class);
                theCache.put("123","XYZ");
        String retrievedValue=theCache.get("123");
        assertEquals("XYZ",retrievedValue);
        clusteredEhCacheManager.close(); // <5>*/
        //clusteredEhCacheManager.getCache(CLUSTERED_EHCACHE_NAME, String.class,String.class).put("123","XYZ");
        // end::clusteredCacheManagerExample[]
    }

    @Test
    public void clusteredCacheManagerWithDynamicallyAddedCacheExample() throws Exception {
        // tag::clusteredCacheManagerWithDynamicallyAddedCacheExample[]
        CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder
                = CacheManagerBuilder.newCacheManagerBuilder()
                .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost/my-application"))
                        .autoCreateOnReconnect(server -> server.defaultServerResource("primary-server-resource")
                                .resourcePool("resource-pool-a", 8, MemoryUnit.MB)));
        PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(false);
        cacheManager.init();

        try {
            CacheConfiguration<Long, String> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                            .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 2, MemoryUnit.MB))).build();

            Cache<Long, String> cache = cacheManager.createCache("clustered-cache", config);

        } finally {
            cacheManager.close();
        }
        // end::clusteredCacheManagerWithDynamicallyAddedCacheExample[]
    }
}
