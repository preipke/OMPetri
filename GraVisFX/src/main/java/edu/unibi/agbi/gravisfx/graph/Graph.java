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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Scale;

/**
 * Layer for the graph pane. This class groups and stores elements that are
 * presented in the scene and belong to the graph such as nodes, connections and
 * labels.
 *
 * Layer object hierarchically reference its parental and child layers, allowing
 * switching between layers by removing/adding the according layer from or to
 * the graph pane.
 *
 * Each layer has additional stacking sublayers for nodes, connections and
 * labels.
 *
 * @author PR
 */
public class Graph extends Group
{
    private final Map<String, IGravisNode> nodes;
    private final Map<String, IGravisConnection> connections;

    private final ConnectionLayer connectionLayer;
    private final LabelLayer labelLayer;
    private final NodeLayer nodeLayer;

    private final Scale scale;

    private final StringProperty name;
    private final List<Graph> childGraphs;
    private Graph parentGraph;

    public Graph() {
        this(null);
    }

    public Graph(Graph parentGraph) {

        this.name = new SimpleStringProperty();
        
        this.nodes = new HashMap();
        this.connections = new HashMap();

        this.connectionLayer = new ConnectionLayer();
        this.labelLayer = new LabelLayer();
        this.nodeLayer = new NodeLayer();

        // order matters! connections > nodes > labels
        this.getChildren().add(connectionLayer);
        this.getChildren().add(nodeLayer);
        this.getChildren().add(labelLayer);

        this.scale = new Scale(1.0d, 1.0d);
        this.getTransforms().add(scale);

        this.childGraphs = new ArrayList();
        this.parentGraph = parentGraph;
        if (parentGraph != null) {
            parentGraph.getChildGraphs().add(this);
        }
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
        } else {
            System.out.println("Connection ID already present! -> " + connection.getId());
        }
    }

    public void clear() {
        connectionLayer.getChildren().clear();
        labelLayer.getChildren().clear();
        nodeLayer.getChildren().clear();
        connections.clear();
        nodes.clear();
    }
    
    public boolean contains(String id) {
        return connections.containsKey(id) || nodes.containsKey(id);
    }

    public boolean contains(IGravisConnection connection) {
        return connections.containsKey(connection.getId());
    }

    public boolean contains(IGravisNode node) {
        return nodes.containsKey(node.getId());
    }

    public IGravisConnection getConnection(String id) {
        return connections.get(id);
    }

    public Collection<IGravisConnection> getConnections() {
        return connections.values();
    }
    
    public IGravisNode getNode(String id) {
        return nodes.get(id);
    }

    public Collection<IGravisNode> getNodes() {
        return nodes.values();
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
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the parent for this graph. Also reassigns this layers child
     * relations.
     *
     * @param parentGraph
     */
    public void setParentGraph(Graph parentGraph) {
        if (this.parentGraph != null) {
            this.parentGraph.getChildGraphs().remove(this);
        }
        parentGraph.getChildGraphs().add(this);
        this.parentGraph = parentGraph;
    }

    /**
     *
     * @return
     */
    public Graph getParentGraph() {
        return parentGraph;
    }

    /**
     *
     * @return
     */
    public List<Graph> getChildGraphs() {
        return childGraphs;
    }

    /**
     * Gets the scale applied to the layer.
     *
     * @return
     */
    public Scale getScale() {
        return scale;
    }
    
    public Collection<Node> getNodeLayerChildren() {
        return nodeLayer.getChildren();
    }
    
    @Override
    public String toString() {
        return name.get();
    }
}
