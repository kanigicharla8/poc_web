package com.bnsf.drools.poc.cache.repo;

/**
 *
 * Base interface for all CRUD operations
 *
 * Created by Rakesh Komulwad on 6/4/2014.
 */
public interface CacheRepository<T> {
    T get(String key);

    void put(String key, T model);

    void delete(String key);
}
