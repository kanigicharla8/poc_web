package com.bnsf.drools.poc.exception;

/**
 * Created to showcase exception handling in the rules
 *
 * Created by rakesh on 10/2/15.
 */
public class CustomException extends Exception{
    public CustomException(String message) {
        super(message);
    }
}
