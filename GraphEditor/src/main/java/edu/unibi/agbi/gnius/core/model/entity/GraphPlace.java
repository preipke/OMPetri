/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity;

import edu.unibi.agbi.petrinet.model.entity.Place;

/**
 *
 * @author PR
 */
public class GraphPlace extends Place implements GraphNode
{
    private final GraphNode.Type nodeType;
    private Type placeType;
    
    public GraphPlace() {
        super();
        nodeType = GraphNode.Type.PLACE;
    }
    
    @Override
    public GraphNode.Type getType() {
        return nodeType;
    }
    
    public enum Type
    {
        CONTINUOUS, DEFAULT, DISCRETE;
    }
}
