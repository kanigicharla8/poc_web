package com.bnsf.drools.poc.cache;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

/**
 * Created by rakesh on 9/29/15.
 */
public class CacheTest {

    private ApplicationContext context = null;

    private CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository;
    private CacheRepository<Train> trainCache;

    @Before
    public void setup(){
        context = new ClassPathXmlApplicationContext("cache/jboss-cache-spring-config.xml");

        //locomotiveInventoryCacheRepository = (CacheRepository<LocomotiveInventory>) context.getBean("locomotiveInventoryCacheRepository");
        //trainCache = (CacheRepository<Train>) context.getBean("trainCacheRepository");
    }

    @Test
    public void test(){
        // Retrieve the system wide cache manager
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        // Define a named cache with default JCache configuration
        Cache<String, String> cache = cacheManager.createCache("namedCache", new MutableConfiguration<String, String>());

        cache.put("hello", "world");
        String value = cache.get("hello");
    }
}
