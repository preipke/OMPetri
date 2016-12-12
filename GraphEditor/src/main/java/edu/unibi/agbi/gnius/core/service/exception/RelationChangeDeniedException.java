/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.exception;

/**
 *
 * @author PR
 */
public class RelationChangeDeniedException extends Exception
{
    public RelationChangeDeniedException() {
        super();
    }

    public RelationChangeDeniedException(String msg) {
        super(msg);
    }

    public RelationChangeDeniedException(Throwable thr) {
        super(thr);
    }
    
    public RelationChangeDeniedException(String msg, Throwable thr) {
        super(msg , thr);
    }
}
