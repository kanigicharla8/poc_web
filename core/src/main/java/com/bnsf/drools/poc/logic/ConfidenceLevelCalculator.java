package com.bnsf.drools.poc.logic;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.events.internal.ConfidenceLevelChangeEvent;
import com.bnsf.drools.poc.model.Train;

/**
 * Utility to showcase a Java method invocation from the rules
 *
 * Created by rakesh on 9/29/15.
 */
public class ConfidenceLevelCalculator {

    private static CacheRepository cache;

    /**
     *
     * Utility method called from the rules.
     *
     * Since the rules do not have access to a instance of this class the method needs to be static
     *
     * @param cache
     * @param trainId
     * @param confidenceLevelToAdd
     * @return
     */
    public static ConfidenceLevelChangeEvent calculateConfidenceLevelInJava(String trainId, double confidenceLevelToAdd){
        /* load the train */
        Train train = (Train)cache.get(trainId);

        /* update the confidence level */
        double oldConfidenceLevel = train.getConfidenceLevel();
        train.calculatePercentage(confidenceLevelToAdd);

        //update the data in the cache
        cache.put(train.getTrainId(),train);

        double newConfidenceLevel = train.getConfidenceLevel();

        return new ConfidenceLevelChangeEvent(oldConfidenceLevel, newConfidenceLevel, train);
    }

    public static CacheRepository getCache() {
        return cache;
    }

    public static void setCache(CacheRepository cache) {
        ConfidenceLevelCalculator.cache = cache;
    }
}
