package com.bnsf.drools.poc.jboss.cache;

import com.bnsf.drools.poc.cache.BNSFCacheManager;

import org.springframework.beans.factory.InitializingBean;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

/**
 * Created by rakesh on 9/30/15.
 */
public class DataGridCacheManager implements BNSFCacheManager, InitializingBean {

    CacheManager cacheManager = null;

    public void afterPropertiesSet() throws Exception {
        initCache();
    }

    public void initCache(){
        // Retrieve the system wide cache manager
        cacheManager = Caching.getCachingProvider().getCacheManager();
    }

    public Cache getCache(String cacheName) {
        // Define a named cache with default JCache configuration
        Cache<String, String> cache = cacheManager.getCache(cacheName);

        //this should never happen, a temporary workaround for the POC
        if(cache == null)
            cache = cacheManager.createCache(cacheName, new MutableConfiguration<String, String>());

        return cache;
    }
}
