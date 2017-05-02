/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author preipke
 */
public class Function
{
    private String function = "1";
    private String unit = "";

    private final Set<Parameter> parameter;

    public Function() {
        parameter = new HashSet();
    }

    public void set(String function) {
        this.function = function;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    /**
     * Gets the set of parameters used inside this function.
     *
     * @return
     */
    public Set<Parameter> getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return function;
    }
}
