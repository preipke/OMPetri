/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

/**
 *
 * @author PR
 */
public class FunctionElement
{
    private final String ident;
    private final Type type;

    public FunctionElement(String ident, Type type) {
        this.ident = ident;
        this.type = type;
    }

    public String get() {
        return ident;
    }

    public Type getType() {
        return type;
    }

    public static enum Type
    {
        NUMBER, OPERATOR, PARAMETER;
    }
}
