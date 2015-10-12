/**
 * 
 */
package com.bnsf.drools.poc;

import com.bnsf.drools.poc.events.GPSLocoEvent;
import com.bnsf.drools.poc.events.internal.ConfidenceLevelPercentage;
import com.bnsf.drools.poc.model.LocomotiveInventory;
import com.bnsf.drools.poc.model.Train;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * @author rakesh
 *
 */
public class GPSLocoEventTest extends AbstractSpringBNSFEventTest {

	public static final String LOCOMOTIVE_ID = "1234";
	private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setup(){
    	//load cache
    	loadData();
    	
    	this.session = getSession();
    	this.gpsHarvestStream = this.session.getEntryPoint( "GPS Harvest Stream" );
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
	 * Tests a single rule 'GPS Harvest With Missing LocomotiveInventory'
	 *
	 * 1. Assert only 'GPS Harvest With Missing LocomotiveInventory' rule is fired
	 * 2. Assert GPSLocoEvent object is inserted into the working memory
	 * 3. Assert LocomotiveInventory object is loaded from the Cache and the same is inserted into the working memory
	 *
	 */
	@Test
	public void testRule_GPS_Harvest_With_Missing_LocomotiveInventory(){
		String ruleName = "GPS Harvest With Missing LocomotiveInventory";

		//capture the data inserted into the working memory
		captureFactsAndEventsInserted();

		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId(LOCOMOTIVE_ID);
		gpsLocoEvent.setLatitude(100);
		gpsLocoEvent.setLongitude(200);

		this.gpsHarvestStream.insert(gpsLocoEvent);
		this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();

		int numberOfRulesFired = fireAllRules(ruleName);

		assertEquals("Expecting 1 rule to be fired",1,numberOfRulesFired);
		//make sure the rule is fired
		assertRuleFired(ruleName);

		// 2 objects GPSLocoEvent and LocomotiveInventory should have been inserted into the working memory
		assertNumberOfObjectsInsertedIntoMemory(2);

		Iterator<ObjectInsertedEvent> events = getFactsAndEventsInsertedInMemory();

		// check the fact that was inserted by the test
		ObjectInsertedEvent objInserted = events.next();
		Assert.assertThat((GPSLocoEvent) objInserted.getObject(), is(equalTo(gpsLocoEvent)));

		// check the LocomotiveInventory fact that should have been inserted by the rule
		objInserted = events.next();
		Assert.assertThat( (LocomotiveInventory) objInserted.getObject(), is(equalTo(locomotiveInventoryCacheRepository.get(LOCOMOTIVE_ID))));
	}

	@Test
	public void testGPSLocoEvent_Insert(){
		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId("1234");
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
        assertEquals("Confidence Level does not match", ConfidenceLevelPercentage.GPSLocoEvent.getPositivePercentageLevel(), train.getConfidenceLevel(), 0.0f);
	}
	
	@Test
	public void testGPSLocoEvent_Missing_LocomotiveInventory(){
		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId("12344444444");
		gpsLocoEvent.setLatitude(100);
		gpsLocoEvent.setLongitude(200);
		
		this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
        int numberOfRulesFired = fireAllRules();
		logger.info("Rules fired {}", trackingAgendaEventListener.getRulesFiredList());
        assertEquals("Invalid number of rules fired", 2, numberOfRulesFired);
	}
	
	@Test
	public void testGPSLocoEvent_Missing_Train() throws InterruptedException {
		//remove the train from the cache
		trainCacheRepository.delete(TRAIN_ID);
		
		assertNull(trainCacheRepository.get(TRAIN_ID));

		GPSLocoEvent gpsLocoEvent = new GPSLocoEvent();
		gpsLocoEvent.setLocomotiveId("1234");
		gpsLocoEvent.setLatitude(100);
		gpsLocoEvent.setLongitude(200);

		this.gpsHarvestStream.insert(gpsLocoEvent);
        this.session.getAgenda().getAgendaGroup( "evaluation" ).setFocus();
		int numberOfRulesFired = fireAllRules();
		logger.info("Rules fired {}", trackingAgendaEventListener.getRulesFiredList());
        assertEquals("Invalid number of rules fired", 3, numberOfRulesFired);

		//Thread.sleep(2000);
		//numberOfRulesFired = this.session.fireAllRules();

	}

}
