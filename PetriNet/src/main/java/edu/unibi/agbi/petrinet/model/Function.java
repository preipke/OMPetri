/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * A class used to represent mathematical functions.
 * 
 * @author preipke
 */
public class Function
{
    private final ArrayList<Function> elements;
    private final Type type;
    
    private String value;
    private String unit;
    
    public Function(Type type) {
        this(null, type);
    }
    
    public Function(String value, Type type) {
        this.value = value;
        this.type = type;
        this.elements = new ArrayList();
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
    
    public void addElement(Function element) {
        elements.add(element);
    }
    
    public void addElement(int index, Function element) {
        elements.add(0, element);
    }
    
    public Collection<Function> getElements() {
        ArrayList<Function> elementsRecursive = new ArrayList();
        for (Function elem : elements) {
            if (elem.getType() == Type.FUNCTION) {
                elementsRecursive.addAll(elem.getElements());
            } else {
                elementsRecursive.add(elem);
            }
        }
        return elementsRecursive;
    }

    /**
     * Gets the set of parameters used within this function.
     *
     * @return
     */
    public HashSet<String> getParameterIds() {
        HashSet<String> parameterIds = new HashSet();
        for (Function elem : elements) {
            if (elem.getType() == Type.FUNCTION) {
                parameterIds.addAll(elem.getParameterIds());
            } else if (elem.getType() == Type.PARAMETER) {
                parameterIds.add(elem.getValue());
            }
        }
        return parameterIds;
    }

    @Override
    public String toString() {
        if (getType() == Type.FUNCTION) {
            String function = "";
            for (Function elem : elements) {
//                if (elem.getType() == Type.FUNCTION) {
//                    function += "( " + elem.toString() + ") ";
//                } else {
//                    function += elem.getValue() + " ";
//                }
                function += elem.toString() + " ";
            }
            return function.trim();
        } else {
            return value;
        }
    }

    public Type getType() {
        return type;
    }

    public static enum Type
    {
        FUNCTION, NUMBER, OPERATOR, PARAMETER;
    }
}
