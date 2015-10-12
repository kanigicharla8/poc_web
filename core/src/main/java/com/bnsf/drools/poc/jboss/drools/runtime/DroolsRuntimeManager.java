package com.bnsf.drools.poc.jboss.drools.runtime;

import com.bnsf.drools.poc.rules.runtime.RuleRuntimeManager;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by rakesh on 10/2/15.
 */
public class DroolsRuntimeManager implements RuleRuntimeManager {

    private static final String DEFAULT_CONFIG_FILE_NAME = "classpath:applicationContext.xml";
    ApplicationContext context = null;

    public void init() {
        init(DEFAULT_CONFIG_FILE_NAME);
    }

    public void init(String configFileName) {
        context = new ClassPathXmlApplicationContext(configFileName);
    }

    public void destroy() {
        //NOOP
    }
}
