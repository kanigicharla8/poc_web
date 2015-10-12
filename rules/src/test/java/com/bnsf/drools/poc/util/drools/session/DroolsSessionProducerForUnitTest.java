package com.bnsf.drools.poc.util.drools.session;

import com.bnsf.drools.poc.jboss.drools.session.DroolsSessionProducer;
import com.bnsf.drools.poc.jboss.drools.util.TrackingAgendaEventListener;

import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;

import static org.mockito.Mockito.mock;

/**
 * Created by rakesh on 10/2/15.
 */
public class DroolsSessionProducerForUnitTest extends DroolsSessionProducer{

    private TrackingAgendaEventListener trackingAgendaEventListener = new TrackingAgendaEventListener();
    private DebugRuleRuntimeEventListener drel = mock( DebugRuleRuntimeEventListener.class );;

    @Override
    protected void configureListeners(KieSession kie_session) {
        super.configureListeners(kie_session);

        kie_session.addEventListener(trackingAgendaEventListener);
        kie_session.addEventListener(drel);

        //kie_session.addEventListener( new DebugAgendaEventListener() );
        //kie_session.addEventListener( new DebugRuleRuntimeEventListener() );

    }

    @Override
    protected KieSessionConfiguration getKieSessionConfiguration() {
        KieSessionConfiguration config = super.getKieSessionConfiguration();
        //configure the pseudo clock
        config.setOption( ClockTypeOption.get("pseudo") );

        return config;
    }

    public TrackingAgendaEventListener getTrackingAgendaEventListener() {
        return trackingAgendaEventListener;
    }

    public DebugRuleRuntimeEventListener getDrel() {
        return drel;
    }
}
