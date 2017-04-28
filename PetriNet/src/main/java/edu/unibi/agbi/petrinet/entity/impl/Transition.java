/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.impl;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.abstr.Node;
import edu.unibi.agbi.petrinet.model.Function;

/**
 *
 * @author PR
 */
public class Transition extends Node
{
    private static final String IDENT = "T";
    private static int COUNT = 0;

    private Function function;

    private Type transitionType;

    public Transition() {

        super(IDENT + ++COUNT);
        type = Element.Type.TRANSITION;

        function = new Function();
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
