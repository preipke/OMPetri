/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author PR
 */
public class Model
{
    private final List<IGravisNode> nodes = new ArrayList();
    private final List<IGravisEdge> edges = new ArrayList();
    
    public boolean add(IGravisNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            return true;
        }
        return false;
    }
    
    public boolean add(IGravisEdge edge) {
        if (!edges.contains(edge)) {
            if (connect(edge)) {
                edges.add(edge);
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(IGravisNode node) {
        return nodes.contains(node);
    }
    
    public boolean contains(IGravisEdge edge) {
        return edges.contains(edge);
    }
    
    public boolean remove(IGravisNode node) {
        return nodes.remove(node);
    }
    
    public boolean remove(IGravisEdge edge) {
        disconnect(edge);
        return edges.remove(edge);
    }
    
    public IGravisNode[] getNodes() {
        
        IGravisNode[] nodes = new IGravisNode[this.nodes.size()];
        
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = this.nodes.get(i);
        }
        
        return nodes;
    }
    
    public IGravisEdge[] getEdges() {
        
        IGravisEdge[] edges = new IGravisEdge[this.edges.size()];
        
        for (int i = 0; i < edges.length; i++) {
            edges[i] = this.edges.get(i);
        }
        
        return edges;
    }
    
    private boolean connect(IGravisEdge edge) {
        if (nodes.contains(edge.getTarget()) && nodes.contains(edge.getSource())) {
            edge.getSource().addChildNode(edge.getTarget());
            edge.getTarget().addParentNode(edge.getSource());
            return true;
        }
        return false;
    }
    
    private void disconnect(IGravisEdge edge) {
        edge.getSource().getChildren().remove(edge.getTarget());
        edge.getTarget().getParents().remove(edge.getSource());
    }
}
