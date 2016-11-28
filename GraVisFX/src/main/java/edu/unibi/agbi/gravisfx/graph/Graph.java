/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.presentation.layer.TopLayer;

/**
 *
 * @author PR
 */
public final class Graph
{
    private final Model model;
    private final TopLayer topLayer;
    
    public Graph() {
        model = new Model();
        topLayer = new TopLayer();
    }
    
    public Model getModel() {
        return model;
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
    
    public void addNode(IGravisNode node) {
        if (model.add(node)) {
            topLayer.getNodeLayer().getChildren().add(node.getShape());
        }
    }
    
    public void addEdge(IGravisEdge edge) {
        if (model.add(edge)){
            topLayer.getEdgeLayer().getChildren().add(edge.getShape());
        }
    }
    
    public boolean containsNode(IGravisNode node) {
        return model.contains(node);
    }
    
    public boolean containsEdge(IGravisEdge edge) {
        return model.contains(edge);
    }
    
    public void removeNode(IGravisNode node) {
        model.remove(node);
        topLayer.getNodeLayer().getChildren().remove(node.getShape());
    }
    
    public void removeEdge(IGravisEdge edge) {
        model.remove(edge);
        topLayer.getEdgeLayer().getChildren().remove(edge.getShape());
    }
    
    public IGravisNode[] getNodes() {
        return model.getNodes();
    }
    
    public IGravisEdge[] getEdges() {
        return model.getEdges();
    }
}
