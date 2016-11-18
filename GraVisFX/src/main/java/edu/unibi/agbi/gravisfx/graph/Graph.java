/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.edge.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public class Graph
{
    private final Model model;
    private final Scale scaling;
    private final TopLayer topLayer;
    
    private final List<GravisEdge> edges;
    
    public Graph() {
        model = new Model();
        
        scaling = new Scale(1.0d , 1.0d);
        
        topLayer = new TopLayer();
        topLayer.getTransforms().add(scaling);
        
        edges = new ArrayList();
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
            topLayer.getChildren().remove(node.getShape());
        }
    }
    
    public IGravisNode getNode(String id) {
        return model.getNode(id);
    }
    
    public IGravisNode[] getNodes() {
        return model.getNodes();
    }
    
    public void connectNodes(IGravisNode parent, IGravisNode child) {
        model.connectNodes(parent , child);
    }
    
    public void connectNodes(String parentId, String childId) {
        model.connectNodes(parentId , childId);
    }
}
