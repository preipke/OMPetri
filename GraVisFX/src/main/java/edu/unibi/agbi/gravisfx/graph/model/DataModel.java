/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.model;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Model used to store all nodes within the graph.
 * @author PR
 */
public class DataModel
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
        disconnect(node);
        return nodes.remove(node);
    }
    
    public boolean remove(IGravisEdge edge) {
        disconnect(edge);
        return edges.remove(edge);
    }
    
    private boolean connect(IGravisEdge edge) {
        
        if (nodes.contains(edge.getTarget()) && nodes.contains(edge.getSource())) {
            
            if (!edge.getSource().getChildren().contains(edge.getTarget())) {
                
                if (!edge.getTarget().getParents().contains(edge.getSource())) {

                    edge.getSource().addChildNode(edge.getTarget());
                    edge.getTarget().addParentNode(edge.getSource());

                    edge.getSource().addEdge(edge);
                    edge.getTarget().addEdge(edge);
                    
                    return true;
                }
            }
        }
        return false;
    }
    
    private void disconnect(IGravisEdge edge) {
        
        edge.getSource().getChildren().remove(edge.getTarget());
        edge.getTarget().getParents().remove(edge.getSource());

        edge.getSource().removeEdge(edge);
        edge.getTarget().removeEdge(edge);
    }
    
    private void disconnect(IGravisNode node) {
        
        for (IGravisEdge edge : node.getEdges()) {
            
            if (edge.getSource() == node) {
                edge.getTarget().removeParent(node);
            } else {
                edge.getSource().removeChild(node);
            }
            edge.getSource().removeEdge(edge);
            edge.getTarget().removeEdge(edge);
        }
    }
    
    public IGravisNode[] getNodes() {
        
        IGravisNode[] nodesArray = new IGravisNode[this.nodes.size()];
        
        for (int i = 0; i < nodesArray.length; i++) {
            nodesArray[i] = this.nodes.get(i);
        }
        
        return nodesArray;
    }
    
    public IGravisEdge[] getEdges() {
        
        IGravisEdge[] edgesArray = new IGravisEdge[this.edges.size()];
        
        for (int i = 0; i < edgesArray.length; i++) {
            edgesArray[i] = this.edges.get(i);
        }
        
        return edgesArray;
    }
}
