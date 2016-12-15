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
public class AssignmentDeniedException extends Exception
{
    public AssignmentDeniedException() {
        super();
    }

    public AssignmentDeniedException(String msg) {
        super(msg);
    }

    public AssignmentDeniedException(Throwable thr) {
        super(thr);
    }
    
    public AssignmentDeniedException(String msg, Throwable thr) {
        super(msg , thr);
    }
}
