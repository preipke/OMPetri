/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.exception;

/**
 *
 * @author PR
 */
public class SimulationServiceException extends Exception
{
    private final Throwable throwable;
    
    public SimulationServiceException(String msg, Throwable throwable) {
        super(msg);
        this.throwable = throwable;
    }
    
    public SimulationServiceException(String msg) {
        super(msg);
        throwable = null;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
}
