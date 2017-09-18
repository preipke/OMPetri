/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph;

import edu.unibi.agbi.gravisfx.entity.root.GravisType;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisCluster;
import edu.unibi.agbi.gravisfx.entity.root.connection.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.ConnectionLayer;
import edu.unibi.agbi.gravisfx.graph.layer.LabelLayer;
import edu.unibi.agbi.gravisfx.graph.layer.NodeLayer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    private final Set<IGravisCluster> clusters;
    private final Map<String, IGravisNode> nodes;
    private final Map<String, IGravisConnection> connections;

    private final ConnectionLayer connectionLayer;
    private final LabelLayer labelLayer;
    private final NodeLayer nodeLayer;

    private final StringProperty name;
    
    private final Set<Graph> childGraphs;
    private Graph parentGraph;

    private final Scale scale;

    public Graph() {
        this(null);
    }

    public Graph(Graph parentGraph) {

        this.name = new SimpleStringProperty();
        
        this.clusters = new HashSet();
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

        this.childGraphs = new HashSet();
        this.parentGraph = parentGraph;
        if (parentGraph != null) {
            parentGraph.getChildGraphs().add(this);
        }
    }
    
    public void add(IGravisNode node) {
        if (!nodes.containsKey(node.getId())) {
            if (node.getType() == GravisType.CLUSTER) {
                clusters.add((IGravisCluster) node);
            }
            nodes.put(node.getId(), node);
            nodeLayer.getChildren().addAll(node.getShapes());
            labelLayer.getChildren().add(node.getLabel());
        }
    }

    public void add(IGravisConnection connection) {
        if (!connections.containsKey(connection.getId())) {
            if (connection.getSource() != null && nodes.containsKey(connection.getSource().getId())) {
                connections.put(connection.getId(), connection);
                connectionLayer.getChildren().addAll(connection.getShapes());
                if (connection.getTarget() != null && nodes.containsKey(connection.getTarget().getId())) {
                    connection.getSource().getChildren().add(connection.getTarget());
                    connection.getSource().getConnections().add(connection);
                    connection.getTarget().getParents().add(connection.getSource());
                    connection.getTarget().getConnections().add(connection);
                }
            }
        }
    }

    public void clear() {
        connectionLayer.getChildren().clear();
        labelLayer.getChildren().clear();
        nodeLayer.getChildren().clear();
        connections.clear();
        nodes.clear();
        clusters.clear();
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

    public Collection<IGravisCluster> getClusters() {
        return clusters;
    }
    
    public IGravisNode getNode(String id) {
        return nodes.get(id);
    }

    public Collection<IGravisNode> getNodes() {
        return nodes.values();
    }
    
    public Collection<Node> getNodeLayerChildren() {
        return nodeLayer.getChildren();
    }

    public IGravisNode remove(IGravisNode node) {
        while (!node.getConnections().isEmpty()) {
            remove(node.getConnections().iterator().next()); // while to prevent concurrent modification exception
        }
        labelLayer.getChildren().remove(node.getLabel());
        nodeLayer.getChildren().removeAll(node.getShapes());
        nodes.remove(node.getId());
        if (node.getType() == GravisType.CLUSTER) {
            clusters.remove((IGravisCluster) node);
        }
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

    public Graph getParentGraph() {
        return parentGraph;
    }

    public Set<Graph> getChildGraphs() {
        return childGraphs;
    }

    public Scale getScale() {
        return scale;
    }
    
    @Override
    public String toString() {
        return name.get();
    }
}
