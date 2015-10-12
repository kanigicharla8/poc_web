package com.bnsf.drools.poc.web;

import com.bnsf.drools.poc.jboss.drools.runtime.DroolsRuntimeManager;
import com.bnsf.drools.poc.rules.runtime.RuleRuntimeManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by rakesh on 10/2/15.
 */
public class ContextLoaderListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void contextInitialized(ServletContextEvent event) {
        logger.info("Started Initializing Application");
        RuleRuntimeManager ruleRuntimeManager = new DroolsRuntimeManager();
        ruleRuntimeManager.init();
        logger.info("Finished Initializing Application");
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}
