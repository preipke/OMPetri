/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.entity;

import edu.unibi.agbi.petrinet.model.entity.Transition;

/**
 *
 * @author PR
 */
public class GraphTransition extends Transition implements GraphNode
{
    private final GraphNode.Type nodeType;
    private Type transitionType;
    
    public GraphTransition() {
        nodeType = GraphNode.Type.TRANSITION;
    }
    
    @Override
    public GraphNode.Type getType() {
        return nodeType;
    }
    public enum Type
    {
        CONTINUOUS, DEFAULT, DISCRETE, STOCHASTIC;
    }
}
