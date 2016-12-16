/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author preipke
 */
public class Function {
    
    private String function;
    private String unit;
    
    private final List<Parameter> parameter;
    
    public Function() {
        parameter = new ArrayList();
    }
    
    public void setFunction(String function) {
        this.function = function;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public List<Parameter> getParameter() {
        return parameter;
    }
    
    @Override
    public String toString() {
        return function;
    }
}
