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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<String,IGravisNode> nodes;
    private final Map<String,IGravisConnection> connections;
    
    private int nextGraphNodeId;

    public Graph(int nextGraphNodeId) {
        
        this.nextGraphNodeId = nextGraphNodeId;

        this.topLayer = new TopLayer();

        this.labelLayer = topLayer.getLabelLayer();
        this.nodeLayer = topLayer.getNodeLayer();
        this.connectionLayer = topLayer.getConnectionLayer();

        this.nodes = new HashMap();
        this.connections = new HashMap();
    }

    public TopLayer getTopLayer() {
        return topLayer;
    }

    public void add(IGravisNode node) {
        if (!nodes.containsKey(node.getId())) {
            nodes.put(node.getId(), node);
            nodeLayer.getChildren().addAll(node.getShapes());
            labelLayer.getChildren().add(node.getLabel());
        }
    }

    public void add(IGravisConnection connection) {
        if (!connections.containsKey(connection.getId())) {
            connections.put(connection.getId(), connection);
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
        return nodes.containsKey(node.getId());
    }

    public boolean contains(IGravisConnection connection) {
        return connections.containsKey(connection.getId());
    }

    public IGravisNode remove(IGravisNode node) {
        while (!node.getConnections().isEmpty()) {
            remove(node.getConnections().get(0)); // while to prevent concurrent modification
        }
        labelLayer.getChildren().remove(node.getLabel());
        nodeLayer.getChildren().removeAll(node.getShapes());
        nodes.remove(node.getId());
        return node;
    }

    public IGravisConnection remove(IGravisConnection connection) {
        connectionLayer.getChildren().removeAll(connection.getShapes());
        connections.remove(connection.getId());
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
        for (IGravisNode node : nodes.values()) {
            listCopy.add(node);
        }
        return listCopy;
    }

    public List<IGravisConnection> getConnections() {
        List<IGravisConnection> listCopy = new ArrayList();
        for (IGravisConnection connection : connections.values()) {
            listCopy.add(connection);
        }
        return listCopy;
    }
    
    /**
     * Get the next available id for nodes and increments the counter.
     * This makes sure that no number will be available more than once.
     * 
     * @return 
     */
    public int getNextGraphNodeId() {
        return nextGraphNodeId++;
    }
}
