/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service.exception;

/**
 *
 * @author PR
 */
public class NodeCreationException extends Exception {
    
    public NodeCreationException() {
        super();
    }
    
    public NodeCreationException(String msg) {
        super(msg);
    }
    
    public NodeCreationException(Throwable thr) {
        super(thr);
    }
}
