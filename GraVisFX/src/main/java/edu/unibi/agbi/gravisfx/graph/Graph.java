/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.presentation.layer.EdgeLayer;
import edu.unibi.agbi.gravisfx.presentation.layer.NodeLayer;
import edu.unibi.agbi.gravisfx.presentation.layer.TopLayer;

/**
 *
 * @author PR
 */
public final class Graph
{
    private final Model model;
    
    private final TopLayer topLayer;
    
    private final NodeLayer nodeLayer;
    private final EdgeLayer edgeLayer;
    
    public Graph() {
        
        model = new Model();
        
        topLayer = new TopLayer();
        
        nodeLayer = topLayer.getNodeLayer();
        edgeLayer = topLayer.getEdgeLayer();
    }
    
    public Model getModel() {
        return model;
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
    
    public void add(IGravisNode node) {
        if (model.add(node)) {
            nodeLayer.getChildren().add(node.getShape());
        }
    }
    
    public void add(IGravisEdge edge) {
        if (model.add(edge)){
            edgeLayer.getChildren().add(edge.getShape());
        }
    }
    
    public boolean containsNode(IGravisNode node) {
        return model.contains(node);
    }
    
    public boolean containsEdge(IGravisEdge edge) {
        return model.contains(edge);
    }
    
    public IGravisNode[] getNodes() {
        return model.getNodes();
    }
    
    public IGravisEdge[] getEdges() {
        return model.getEdges();
    }
    
    public void remove(IGravisNode node) {
        model.remove(node);
        nodeLayer.getChildren().remove(node.getShape());
    }
    
    public void remove(IGravisEdge edge) {
        model.remove(edge);
        edgeLayer.getChildren().remove(edge.getShape());
    }
}
