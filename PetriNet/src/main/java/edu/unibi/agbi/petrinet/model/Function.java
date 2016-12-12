/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model;

import java.util.List;

/**
 *
 * @author preipke
 */
public class Function {
    
    private String function;
    private String unit;
    
    private List<Parameter> parameter;
    
    public String getUnit() {
        return unit;
    }
    
    @Override
    public String toString() {
        return function;
    }
    
}
