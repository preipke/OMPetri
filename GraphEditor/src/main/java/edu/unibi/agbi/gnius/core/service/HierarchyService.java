/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.editor.model.HierarchyController;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PR
 */
@Service
public class HierarchyService
{
    @Autowired private Calculator calculator;
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private HierarchyController hierarchyController;
    @Autowired private SelectionService selectionService;

    /**
     * Climbs one level in the graph layer hierarchy.
     */
    public void climb() {
        if (dataService.getGraph().getParentGraph() != null) {
            show(dataService.getGraph().getParentGraph());
        }
    }

    /**
     * Opens a cluster, showing its graph layers.
     *
     * @param cluster
     */
    public void open(IGraphCluster cluster) {
        show(cluster.getGraph());
    }

    /**
     * Shows a graph layer.
     *
     * @param graph
     */
    public void show(Graph graph) {
        dataService.setGraph(graph);
        hierarchyController.update();
        selectionService.unselectAll();
    }

    /**
     * Groups and hides elements in a new cluster.
     *
     * @param selected
     * @return
     * @throws DataServiceException
     */
    public synchronized IGraphCluster cluster(List<IGraphElement> selected) throws DataServiceException {

        if (selected.isEmpty()) {
            messengerService.addWarning("Nothing was selected for clustering!");
            return null;
        }

        List<IGraphCluster> clustersInside = new ArrayList();
        List<IGraphNode> nodesInside = new ArrayList();

        for (IGraphElement element : selected) {
            switch (element.getDataElement().getElementType()) {
                case CLUSTER:
                    nodesInside.add((IGraphNode) element);
                    clustersInside.add((IGraphCluster) element);
                    break;
                case PLACE:
                    nodesInside.add((IGraphNode) element);
                    break;
                case TRANSITION:
                    nodesInside.add((IGraphNode) element);
                    break;
                default:
                    messengerService.addWarning("Element '" + element.getId() + "' cannot be selected for clustering! Skipping.");
                    break;
            }
        }
        if (nodesInside.size() + clustersInside.size() <= 1) {
            messengerService.printMessage("Invalid selection for clustering!");
            return null;
        }

        IGraphCluster cluster = create(nodesInside, clustersInside);
        dataService.getDao().setHasChanges(true);

        hierarchyController.update();

        return cluster;
    }

    /**
     * Restores and shows all the nodes stored within the given cluster(s).
     *
     * @param selected
     */
    public synchronized void restore(List<IGraphElement> selected) {

        if (selected.isEmpty()) {
            return;
        }

        List<IGraphCluster> clusters = new ArrayList();

        for (IGraphElement element : selected) {
            switch (element.getDataElement().getElementType()) {
                case CLUSTER:
                    clusters.add((IGraphCluster) element);
                    break;
                default:
                    messengerService.addWarning("Element '" + element.getId() + "' is not a cluster! Skipping.");
            }
        }

        for (IGraphCluster cluster : clusters) {
            try {
                remove(cluster);
            } catch (DataServiceException ex) {
                messengerService.addException("Cannot ungroup cluster!", ex);
            }
        }

        hierarchyController.update();
    }

    /**
     * Creates a cluster, moving all given nodes and related arcs to a new graph
     * layer.
     *
     * @param nodes
     * @param clusters
     * @return
     * @throws DataServiceException
     */
    private IGraphCluster create(List<IGraphNode> nodes, List<IGraphCluster> clusters) throws DataServiceException {

        Graph graph;
        Graph graphChild;

        DataArc dataArc;
        DataCluster dataCluster;
        DataClusterArc dataClusterArc;

        GraphCluster shapeCluster;
        IGraphArc arc;

        Set<IGraphArc> arcsInside = new HashSet();
        Set<IGraphArc> arcsToOutside = new HashSet();
        Set<IGraphArc> arcsFromOutside = new HashSet();

        // Determine wether arcs connect to nodes in or outside of the cluster
        for (IGraphNode node : nodes) {
            for (IGravisConnection connection : node.getConnections()) {
                arc = (IGraphArc) connection;
                if (!nodes.contains(arc.getTarget())) { // target OUTSIDE
                    arcsToOutside.add(arc);
                } else if (!nodes.contains(arc.getSource())) { // source OUTSIDE
                    arcsFromOutside.add(arc);
                } else {
                    arcsInside.add(arc);
                }
            }
        }

        // Reassign arcs that connect to nodes outside the new graph level
        for (IGraphCluster cluster : clusters) {
            for (IGraphArc arcFromOutside : cluster.getDataElement().getArcsFromOutside()) {
                if (!nodes.contains(arcFromOutside.getSource())) {
//                    arcsFromOutside.add(arcFromOutside);
                    cluster.getDataElement().getArcsFromOutside().remove(arcFromOutside);
                }
            }
            for (IGraphArc arcToOutside : cluster.getDataElement().getArcsToOutside()) {
                if (!nodes.contains(arcToOutside.getTarget())) {
//                    arcsToOutside.add(arcToOutside);
                    cluster.getDataElement().getArcsToOutside().remove(arcToOutside);
                }
            }
        }

        // Create cluster data and shape
        dataCluster = new DataCluster(dataService.getClusterId(), clusters, nodes, arcsInside, arcsFromOutside, arcsToOutside);
        shapeCluster = new GraphCluster(dataCluster.getId(), dataCluster);

        // Get graphs and reassign hierarchy
        graph = dataService.getGraph();
        graphChild = dataCluster.getGraph();
        graphChild.nameProperty().set(dataCluster.getId());
        graphChild.setParentGraph(graph);
        clusters.forEach(c -> c.getGraph().setParentGraph(graphChild));

        // Add and style cluster shape
        graph.add(shapeCluster);
        dataService.styleElement(shapeCluster);

        Point2D pos = calculator.getCenterN(nodes);
        shapeCluster.setTranslateX(pos.getX());
        shapeCluster.setTranslateY(pos.getY());

        // Move arcs and nodes from graph to child graph
        for (IGraphNode n : nodes) {
            graph.remove(n);
            graphChild.add(n);
        }
//        for (IGraphArc a : arcs) {
//            graph.remove(a);
//        }
        for (IGraphArc a : arcsInside) {
            graphChild.add(a);
        }

        // Create new connections for arcs with target node outside the cluster
        for (IGraphArc arcOutgoing : arcsToOutside) {

            dataClusterArc = new DataClusterArc(shapeCluster.getDataElement(), arcOutgoing.getTarget().getDataElement());
            arc = dataService.createConnection(shapeCluster, arcOutgoing.getTarget(), dataClusterArc); // create arc to obtain correct ID

            if (!graph.contains(arc)) {
                graph.add(arc);
                dataService.styleElement(arc);
            } else {
                arc = (IGraphArc) graph.getConnection(arc.getId());
            }

            dataClusterArc = (DataClusterArc) arc.getDataElement();
            dataClusterArc.getShapes().add(arc);

            // add reference to cluster arc
            dataArc = arcOutgoing.getDataElement();
            dataArc.getShapes().clear();
            dataArc.getShapes().add(arc);
        }

        // Create new connections for arcs with source node outside the cluster
        for (IGraphArc arcIncoming : arcsFromOutside) {

            dataClusterArc = new DataClusterArc(arcIncoming.getTarget().getDataElement(), shapeCluster.getDataElement());
            arc = dataService.createConnection(arcIncoming.getSource(), shapeCluster, dataClusterArc); // create arc to obtain correct ID

            if (!graph.contains(arc)) {
                graph.add(arc);
                dataService.styleElement(arc);
            } else {
                arc = (IGraphArc) graph.getConnection(arc.getId());
            }

            dataClusterArc = (DataClusterArc) arc.getDataElement();
            dataClusterArc.getShapes().add(arc);

            // add reference to cluster arc
            dataArc = arcIncoming.getDataElement();
            dataArc.getShapes().clear();
            dataArc.getShapes().add(arc);
        }

        return shapeCluster;
    }

    /**
     * Removes the given cluster and un-groups all elements stored inside.
     *
     * @param cluster
     * @return
     */
    private IGraphCluster remove(IGraphCluster cluster) throws DataServiceException {

        Graph graph = cluster.getGraph();
        Graph graphParent = graph.getParentGraph();

        DataArc arcData;
        DataCluster clusterData = cluster.getDataElement();
        DataClusterArc arcClusterData;
        IGraphArc clusterArc;
        IGraphCluster clusterChild;

        Point2D pos = calculator.getCenter(graph.getNodes());
        double translateX = cluster.getShape().getTranslateX() - pos.getX();
        double translateY = cluster.getShape().getTranslateY() - pos.getY();

        for (IGravisNode n : graph.getNodes()) {
            graphParent.add(n);
            n.translateXProperty().set(n.translateXProperty().get() + translateX);
            n.translateYProperty().set(n.translateYProperty().get() + translateY);
        }

        for (IGravisConnection c : graph.getConnections()) {
            graphParent.add(c);
        }

        while (!graph.getNodes().isEmpty()) {
            graph.remove(graph.getNodes().iterator().next());
        }

        for (IGraphArc arc : cluster.getDataElement().getArcsFromOutside()) {

            arcData = arc.getDataElement();

            if (graphParent.contains(arc.getSource())) { // should be implicit
                arcData.getShapes().clear();
                arcData.getShapes().add(arc);
                graphParent.add(arc);
                
            } else {
                System.out.println("Source ist NICHT im parent graph layer! -> Arc: " + arc.getId() + " | " + arcData.getId());
                clusterArc = (IGraphArc) arcData.getShapes().iterator().next();
                clusterChild = (IGraphCluster) clusterArc.getSource(); // clusterarc source should be cluster
                System.out.println("Source ist ersetzt durch Cluster! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());

                System.out.println("Target Cluster wird entfernt - finde embedded Cluster der Node enthält!");
                for (IGraphCluster embeddedCluster : clusterData.getClusters()) { // GEHT NICHT - EMBEDDED CLUSTER MÜSSEN ANDERS GEFUNDEN WERDEN
                    if (clusterContainsNode(embeddedCluster, arc.getSource())) {
                        clusterChild = embeddedCluster;
                        break;
                    }
                }
                System.out.println("Embedded Cluster gefunden! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());
                
                // create cluster arc and assign as shape to data element
                arcClusterData = new DataClusterArc(clusterChild.getDataElement(), arc.getTarget().getDataElement());
                clusterArc = dataService.createConnection(clusterChild, arc.getTarget(), arcClusterData); // create arc to obtain correct ID

                if (!graphParent.contains(clusterArc)) {
                    graphParent.add(clusterArc);
                    dataService.styleElement(clusterArc);
                } else {
                    clusterArc = (IGraphArc) graphParent.getConnection(clusterArc.getId());
                }
                System.out.println("Erstelle connection von Cluster zu Target! -> " + clusterArc.getId() + " | " + clusterArc.getDataElement().getId());

                arcClusterData = (DataClusterArc) clusterArc.getDataElement();
                arcClusterData.getShapes().add(clusterArc);

                // add reference to cluster arc
                arcData.getShapes().clear();
                arcData.getShapes().add(clusterArc);
            }

            // check if node is in scene. if not -> find cluster that contains node
            // if source outside -> cluster.getDataElement().getArcsFromOutside().add arc
            // else target outside -> cluster.getDataElement().getArcsToOutside().add arc
        }

        for (IGraphArc arc : cluster.getDataElement().getArcsToOutside()) {

            arcData = arc.getDataElement();

            if (graphParent.contains(arc.getTarget())) { // should be implicit
                arcData.getShapes().clear();
                arcData.getShapes().add(arc);
                graphParent.add(arc);
            } else {
                System.out.println("Target ist NICHT im parent graph layer! -> Arc: " + arc.getId() + " | " + arcData.getId());
                clusterArc = (IGraphArc) arcData.getShapes().iterator().next();
                clusterChild = (IGraphCluster) clusterArc.getTarget(); // clusterarc target should be cluster
                System.out.println("Target ist ersetzt durch Cluster! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());
                
                System.out.println("Target Cluster wird entfernt - finde embedded Cluster der Node enthält!");
                for (IGraphCluster embeddedCluster : clusterData.getClusters()) {
                    if (clusterContainsNode(embeddedCluster, arc.getTarget())) {
                        clusterChild = embeddedCluster;
                        break;
                    }
                }
                System.out.println("Embedded Cluster gefunden! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());

                // create cluster arc and assign as shape to data element
                // create cluster arc and assign as shape to data element
                arcClusterData = new DataClusterArc(arc.getSource().getDataElement(), clusterChild.getDataElement());
                clusterArc = dataService.createConnection(arc.getSource(), clusterChild, arcClusterData); // create arc to obtain correct ID

                if (!graphParent.contains(clusterArc)) {
                    graphParent.add(clusterArc);
                    dataService.styleElement(clusterArc);
                } else {
                    clusterArc = (IGraphArc) graphParent.getConnection(clusterArc.getId());
                }
                System.out.println("Erstelle connection von Source zu Cluster! -> " + clusterArc.getId() + " | " + clusterArc.getDataElement().getId());

                arcClusterData = (DataClusterArc) clusterArc.getDataElement();
                arcClusterData.getShapes().add(clusterArc);

                // add reference to cluster arc
                arcData.getShapes().clear();
                arcData.getShapes().add(clusterArc);
            }
        }

        // remove cluster from parent graph and reassign hierarchy
        graphParent.remove(cluster);
        graphParent.getChildGraphs().remove(graph);
        while (!graph.getChildGraphs().isEmpty()) {
            graph.getChildGraphs().get(0).setParentGraph(graphParent);
        }

        return cluster;
    }
    
    private boolean clusterContainsNode(IGraphCluster cluster, IGraphNode node) {
        
        DataCluster data = cluster.getDataElement();
        
        if (data.getNodes().contains(node)) {
            return true;
        } else {
            if (data.getClusters().stream().anyMatch((clstr) -> (clusterContainsNode(clstr, node)))) {
                return true; 
            }
        }
        return false;
    }
}
