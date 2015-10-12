/**
 * 
 */
package com.bnsf.drools.poc.cache.repo;

import com.bnsf.drools.poc.model.Train;

import javax.cache.Cache;

/**
 * @author rakesh
 *
 */
public class TrainCacheRepository extends JSR107CacheRepository<Train>{

    public TrainCacheRepository(Cache<String, Train> cache) {
        super(cache);
    }
}
