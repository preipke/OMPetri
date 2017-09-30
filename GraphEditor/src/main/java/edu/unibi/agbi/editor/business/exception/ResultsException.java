/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.business.exception;

/**
 *
 * @author PR
 */
public class ResultsException extends Exception
{
    public ResultsException(String msg) {
        super(msg);
    }
    public ResultsException(Throwable throwable) {
        super(throwable);
    }
}
