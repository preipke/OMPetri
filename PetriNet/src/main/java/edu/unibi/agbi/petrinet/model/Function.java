/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * An implementation used to represent mathematical functions.
 * The list design has been chosen to easily access and replace specific
 * elements within the function without having to make certain that any
 * similar element is replaced or changed.
 * 
 * @author preipke
 */
public class Function
{
    private final ArrayList<FunctionElement> functionElements;
    private final HashSet<String> parameterIds;

    private String unit = "";

    public Function() {
        functionElements = new ArrayList();
        parameterIds = new HashSet();
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
    
    /**
     * Gets the list of elements that represents this function.
     * 
     * @return 
     */
    public ArrayList<FunctionElement> getElements() {
        return functionElements;
    }

    /**
     * Gets the set of parameters used inside this function.
     *
     * @return
     */
    public HashSet<String> getParameterIds() {
        return parameterIds;
    }

    @Override
    public String toString() {
        String function = "";
        for (FunctionElement elem : functionElements) {
            function = function + elem.get();
        }
        return function;
    }
}
