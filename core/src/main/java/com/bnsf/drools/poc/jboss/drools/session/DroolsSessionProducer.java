package com.bnsf.drools.poc.jboss.drools.session;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.channel.ConfidenceLevelChangeEventChannel;
import com.bnsf.drools.poc.channel.LocomotiveInventoryMissingInCacheEventEventChannel;
import com.bnsf.drools.poc.channel.TrainMissingInCacheEventChannel;
import com.bnsf.drools.poc.jboss.drools.util.DroolsExceptionHandler;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.time.SessionClock;
import org.kie.internal.conf.ConsequenceExceptionHandlerOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by rakesh on 9/23/15.
 */
public class DroolsSessionProducer implements InitializingBean{

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private KieSession session;

    private CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository;
    private CacheRepository<Train> trainCacheRepository;

    private ConfidenceLevelChangeEventChannel confidenceLevelChangeEventChannel;

    public void afterPropertiesSet() throws Exception {
        session = createNewSession();
    }

    private KieSession createNewSession(){

        setDroolsSystemProperties();

        KieSessionConfiguration config = getKieSessionConfiguration();

        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.getKieClasspathContainer();

        KieSession kie_session = kc.newKieSession("BNSF_KS", config);

        //global variables
        configureGlobalVariables(kie_session);

        //channels
        configureChannels(kie_session);

        //listeners
        configureListeners(kie_session);

        return kie_session;
    }

    protected void setDroolsSystemProperties() {
        //register the exception handler
        System.setProperty(ConsequenceExceptionHandlerOption.PROPERTY_NAME,DroolsExceptionHandler.class.getName());
    }

    public SessionClock getClock(){
        return session.getSessionClock();
    }

    protected void configureListeners(KieSession kie_session) {
    }

    protected void configureChannels(KieSession kie_session) {
        kie_session.registerChannel("missing_data_loco_inv", new LocomotiveInventoryMissingInCacheEventEventChannel());
        kie_session.registerChannel("missing_data_train", new TrainMissingInCacheEventChannel());
        kie_session.registerChannel("confidence_level_change_channel", confidenceLevelChangeEventChannel);
    }

    protected void configureGlobalVariables(KieSession kie_session) {
        kie_session.setGlobal("locomotiveInventoryCacheRepository", locomotiveInventoryCacheRepository);
        kie_session.setGlobal("trainCacheRepository", trainCacheRepository);
        kie_session.setGlobal("logger", logger);
    }

    protected KieSessionConfiguration getKieSessionConfiguration() {
        KieSessionConfiguration config = KieServices.Factory.get().newKieSessionConfiguration();
        return config;
    }

    public KieSession getSession() {
        return session;
    }

    public void setSession(KieSession session) {
        this.session = session;
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

    public ConfidenceLevelChangeEventChannel getConfidenceLevelChangeEventChannel() {
        return confidenceLevelChangeEventChannel;
    }

    public void setConfidenceLevelChangeEventChannel(ConfidenceLevelChangeEventChannel confidenceLevelChangeEventChannel) {
        this.confidenceLevelChangeEventChannel = confidenceLevelChangeEventChannel;
    }
}
