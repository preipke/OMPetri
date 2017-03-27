/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisSubElement;
import edu.unibi.agbi.gravisfx.graph.layer.ConnectionLayer;
import edu.unibi.agbi.gravisfx.graph.layer.LabelLayer;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import java.util.ArrayList;
import java.util.List;

/**
 * The Graph model. Serves as an access object to the underlying data.
 * @author PR
 */
public class Graph
{
    private final TopLayer topLayer;
    
    private final LabelLayer labelLayer;
    private final NodeLayer nodeLayer;
    private final ConnectionLayer connectionLayer;
    
    private final List<IGravisNode> nodes;
    private final List<IGravisConnection> connections;
    
    public Graph() {
        
        topLayer = new TopLayer();
        
        labelLayer = topLayer.getLabelLayer();
        nodeLayer = topLayer.getNodeLayer();
        connectionLayer = topLayer.getConnectionLayer();
        
        nodes = new ArrayList();
        connections = new ArrayList();
    }
    
    public TopLayer getTopLayer() {
        return topLayer;
    }
    
    public void add(IGravisNode node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            nodeLayer.getChildren().addAll(node.getShapes());
            labelLayer.getChildren().add(node.getLabel());
        }
    }
    
    public void add(IGravisConnection connection) {
        if (!connections.contains(connection)) {
            connections.add(connection);
            connectionLayer.getChildren().addAll(connection.getShapes());
        }
    }
    
    public boolean contains(IGravisNode node) {
        return nodes.contains(node);
    }
    
    public boolean contains(IGravisConnection connection) {
        return connections.contains(connection);
    }
    
    public IGravisNode remove(IGravisNode node) {
        
        for (IGravisConnection connection : node.getConnections()) {
            remove(connection);
        }
        
        labelLayer.getChildren().remove(node.getLabel());
        nodeLayer.getChildren().removeAll(node.getShapes());
        nodes.remove(node);
        
        return node;
    }
    
    public IGravisConnection remove(IGravisConnection connection) {
        
        connectionLayer.getChildren().removeAll(connection.getShapes());
        connections.remove(connection);
        
        return connection;
    }
    
    public IGravisNode[] getNodes() {
        IGravisNode[] nodesArray = new IGravisNode[this.nodes.size()];
        for (int i = 0; i < nodesArray.length; i++) {
            nodesArray[i] = this.nodes.get(i);
        }
        return nodesArray;
    }
    
    public IGravisConnection[] getConnections() {
        IGravisConnection[] connectionsArray = new IGravisConnection[this.connections.size()];
        for (int i = 0; i < connectionsArray.length; i++) {
            connectionsArray[i] = this.connections.get(i);
        }
        return connectionsArray;
    }
}
