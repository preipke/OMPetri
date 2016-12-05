/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.entity;

import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;

/**
 *
 * @author PR
 */
public class Place extends GravisRectangle implements GraphNode
{
    private final GraphNode.Type nodeType;
    
    public Place() {
        super();
        nodeType = GraphNode.Type.PLACE;
    }
    
    @Override
    public GraphNode.Type getGraphNodeType() {
        return nodeType;
    }
    
    public enum Type
    {
        CONTINUOUS, DEFAULT, DISCRETE;
    }
}
