/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity;

import edu.unibi.agbi.petrinet.model.Function;
import java.util.List;

/**
 *
 * @author PR
 */
public abstract class Transition extends PNNode
{
    private boolean isEnabled;
    
    private Function function;
    
    private List<Place> incomingPlaces;
    private List<Place> outgoingPlaces;
    
    private Type transitionType;
    
    public Transition() {
        this.pnNodeType = PNNode.Type.TRANSITION;
    }
    
    public Type getTransitionType() {
        return transitionType;
    }
    
    public enum Type {
        CONTINUOUS, DEFAULT, DISCRETE, STOCHASTIC;
    }
}
