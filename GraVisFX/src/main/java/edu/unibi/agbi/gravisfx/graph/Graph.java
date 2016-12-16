/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;
import edu.unibi.agbi.gravisfx.graph.layer.LabelLayer;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import java.util.ArrayList;
import java.util.List;

/**
 * The Graph. Serves as a service to access the underlying data models.
 * @author PR
 */
public class Graph
{
    private final TopLayer topLayer;
    
    private final LabelLayer labelLayer;
    private final NodeLayer nodeLayer;
    private final EdgeLayer edgeLayer;
    
    private final List<IGravisNode> nodes;
    private final List<IGravisEdge> edges;
    
    public Graph() {
        
        topLayer = new TopLayer();
        
        labelLayer = topLayer.getLabelLayer();
        nodeLayer = topLayer.getNodeLayer();
        edgeLayer = topLayer.getEdgeLayer();
        
        nodes = new ArrayList();
        edges = new ArrayList();
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
    
    public void add(IGravisNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            nodeLayer.getChildren().add(node.getShape());
        }
    }
    
    public void add(IGravisEdge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
            edgeLayer.getChildren().add(edge.getShape());
        }
    }
    
    public boolean contains(IGravisNode node) {
        return nodes.contains(node);
    }
    
    public boolean contains(IGravisEdge edge) {
        return edges.contains(edge);
    }
    
    public IGravisNode remove(IGravisNode node) {
        
        for (IGravisEdge edge : node.getEdges()) {
            remove(edge);
        }
        nodeLayer.getChildren().remove(node.getShape());
        nodes.remove(node);
        
//        for (IGravisEdge edge : node.getEdges()) {
//            if (edge.getSource() == node) {
//                edge.getTarget().removeParent(node);
//                edge.getTarget().removeEdge(edge);
//            } else {
//                edge.getSource().removeChild(node);
//                edge.getSource().removeEdge(edge);
//            }
//        }
//        node.getEdges().clear();
        
        return node;
    }
    
    public IGravisEdge remove(IGravisEdge edge) {
        
        edgeLayer.getChildren().remove(edge.getShape());
        edges.remove(edge);
        
        return edge;
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
