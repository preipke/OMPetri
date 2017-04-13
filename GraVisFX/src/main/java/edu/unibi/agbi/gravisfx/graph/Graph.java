/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.ConnectionLayer;
import edu.unibi.agbi.gravisfx.graph.layer.LabelLayer;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import java.util.ArrayList;
import java.util.List;

/**
 * The Graph model. Serves as an access object to the stored scene objects.
 * 
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
            if (connection.getSource() != null && connection.getTarget() != null) {
                connection.getSource().getChildren().add(connection.getTarget());
                connection.getSource().getConnections().add(connection);
                connection.getTarget().getParents().add(connection.getSource());
                connection.getTarget().getConnections().add(connection);
            }
        }
    }
    
    public boolean contains(IGravisNode node) {
        return nodes.contains(node);
    }
    
    public boolean contains(IGravisConnection connection) {
        return connections.contains(connection);
    }
    
    public IGravisNode remove(IGravisNode node) {
        while (!node.getConnections().isEmpty()) {
            remove(node.getConnections().get(0)); // while to prevent concurrent modification
        }
        labelLayer.getChildren().remove(node.getLabel());
        nodeLayer.getChildren().removeAll(node.getShapes());
        nodes.remove(node);
        return node;
    }
    
    public IGravisConnection remove(IGravisConnection connection) {
        connectionLayer.getChildren().removeAll(connection.getShapes());
        connections.remove(connection);
        if (connection.getSource() != null && connection.getTarget() != null) {
            connection.getSource().getChildren().remove(connection.getTarget());
            connection.getTarget().getParents().remove(connection.getSource());
            connection.getSource().getConnections().remove(connection);
            connection.getTarget().getConnections().remove(connection);
        }
        return connection;
    }
    
    public List<IGravisNode> getNodes() {
        List<IGravisNode> listCopy = new ArrayList();
        for (IGravisNode node : nodes) {
            listCopy.add(node);
        }
        return listCopy;
    }
    
    public List<IGravisConnection> getConnections() {
        List<IGravisConnection> listCopy = new ArrayList();
        for (IGravisConnection connection : connections) {
            listCopy.add(connection);
        }
        return listCopy;
    }
}
