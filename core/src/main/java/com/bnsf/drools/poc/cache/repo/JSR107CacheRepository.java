package com.bnsf.drools.poc.cache.repo;

import javax.cache.Cache;

/**
 * Created by rakesh on 9/30/15.
 */
public abstract class JSR107CacheRepository<T> implements CacheRepository<T> {

    Cache<String, T> cache = null;

    public JSR107CacheRepository(Cache<String, T> cache) {
        this.cache = cache;
    }

    protected Cache<String, T> getCache() {
        return cache;
    }

    public T get(String key) {
        return cache.get(key);
    }

    public void put(String key, T model) {
        cache.put(key,model);
    }

    public void delete(String key) {
        cache.remove(key);
    }

}
