/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.exception.controller;

/**
 *
 * @author pr
 */
public class ControllerNotNullException extends Exception {
    
    public ControllerNotNullException() {
        super();
    }
    
    public ControllerNotNullException(String msg) {
        super(msg);
    }
    
    public ControllerNotNullException(Throwable thr) {
        super(thr);
    }
}
