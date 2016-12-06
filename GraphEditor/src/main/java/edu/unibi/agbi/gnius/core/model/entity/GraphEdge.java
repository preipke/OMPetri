/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity;

import edu.unibi.agbi.petrinet.model.entity.Arc;

/**
 *
 * @author PR
 */
public class GraphEdge extends Arc implements GraphNode
{
    private final GraphNode.Type nodeType;
    private Type edgeType;

    public GraphEdge() {
        nodeType = GraphNode.Type.EDGE;
    }
    
    @Override
    public GraphNode.Type getType() {
        return nodeType;
    }
    
    public enum Type
    {
        DEFAULT, EQUAL, INHIBITORY, READ, RESET;
    }
}
