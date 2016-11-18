/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.edge.GravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.node.GravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public void addNode(GravisNode node) {
        if (model.addNode(node)) {
            topLayer.getNodeLayer().getChildren().add(node.getShape());
        }
    }
    
    public void removeNode(GravisNode node) {
        if (model.removeNode(node)) {
            topLayer.getChildren().remove(node.getShape());
        }
    }
    
    public GravisNode getNode(String id) {
        return model.getNode(id);
    }
    
    public GravisNode[] getNodes() {
        return model.getNodes();
    }
    
    public void connectNodes(GravisNode parent, GravisNode child) {
        model.connectNodes(parent , child);
    }
    
    public void connectNodes(String parentId, String childId) {
        model.connectNodes(parentId , childId);
    }
}
