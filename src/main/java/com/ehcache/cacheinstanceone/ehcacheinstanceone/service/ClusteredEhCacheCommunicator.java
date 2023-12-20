package com.ehcache.cacheinstanceone.ehcacheinstanceone.service;

import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.ehcache.cacheinstanceone.ehcacheinstanceone.constants.Constants.CLUSTERED_EHCACHE_NAME;

@Service
public class ClusteredEhCacheCommunicator {

    @Autowired(required = true)
    @Qualifier("ClusteredCacheManager")
    CacheManager clusteredEhCacheManager;

    public void writeToClusteredEhCache(String key, String value) {
        clusteredEhCacheManager.getCache(CLUSTERED_EHCACHE_NAME, String.class, String.class).putIfAbsent(key, value);
    }

    public String getFromClusteredEhcache(String key) {
        return clusteredEhCacheManager.getCache(CLUSTERED_EHCACHE_NAME, String.class, String.class).get(key);
    }

    public void removeFromClusteredCache(String key)
    {
        clusteredEhCacheManager.getCache(CLUSTERED_EHCACHE_NAME, String.class, String.class).remove(key);
    }
}
