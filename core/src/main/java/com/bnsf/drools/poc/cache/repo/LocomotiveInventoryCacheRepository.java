/**
 * 
 */
package com.bnsf.drools.poc.cache.repo;

import com.bnsf.drools.poc.model.LocomotiveInventory;

import javax.cache.Cache;

/**
 * @author rakesh
 *
 */
public class LocomotiveInventoryCacheRepository extends JSR107CacheRepository<LocomotiveInventory>{

    public LocomotiveInventoryCacheRepository(Cache<String, LocomotiveInventory> cache) {
        super(cache);
    }
}
