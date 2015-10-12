package com.bnsf.drools.poc.rules.runtime;

/**
 * Created by rakesh on 10/2/15.
 */
public interface RuleRuntimeManager {
    public void init();

    public void init(String configFileName);

    public void destroy();
}
