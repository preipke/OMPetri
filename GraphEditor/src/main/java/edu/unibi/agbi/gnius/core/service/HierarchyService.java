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

    /**
     * Climbs up one level in the visual hierarchy.
     */
    public void climb() {
        if (dataService.getDao().getGraphPane().getGraph().getParentGraph() != null) {
            dataService.getDao().getGraphPane().setGraph(dataService.getDao().getGraphPane().getGraph().getParentGraph());
        }
    }

    /**
     * Opens a cluster, moving down one level in the visual graph hierarchy.
     * 
     * @param cluster 
     */
    public void open(IGraphCluster cluster) {
        dataService.getDao().getGraphPane().setGraph(cluster.getGraph());
    }

    /**
     * Shows a graph hierarchy level.
     * 
     * @param graph 
     */
    public void show(Graph graph) {
        dataService.getDao().getGraphPane().setGraph(graph);
    }

    /**
     * Groups and hides elements in a cluster element. 
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
        
        hierarchyController.setDao(dataService.getDao());

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
     * Creates a cluster, assigning all given nodes and related arcs to a new
     * layer.
     *
     * @param nodesInside
     * @param clustersInside
     * @return
     * @throws DataServiceException
     */
    private IGraphCluster create(List<IGraphNode> nodesInside, List<IGraphCluster> clustersInside) throws DataServiceException {

        Graph graph;
        Graph graphNew;

        DataArc dataArcToOutside;
        DataCluster dataCluster;
        DataClusterArc dataClusterArc;
        
        GraphCluster shapeCluster;
        IGraphArc arc;

        Set<IGraphArc> arcsInside = new HashSet();
        Set<IGraphArc> arcsToOutside = new HashSet();

        List<IGraphArc> arcsSourceOutside = new ArrayList();
        List<IGraphArc> arcsTargetOutside = new ArrayList();

        // Determine wether arcs connect to nodes in or outside of the cluster
        for (IGraphNode node : nodesInside) {

            for (IGravisConnection connection : node.getConnections()) {
                arc = (IGraphArc) connection;

                if (!nodesInside.contains(arc.getSource())) { // source is OUTSIDE
                    arcsToOutside.add(arc);
                    arcsSourceOutside.add(arc);
                } else if (!nodesInside.contains(arc.getTarget())) { // target is OUTSIDE
                    arcsToOutside.add(arc);
                    arcsTargetOutside.add(arc);
                } else {
                    arcsInside.add(arc);
                }
            }
        }

        // Create layer for cluster
        graph = dataService.getDao().getGraphPane().getGraph();
        graphNew = new Graph(graph);

        // Reassign parent layer for embedded clusters
        for (IGraphCluster embeddedCluster : clustersInside) {
            embeddedCluster.getGraph().setParentGraph(graphNew);
        }

        // Create and add cluster graph node
        dataCluster = new DataCluster(
                dataService.getClusterId(),
                graphNew,
                nodesInside,
                arcsInside,
                arcsToOutside);
        graphNew.nameProperty().set(dataCluster.getId());
        shapeCluster = new GraphCluster(dataCluster.getId(), dataCluster);

        graph.add(shapeCluster);
        dataService.styleElement(shapeCluster);

        Point2D pos = calculator.getCenter(nodesInside);
        shapeCluster.translateXProperty().set(pos.getX());
        shapeCluster.translateYProperty().set(pos.getY());

        // Create new connections with target node outside the cluster
        for (IGraphArc arcToOutside : arcsTargetOutside) {

            dataArcToOutside = arcToOutside.getDataElement();
            dataArcToOutside.getShapes().remove(arcToOutside); // remove reference to old arc
            
            arc = dataService.createConnection(shapeCluster, arcToOutside.getTarget(), new DataClusterArc(dataCluster));
            arc.setCircleHeadVisible(false);

            if (!graph.contains(arc)) {
                graph.add(arc);
                dataService.styleElement(arc);
            } else {
                arc = (IGraphArc) graph.getConnection(arc.getId());
            }

            dataClusterArc = (DataClusterArc) arc.getDataElement();
            dataClusterArc.getShapes().add(arc);
            
            dataArcToOutside.getShapes().add(arc); // add reference to cluster arc
        }

        // Create new connections with source node outside the cluster
        for (IGraphArc arcToOutside : arcsSourceOutside) {

            dataArcToOutside = arcToOutside.getDataElement();
            dataArcToOutside.getShapes().remove(arcToOutside); // remove reference to old arc
            
            arc = dataService.createConnection(arcToOutside.getSource(), shapeCluster, new DataClusterArc(dataCluster));
            arc.setCircleHeadVisible(false);

            if (!graph.contains(arc)) {
                graph.add(arc);
                dataService.styleElement(arc);
            } else {
                arc = (IGraphArc) graph.getConnection(arc.getId());
            }

            dataClusterArc = (DataClusterArc) arc.getDataElement();
            dataClusterArc.getShapes().add(arc);
            
            dataArcToOutside.getShapes().add(arc); // add reference to new arc
        }

        // Remove clustered arcs and nodes from active layer and move to cluster layer
        for (IGraphNode n : nodesInside) {
            graph.remove(n);
            graphNew.add(n);
        }
        for (IGraphArc a : arcsInside) {
            graph.remove(a);
            graphNew.add(a);
        }
        
        hierarchyController.update();

        return shapeCluster;
    }

    /**
     * Removes the given cluster and un-groups all elements stored inside.
     *
     * @param cluster
     * @return
     */
    private IGraphCluster remove(IGraphCluster cluster) throws DataServiceException {

//        List<IGraphCluster> clustersInside = cluster.getDataElement().getChildClusters();
//        List<IGraphNode> nodesInside = cluster.getDataElement().getClusteredNodesInside();
//        List<IGraphArc> arcsInside = cluster.getDataElement().getClusteredArcsInside();
//        List<IGraphArc> arcsToOutside = cluster.getDataElement().getClusteredArcsToOutside();
//
//        Point2D oldNodesPosition = calculator.getCenter(nodesInside);
//        double translateX = cluster.getShape().getTranslateX() - oldNodesPosition.getX();
//        double translateY = cluster.getShape().getTranslateY() - oldNodesPosition.getY();
//
//        dataService.remove(cluster);
//        dataService.getDao().getHierarchicalNodes().remove(cluster);
//        dataService.getDao().getHierarchicalNodes().addAll(clustersInside);
//
//        for (IGraphCluster clstr : clustersInside) {
//            clstr.translateXProperty().set(clstr.translateXProperty().get() + translateX);
//            clstr.translateYProperty().set(clstr.translateYProperty().get() + translateY);
//            dataService.getGraph().add(clstr);
//        }
//
//        for (IGraphNode node : nodesInside) {
//            if (dataService.getModel().getNodeIds().contains(node.getDataElement().getId())) { // restore only if node has not been deleted
//                node.translateXProperty().set(node.translateXProperty().get() + translateX);
//                node.translateYProperty().set(node.translateYProperty().get() + translateY);
//                dataService.getGraph().add(node);
//                node.getDataElement().getShapes().add(node);
//            }
//        }
//
//        for (IGraphArc arc : arcsInside) {
//            if (dataService.getModel().getArcs().contains(arc.getDataElement())) { // restore only if arc has not been deleted
//                dataService.getGraph().add(arc);
//                arc.getDataElement().getShapes().add(arc);
//            }
//        }
//        for (IGraphArc arc : arcsToOutside) {
//            if (dataService.getModel().getArcs().contains(arc.getDataElement())) { // restore only if arc has not been deleted
//                dataService.getGraph().add(arc);
////                arc.getDataElement().getShapes().add(arc);
//            }
//        }

        return cluster;
    }
}
