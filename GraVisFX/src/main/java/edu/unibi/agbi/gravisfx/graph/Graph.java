/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisConnection;
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
    private final List<IGravisConnection> edges;
    
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
            nodeLayer.getChildren().addAll(node.getAllShapes());
        }
    }
    
    public void add(IGravisConnection edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
            edgeLayer.getChildren().addAll(edge.getAllShapes());
        }
    }
    
    public boolean contains(IGravisNode node) {
        return nodes.contains(node);
    }
    
    public boolean contains(IGravisConnection edge) {
        return edges.contains(edge);
    }
    
    public IGravisNode remove(IGravisNode node) {
        
        for (IGravisConnection edge : node.getEdges()) {
            remove(edge);
        }
        
        nodeLayer.getChildren().removeAll(node.getAllShapes());
        nodes.remove(node);
        
        return node;
    }
    
    public IGravisConnection remove(IGravisConnection edge) {
        
        edgeLayer.getChildren().removeAll(edge.getAllShapes());
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
    
    public IGravisConnection[] getEdges() {
        IGravisConnection[] edgesArray = new IGravisConnection[this.edges.size()];
        for (int i = 0; i < edgesArray.length; i++) {
            edgesArray[i] = this.edges.get(i);
        }
        return edgesArray;
    }
}
