package com.bnsf.drools.poc.cache;

import javax.cache.Cache;

/**
 * Created by rakesh on 9/30/15.
 */
public interface BNSFCacheManager {
    public Cache getCache(final String cacheName);
}
