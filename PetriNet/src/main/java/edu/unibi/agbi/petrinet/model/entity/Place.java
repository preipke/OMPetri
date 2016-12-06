/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity;


import java.util.List;

/**
 *
 * @author PR
 */
public abstract class Place extends PNNode
{
    private double token;
    private double token_t0;
    private double tokenMinimum;
    private double tokenMaximum;
    
    private List<Transition> incomingTransitions;
    private List<Transition> outgoingTransitions;
    
    private Type placeType;
    
    public Place() {
        this.pnNodeType = PNNode.Type.PLACE;
    }
    
    public Type getPlaceType() {
        return placeType;
    }
    
    public enum Type {
        CONTINUOUS, DEFAULT, DISCRETE;
    }
}
