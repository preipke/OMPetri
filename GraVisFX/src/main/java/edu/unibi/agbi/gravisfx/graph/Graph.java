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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PR
 */
public class Graph
{
    private final Model model;
    private final TopLayer topLayer;
    
    private final List<GravisEdge> edges = new ArrayList();
    
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
        if (model.addNode(node)) {
            topLayer.getNodeLayer().getChildren().add(node.getShape());
        }
    }
    
    public void removeNode(IGravisNode node) {
        if (model.removeNode(node)) {
            topLayer.getChildren().remove(node.getShape());
        }
    }
    
    public void connectNodes(IGravisNode parent, IGravisNode child) {
        model.connectNodes(parent , child);
    }
    
    public void connectNodes(String parentId, String childId) {
        model.connectNodes(parentId , childId);
    }
    
    public IGravisNode[] getNodes() {
        return model.getNodes();
    }
}
