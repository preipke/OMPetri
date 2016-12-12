/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.entity.PN_Node;
import edu.unibi.agbi.petrinet.entity.PN_Element;

/**
 *
 * @author PR
 */
public abstract class Transition extends PN_Node
{
    private final Function function;
    
    private Type transitionType;
    
    public Transition() {
        
        type = PN_Element.Type.TRANSITION;
        
        function = new Function();
    }

    public Function getFunction() {
        return function;
    }
    
    public Type getTransitionType() {
        return transitionType;
    }
    
    public enum Type {
        CONTINUOUS, DEFAULT, DISCRETE, STOCHASTIC;
    }
}
