/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.ArrayList;
import java.util.TreeSet;

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
    private final ArrayList<FunctionElement> elements;
    private final HashSet<Parameter> parameter; // anpassen wegen parameter comparable - tree sinnvoll, ja nein? parameter referenzierung überarbeiten

    private String unit = "";

    public Function() {
        elements = new ArrayList();
        parameter = new TreeSet();
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
        return elements;
    }

    /**
     * Gets the set of parameters used inside this function.
     *
     * @return
     */
    public TreeSet<Parameter> getParameters() {
        return parameter;
    }

    @Override
    public String toString() {
        String function = "";
        for (FunctionElement elem : elements) {
            function = function + elem.get();
        }
        return function;
    }
}
