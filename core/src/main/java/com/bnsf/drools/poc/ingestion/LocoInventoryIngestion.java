package com.bnsf.drools.poc.ingestion;

import com.google.common.base.Splitter;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by rakesh on 10/6/15.
 */
public class LocoInventoryIngestion {
    private CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository;
    private CacheRepository<Train> trainCacheRepository;

    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        String strEvent = (String) in.getBody();

        // Handling incoming message
        //split the message
        Splitter pipeSplitter = Splitter.on('|').trimResults();
        Iterable<String> tokens = pipeSplitter.split(strEvent);
        Iterator<String> tokenIterator = tokens.iterator();

        String locoID=tokenIterator.next();
        String trainID=tokenIterator.next();

        LocomotiveInventory loco=new LocomotiveInventory();

        loco.setLocomotiveId(locoID);
        loco.setTrainId(trainID);
        loco.setCreatedTimestamp(new Date());
        loco.setLastUpdatedTimestamp(new Date());

        Train train=new Train();
        train.setTrainId(trainID);
        train.setCreatedTimestamp(new Date());
        train.setLastUpdatedTimestamp(new Date());

        locomotiveInventoryCacheRepository.put(locoID, loco);
        trainCacheRepository.put(trainID, train);

    }

    public CacheRepository<LocomotiveInventory> getLocomotiveInventoryCacheRepository() {
        return locomotiveInventoryCacheRepository;
    }

    public void setLocomotiveInventoryCacheRepository(CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository) {
        this.locomotiveInventoryCacheRepository = locomotiveInventoryCacheRepository;
    }

    public CacheRepository<Train> getTrainCacheRepository() {
        return trainCacheRepository;
    }

    public void setTrainCacheRepository(CacheRepository<Train> trainCacheRepository) {
        this.trainCacheRepository = trainCacheRepository;
    }
}
