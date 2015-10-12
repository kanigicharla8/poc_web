package com.bnsf.drools.poc.jboss.drools.util;

import org.kie.api.runtime.rule.ConsequenceExceptionHandler;
import org.kie.api.runtime.rule.Match;
import org.kie.api.runtime.rule.RuleRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rakesh on 10/2/15.
 */
public class DroolsExceptionHandler implements ConsequenceExceptionHandler {
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public void handleException(Match match, RuleRuntime workingMemory, Exception exception) {
        logger.error("Exception caught {} ",exception.getMessage(),exception);
    }
}
