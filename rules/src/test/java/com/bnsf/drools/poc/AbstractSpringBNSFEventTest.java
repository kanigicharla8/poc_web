package com.bnsf.drools.poc;

import com.bnsf.drools.poc.cache.repo.CacheRepository;
import com.bnsf.drools.poc.cache.repo.LocomotiveInventoryCacheRepository;
import com.bnsf.drools.poc.cache.repo.TrainCacheRepository;
import com.bnsf.drools.poc.jboss.drools.runtime.DroolsRuntimeManager;
import com.bnsf.drools.poc.jboss.drools.session.DroolsSessionManager;
import com.bnsf.drools.poc.jboss.drools.util.TrackingAgendaEventListener;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;
import com.bnsf.drools.poc.rules.runtime.RuleRuntimeManager;
import com.bnsf.drools.poc.util.SpringUtil;
import com.bnsf.drools.poc.util.drools.session.DroolsSessionProducerForUnitTest;

import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.drools.core.time.SessionPseudoClock;
import org.junit.After;
import org.junit.Before;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Work in progress
 */
public class AbstractSpringBNSFEventTest {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    RuleRuntimeManager ruleRuntimeManager = null;
    DroolsSessionManager sessionManager = null;

    protected KieSession session;
    protected SessionPseudoClock clock = null;

    protected EntryPoint gpsHarvestStream;

    protected CacheRepository<LocomotiveInventory> locomotiveInventoryCacheRepository = null;
    protected CacheRepository<Train> trainCacheRepository = null;

    protected ArgumentCaptor<ObjectInsertedEvent> workingMemoryObjectsCaptor;
    protected DebugRuleRuntimeEventListener drel = null;
    protected TrackingAgendaEventListener trackingAgendaEventListener = null;

    protected static final String TRAIN_ID = "Train-ABC";

    @Before
    public void init(){
        ruleRuntimeManager = new DroolsRuntimeManager();
        ruleRuntimeManager.init("applicationContext-test.xml");

        sessionManager = SpringUtil.getBean(DroolsSessionManager.class);

        DroolsSessionManager sessionManager = SpringUtil.getBean(DroolsSessionManager.class);
        assertNotNull(sessionManager);

        session = sessionManager.getSession();
        assertNotNull(session);

        DroolsSessionProducerForUnitTest sessionProducerForUnitTest = (DroolsSessionProducerForUnitTest) sessionManager.getSessionProducer();
        clock = (SessionPseudoClock) sessionProducerForUnitTest.getClock();
        assertNotNull(clock);
        locomotiveInventoryCacheRepository = SpringUtil.getBean(LocomotiveInventoryCacheRepository.class);
        assertNotNull(locomotiveInventoryCacheRepository);
        trainCacheRepository = SpringUtil.getBean(TrainCacheRepository.class);
        assertNotNull(trainCacheRepository);

        drel = sessionProducerForUnitTest.getDrel();
        trackingAgendaEventListener = sessionProducerForUnitTest.getTrackingAgendaEventListener();
    }

    @After
    public void cleanup() throws Exception {
        sessionManager.destroy();
    }

    protected KieSession getSession(){
        return session;
    }

    /*
      Utility methods
     */

    /**
     *  calls session.fireAllRules()
     *
     * @return number of rules fired
     */
    protected int fireAllRules(){
        return session.fireAllRules();
    }

    /**
     *  calls session.fireAllRules(with RuleNameEqualsAgendaFilter)
     *
     * @param ruleToConsider
     * @return number of rules fired
     */
    protected int fireAllRules(String ruleToConsider){
        return session.fireAllRules(new RuleNameEqualsAgendaFilter(ruleToConsider));
    }

    protected void advanceClock(long amount, TimeUnit unit ){
        clock.advanceTime(amount, unit);
    }

    /**
     * Captures all the Facts and Events inserted into the working memory
     * <p/>Make sure assertNumberOfObjectsInsertedIntoMemory is called before calling getFactsAndEventsInsertedInMemory
     */
    protected void captureFactsAndEventsInserted(){
        workingMemoryObjectsCaptor = ArgumentCaptor.forClass(ObjectInsertedEvent.class);
    }

    /**
     * Verifies the numberExpected facts/events are inserted into the working memory
     * @param numberExpected
     */
    protected void assertNumberOfObjectsInsertedIntoMemory(int numberExpected){
        verify(drel, times(numberExpected)).objectInserted(workingMemoryObjectsCaptor.capture());
    }

    /**
     *
     * @return Facts/Events inserted into the working memory
     */
    protected Iterator<ObjectInsertedEvent> getFactsAndEventsInsertedInMemory(){
        return workingMemoryObjectsCaptor.getAllValues().iterator();
    }

    protected void assertRuleFired(final String ruleName){
        assertTrue("Expecting "+ruleName+" to be fired",trackingAgendaEventListener.isRuleFired(ruleName));
    }

    protected void assertRuleNotFired(final String ruleName) {
        assertFalse("Expecting " + ruleName + " to be fired", trackingAgendaEventListener.isRuleFired(ruleName));
    }

}
