/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PR
 */
public class Transition extends Node
{
    private final Map<String,Parameter> parameters;
    private Type transitionType;
    private Function function;

    public Transition(String id) {
        super(id);
        super.type = Element.Type.TRANSITION;
        this.function = new Function(Function.Type.FUNCTION);
        this.function.addElement(new Function("1", Function.Type.NUMBER));
        this.parameters = new HashMap();
    }
    
    public Map<String,Parameter> getParameters() {
        return parameters;
    }
    
    public Parameter getParameter(String id) {
        return parameters.get(id);
    }

    public final void setFunction(Function function) {
        this.function = function;
    }

    public final Function getFunction() {
        return function;
    }

    public final void setTransitionType(Type transitionType) {
        this.transitionType = transitionType;
    }

    public final Type getTransitionType() {
        return transitionType;
    }

    public enum Type
    {
        CONTINUOUS, DISCRETE, STOCHASTIC;
    }
}
