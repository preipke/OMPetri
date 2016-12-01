/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.data;

import edu.unibi.agbi.gnius.exception.data.NodeCreationException;
import edu.unibi.agbi.gnius.model.NodeType;

import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;

import edu.unibi.agbi.petrinet.model.PNNode;
import edu.unibi.agbi.petrinet.model.entity.Place;
import edu.unibi.agbi.petrinet.model.entity.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PR
 */
public class DataController
{
    private static final Map<String,PNNode> pnNodesInternalIds = new HashMap();
    private static final Map<String,ArrayList<PNNode>> pnNodesUserIds = new HashMap();
    
    public static PNNode createNode(NodeType.Type type) throws NodeCreationException {
        
        PNNode node;
        
        switch(type) {
            case PLACE:
                node = new Place();
                break;
            case TRANSITION:
                node = new Transition();
                break;
            default:
                throw new NodeCreationException("No suitable node type selected!");
        }
        
        pnNodesInternalIds.put(node.getInternalId(), node);
        
        return node;
    }
    
    public static void connectNodes(IGravisNode source, IGravisNode target) {
        
    }
    
    public static void copyNodes(List<IGravisNode> nodes) {
        
    }
    
    public static void deleteNodes() {
        
        
    }
    
    
    /**
     * Ideas for functionality.
     * 
     * unused shapes: remove all shapes not linked to node in petri net
     * node table overview: center view to on graph
     * 
     * copy node(s)
     * clone node(s)
     * delete node(s)
     * connect nodes
     * 
     * right clicking node: options for...
     */
}
