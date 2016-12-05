/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.entity;

import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisEdge;

/**
 *
 * @author PR
 */
public class Edge extends GravisEdge implements GraphNode
{
    private final GraphNode.Type nodeType;
    private Type edgeType;

    public Edge(IGravisNode source , IGravisNode target) {
        super(source , target);
        nodeType = GraphNode.Type.EDGE;
    }
    
    @Override
    public GraphNode.Type getGraphNodeType() {
        return nodeType;
    }
    
    public enum Type
    {
        DEFAULT, EQUAL, INHIBITORY, READ, RESET;
    }
}
