/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.node.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public final class Graph
{
    private final Model model;
    private final Scale scaling;
    private final TopLayer topLayer;
    
    public Graph() {
        model = new Model();
        
        scaling = new Scale(1.0d , 1.0d);
        
        topLayer = new TopLayer();
        topLayer.getTransforms().add(scaling);
    }
    
    public Model getModel() {
        return model;
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
    
    public Scale getScaling() {
        return scaling;
    }
    
    public void addNode(IGravisNode node) {
        node.setScale(scaling);
        if (model.addNode(node)) {
            topLayer.getNodeLayer().getChildren().add(node.getShape());
        }
    }
    
    public void removeNode(IGravisNode node) {
        if (model.removeNode(node)) {
            topLayer.getNodeLayer().getChildren().remove(node.getShape());
        }
    }
    
    public IGravisNode getNode(String id) {
        return model.getNode(id);
    }
    
    public IGravisNode[] getNodes() {
        return model.getNodes();
    }
    
    public void createEdge(IGravisNode parent, IGravisNode child) {
        createEdge(parent.getId() , child.getId());
    }
    
    public void createEdge(String parentId, String childId) {
        
        if (model.connectNodes(parentId , childId)) {

            GravisEdge edge = new GravisEdge(model.getNode(parentId) , model.getNode(childId));
            
            topLayer.getEdgeLayer().getChildren().add(edge);
        }
    }
    
    public void removeEdge(GravisEdge edge) {
        
        model.disconnectNodes(edge);
        
        topLayer.getEdgeLayer().getChildren().remove(edge);
    }
}
