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
public class Parameter
{
    private final Type type;
    
    private String name;
    private String value;
    private String note;
    
    public Parameter(String name, String value, String note, Type type) {
        this.name = name;
        this.value = value;
        this.note = note;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }
    
    public enum Type {
        INFO, COMPUTE, FUNCTION;
    }
}
