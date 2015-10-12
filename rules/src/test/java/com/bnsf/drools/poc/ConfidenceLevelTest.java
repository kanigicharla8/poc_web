package com.bnsf.drools.poc;

import com.bnsf.drools.poc.events.AEIEvent;
import com.bnsf.drools.poc.events.GPSLocoEvent;
import com.bnsf.drools.poc.events.internal.ConfidenceLevelPercentage;
import com.bnsf.drools.poc.events.internal.MissingGPSLocoEvent;
import com.bnsf.drools.poc.logic.ConfidenceLevelCalculator;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rakesh on 9/16/15.
 */
public class ConfidenceLevelTest extends AbstractSpringBNSFEventTest {

    private static final String LOCOMOTIVE_ID = "1234";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private EntryPoint aeiHarvestStream;

    @Before
    public void setup(){
        //load cache
        loadData();

        this.session = getSession();
        this.gpsHarvestStream = this.session.getEntryPoint("GPS Harvest Stream");
        this.aeiHarvestStream = this.session.getEntryPoint("AEI Harvest Stream");

        //in case java functions/methods are invoked from the rules make sure the dependencies are resolved
        ConfidenceLevelCalculator.setCache(trainCacheRepository);
    }

    @After
    public void cleanup(){
        this.session.dispose();
        this.trackingAgendaEventListener.reset();
    }

    private void loadData() {
        //populate the inventory
        LocomotiveInventory locomotiveInventory = new LocomotiveInventory();
        locomotiveInventory.setLocomotiveId(LOCOMOTIVE_ID);
        locomotiveInventory.setTrainId(TRAIN_ID);
        locomotiveInventoryCacheRepository.put(locomotiveInventory.getId(), locomotiveInventory);

        //populate the trains
        Train train = new Train();
        train.setTrainId(TRAIN_ID);
        trainCacheRepository.put(train.getId(), train);
    }

    /**
     * Test: rule 'GPSLocoEvent did not receive a subsequent GPSLocoEvent'
     * <p/> If a subsequent GPS event is not received in 30m then a MissingGPSLocoEvent event is expected
     *
     * <p/>
     * 1. Insert GPSLocoEvent
     * 2. Assert no rules are fired
     * 3. Advance the clock by 30m
     * 4. Assert only 'GPSLocoEvent did not receive a subsequent GPSLocoEvent' is fired
     * 5. Assert 2 objects are inserted into the working memory
     * 6. Assert the GPSLocoEvent is same as #1
     * 7. Assert the GPSLocoEvent inside MissingGPSLocoEvent is the same as #1
     *
     */
    @Test
    public void testRule_GPSLocoEvent_did_not_receive_a_subsequent_GPSLocoEvent(){
        String ruleName="GPSLocoEvent did not receive a subsequent GPSLocoEvent";

        captureFactsAndEventsInserted();

        GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
        gpsLocoEvent.setLocomotiveId(LOCOMOTIVE_ID);
        gpsLocoEvent.setLatitude(100);
        gpsLocoEvent.setLongitude(200);

        this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        int rulesFired = fireAllRules(ruleName);

        assertEquals("No rules should be fired",0,rulesFired);

        //train should not have any changes
        Train train = trainCacheRepository.get(TRAIN_ID);
        assertNotNull("Train should not be null", train);
        assertEquals("Latitude should not change", 0.0, train.getLatitude(), 0.0f);
        assertEquals("Longitude should not change", 0.0, train.getLongitude(), 0.0f);
        assertEquals("Confidence level not have been computer", 0.0, train.getConfidenceLevel(), 0.0f);

        //advance the clock by 30 min
        advanceClock(30, TimeUnit.MINUTES);

        this.trackingAgendaEventListener.reset();
        rulesFired = fireAllRules(ruleName);

        assertEquals("1 rule should be fired",1,rulesFired);
        assertRuleFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");

        // 2 objects GPSLocoEvent and MissingGPSLocoEvent should have been inserted into the working memory
        assertNumberOfObjectsInsertedIntoMemory(2);

        Iterator<ObjectInsertedEvent> events = getFactsAndEventsInsertedInMemory();

        GPSLocoEvent prevGPSEvent = (GPSLocoEvent) events.next().getObject();
        Assert.assertThat(prevGPSEvent, is(equalTo(gpsLocoEvent)));

        // MissingGPSLocoEvent fact should have been inserted by the rule
        MissingGPSLocoEvent missingGPSLocoEvent = (MissingGPSLocoEvent) events.next().getObject();
        Assert.assertThat(missingGPSLocoEvent.getGpsLocoEvent() , is(equalTo(prevGPSEvent)));
    }

    /**
     * Test for missing GPSLocoEvents
     * For a GPSLocoEvent a subsequent GPSLocoEvent is expected in 30m
     * This tests the missing GPSLocoEvent
     *
     */
    @Test
    public void testGPSLocoEvent_Handle_MissingGPSLocoEvent() {
        GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
        gpsLocoEvent.setLocomotiveId(LOCOMOTIVE_ID);
        gpsLocoEvent.setLatitude(100);
        gpsLocoEvent.setLongitude(200);

        this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        fireAllRules();

        //train should have the latest GPS co-ordinate set
        Train train = trainCacheRepository.get(TRAIN_ID);
        assertNotNull("Train should not be null", train);
        assertEquals("Latitude should be same", gpsLocoEvent.getLatitude(), train.getLatitude(), 0.0f);
        assertEquals("Longitude should be same", gpsLocoEvent.getLongitude(), train.getLongitude(), 0.0f);
        assertEquals("Confidence level not increased", ConfidenceLevelPercentage.GPSLocoEvent.getPositivePercentageLevel(), train.getConfidenceLevel(), 0.0f);

        //advance the clock by 30 min
        advanceClock(30, TimeUnit.MINUTES);
        this.trackingAgendaEventListener.reset();
        fireAllRules();
        assertRuleFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");
        assertRuleFired("Handle MissingGPSLocoEvent");

        train = trainCacheRepository.get(TRAIN_ID);
        //confidence level should have reduced from 10 to 5
        assertEquals("Confidence level not reduced", 5.0,train.getConfidenceLevel(),0.0f);
        assertRuleFired("Handle Changes to ConfidenceLevels");
    }

    /**
     * Insert 2 GPSLocoEvents, advance the clock 30min, rules should handle the 3rd missing GPSLocoEvent
     */
    @Test
    public void testGPSLocoEvent_SubsequentEvent(){
        GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
        gpsLocoEvent.setLocomotiveId(LOCOMOTIVE_ID);
        gpsLocoEvent.setLatitude(100);
        gpsLocoEvent.setLongitude(200);

        this.gpsHarvestStream.insert( gpsLocoEvent );
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        fireAllRules();

        //train should have the latest GPS co-ordinate set
        Train train = trainCacheRepository.get(TRAIN_ID);
        assertNotNull("Train should not be null", train);
        assertEquals("Latitude should be same", gpsLocoEvent.getLatitude(), train.getLatitude(), 0.0f);
        assertEquals("Longitude should be same", gpsLocoEvent.getLongitude(), train.getLongitude(), 0.0f);

        //send a subsequent GPSLocoEvent
        GPSLocoEvent gpsLocoEvent_2 = new GPSLocoEvent();
        gpsLocoEvent_2.setLocomotiveId(LOCOMOTIVE_ID);
        gpsLocoEvent_2.setLatitude(300);
        gpsLocoEvent_2.setLongitude(400);

        //advance the clock by 15 min
        advanceClock(15, TimeUnit.MINUTES);
        this.gpsHarvestStream.insert(gpsLocoEvent_2);
        this.session.getAgenda().getAgendaGroup("evaluation").setFocus();
        this.trackingAgendaEventListener.reset();
        fireAllRules();

        //rule should not be fired gpsLocoEvent_2 should have been processed
        assertRuleNotFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");

        //advance the clock by 30 min
        advanceClock(30, TimeUnit.MINUTES);
        this.trackingAgendaEventListener.reset();
        fireAllRules();

        //rule should have been fired as the 3rd event is missing
        assertRuleFired("GPSLocoEvent did not receive a subsequent GPSLocoEvent");
    }

    /**
     * For a GPSLocoEvent a subsequent AEIEvent is expected in 45m
     * This tests the AEIEvent is received for a GPSLocoEvent in 15m
     *
     */
    @Test
    public void test_AEIEvent_After_GPSLocoEvent(){
        GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
        gpsLocoEvent.setLocomotiveId(LOCOMOTIVE_ID);
        gpsLocoEvent.setLatitude(100);
        gpsLocoEvent.setLongitude(200);

        this.gpsHarvestStream.insert(gpsLocoEvent);
        //this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        fireAllRules();

        AEIEvent aeiEvent = new AEIEvent();
        aeiEvent.setLocomotiveId(LOCOMOTIVE_ID);
        aeiEvent.setAEIReaderId("Reader-123");

        this.trackingAgendaEventListener.reset();

        //advance the clock by 15 min
        advanceClock(15, TimeUnit.MINUTES);
        //insert AEIEvent
        this.aeiHarvestStream.insert(aeiEvent);
        this.session.getAgenda().getAgendaGroup("evaluation").setFocus();

        //fire rules
        fireAllRules();

        assertRuleFired("GPSLocoEvent received a subsequent AEIEvent");

        Train train = trainCacheRepository.get(TRAIN_ID);

        //confidence should have changed from 10 to 15 - train received a subsequent AEI event
        assertEquals("Confidence level not increased", 15.0, train.getConfidenceLevel(), 0.0f);
        assertRuleFired("Handle Changes to ConfidenceLevels");
    }

    /**
     * For a GPSLocoEvent a subsequent AEIEvent is expected in 45m
     * This tests the AEIEvent is not received for a GPSLocoEvent in 45m
     *
     */
    @Test
    public void test_Missing_AEIEvent_After_GPSLocoEvent() {
        GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
        gpsLocoEvent.setLocomotiveId(LOCOMOTIVE_ID);
        gpsLocoEvent.setLatitude(100);
        gpsLocoEvent.setLongitude(200);

        this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        fireAllRules();

        //advance the clock by 45 min
        advanceClock(45, TimeUnit.MINUTES);

        this.trackingAgendaEventListener.reset();
        fireAllRules();
        assertRuleFired("GPSLocoEvent did not receive a subsequent AEIEvent");

        Train train = trainCacheRepository.get(TRAIN_ID);
        //confidence level should have reduced from 8 to 3
        //confidence changed from 10 to 2 - train did not receive a subsequent AEI event
        //confidence changed from 8 to 3 - train did not receive a subsequent GPSLoco event
        assertEquals("Confidence level not reduced", 3.0, train.getConfidenceLevel(), 0.0f);
        assertRuleFired("Handle Changes to ConfidenceLevels");
    }

}
