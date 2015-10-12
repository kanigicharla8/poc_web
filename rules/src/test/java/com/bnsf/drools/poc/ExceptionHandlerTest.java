package com.bnsf.drools.poc;

import com.bnsf.drools.poc.model.SampleModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by rakesh on 10/2/15.
 */
public class ExceptionHandlerTest extends AbstractSpringBNSFEventTest{

    @Before
    public void setup(){
        this.session = getSession();
        this.gpsHarvestStream = this.session.getEntryPoint( "GPS Harvest Stream" );
    }

    @After
    public void cleanup(){
        this.session.dispose();
        this.trackingAgendaEventListener.reset();
    }

    @Test
    public void testExceptionHandlerRule(){
        String ruleName = "ExceptionHandlerRule";

        SampleModel sampleModel = new SampleModel();

        //capture the data inserted into the working memory
        captureFactsAndEventsInserted();

        try{
            this.session.insert(sampleModel);

            int numberOfRulesFired = fireAllRules(ruleName);

            assertEquals("Expecting 1 rule to be fired",1,numberOfRulesFired);
            //make sure the rule is fired
            assertRuleFired(ruleName);
        } catch(Throwable t){
            fail("Exception should not occur, it should have been handled by the exception handler");
        }
    }
}
