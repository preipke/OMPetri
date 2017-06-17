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
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.IGravisCluster;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
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

        Set<GraphCluster> clusters = new HashSet();
        Set<IGraphNode> nodes = new HashSet();

        for (IGraphElement element : selected) {
            switch (element.getType()) {
                case CLUSTER:
//                    nodes.add((IGraphNode) element);
                    clusters.add((GraphCluster) element);
                    break;
                case NODE:
                    nodes.add((IGraphNode) element);
                    break;
                default:
                    messengerService.addWarning("Element '" + element.getId() + "' cannot be selected for clustering! Skipping.");
                    break;
            }
        }
        if (nodes.size() + clusters.size() <= 1) {
            messengerService.printMessage("Invalid selection for clustering!");
            return null;
        }

        IGraphCluster cluster = create(nodes, clusters);
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
            switch (element.getType()) {
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
    private IGraphCluster create(Set<IGraphNode> nodes, Set<GraphCluster> clusters) throws DataServiceException {

        DataCluster dataCluster;
        GraphCluster cluster;
        IGraphArc arc;

        Graph graph;
        Graph graphChild;

        /**
         * Create cluster data and shape.
         */
        dataCluster = new DataCluster(dataService.getClusterId());
        cluster = new GraphCluster(dataCluster.getId(), dataCluster);

        /**
         * Reassign graph hierarchy.
         */
        graph = dataService.getGraph();
        graphChild = dataCluster.getGraph();
        graphChild.nameProperty().set(dataCluster.getId());
        graphChild.setParentGraph(graph);
        clusters.forEach(c -> c.getGraph().setParentGraph(graphChild));

        /**
         * Determine wether arcs connect to nodes in or outside of the cluster.
         * All arcs connecting to nodes outside the cluster will be clusterarcs.
         */
        nodes.addAll(clusters);
        for (IGraphNode node : nodes) {
            for (IGravisConnection connection : node.getConnections()) {
                arc = (IGraphArc) connection;
                if (!nodes.contains(arc.getTarget())) { // target OUTSIDE
                    dataCluster.getArcs().add(createClusterArc(cluster, arc.getTarget(), arc));
                } else if (!nodes.contains(arc.getSource())) { // source OUTSIDE
                    dataCluster.getArcs().add(createClusterArc(arc.getSource(), cluster, arc));
                } else {
                    dataCluster.getStoredArcs().add(arc);
                }
            }
        }

        /**
         * Add and style the cluster node.
         */
        graph.add(cluster);
        dataService.styleElement(cluster);

        Point2D pos = calculator.getCenterN(nodes);
        cluster.setTranslateX(pos.getX());
        cluster.setTranslateY(pos.getY());

        /**
         * Move nodes, clusters and arcs. All elements inside the new cluster
         * have to be moved from the old graph and to the new graph.
         */
        for (IGraphNode n : nodes) {
            graph.remove(n);
            graphChild.add(n);
        }
        for (IGraphArc a : dataCluster.getStoredArcs()) {
            graphChild.add(a);
        }

        /**
         * Add cluster arcs.
         */
        for (IGraphArc clusterArc : dataCluster.getArcs()) {
            if (!graph.contains(clusterArc)) {
                graph.add(clusterArc);
                dataService.styleElement(clusterArc);
            } else {
                clusterArc = (IGraphArc) graph.getConnection(clusterArc.getId());
            }
        }

        return cluster;
    }
    
    private void addArc(Graph graph, IGraphArc arc) {
        // check for double linked connection
        // if so, recreate shapes
    }
    
    private void addNode(Graph graph, IGraphArc arc) {
        
    }
    
    private void removeArc(Graph graph, IGraphNode node) {
        // check for double linked connection
        // if so, recreate shapes
    }
    
    private void removeNode(Graph graph, IGraphNode node) {
        // check for double linked connection
        // if so, recreate shapes
    }
    
    /**
     * Creates an arc connecting to a cluster node. 
     * 
     * @param source
     * @param target
     * @param relatedArc
     * @return
     * @throws DataServiceException 
     */
    private IGraphArc createClusterArc(IGraphNode source, IGraphNode target, IGraphArc relatedArc) throws DataServiceException {
        
        DataClusterArc data;
        IGraphArc shape;
        
        if (source.getType() != GravisType.CLUSTER && target.getType() != GravisType.CLUSTER) {
            throw new DataServiceException("Trying to create cluster arc without source or target being a cluster!");
        }
        
        data = new DataClusterArc(source.getDataElement(), target.getDataElement());
        shape = dataService.createConnection(source, target, data);
        
        if (relatedArc.getDataElement().getElementType() == Element.Type.CLUSTERARC) {
            
            for (IGraphArc arcRel : ((DataClusterArc) relatedArc.getDataElement()).getStoredArcs()) {
                data.getStoredArcs().add(arcRel);
                arcRel.getDataElement().getShapes().clear();
                arcRel.getDataElement().getShapes().add(shape);
            }
            relatedArc.getDataElement().getShapes().clear();
                    
        } else {
            
            data.getStoredArcs().add(relatedArc);
            relatedArc.getDataElement().getShapes().clear();
            relatedArc.getDataElement().getShapes().add(shape);
            
        }
        return shape;
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

//        for (IGraphArc arc : cluster.getDataElement().getArcsFromOutside()) {
//
//            arcData = arc.getDataElement();
//            clusterChild = null;
//
//            if (graphParent.contains(arc.getSource())) { // should be implicit
//                arcData.getShapes().clear();
//                arcData.getShapes().add(arc);
//                graphParent.add(arc);
//                
//            } else {
//                System.out.println("Source ist NICHT im parent graph layer! -> Arc: " + arc.getId() + " | " + arcData.getId());
//                clusterArc = (IGraphArc) arcData.getShapes().iterator().next();
//                System.out.println("Source ist ersetzt durch Cluster! -> Node: " + clusterArc.getSource().getId() + " | " + clusterArc.getSource().getDataElement().getId());
//
//                System.out.println("Target Cluster wird entfernt - finde embedded Cluster der Node enthält!");
//                for (IGravisCluster embeddedCluster : graph.getClusters()) {
//                    if (graphContainsNode(embeddedCluster.getGraph(), arc.getSource())) {
//                        clusterChild = (IGraphCluster) embeddedCluster;
//                        break;
//                    }
//                }
//                if (clusterChild == null) {
//                    throw new DataServiceException("Clustered node is nowhere to be found! Data integritiy breached!");
//                } else {
//                    System.out.println("Embedded Cluster gefunden! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());
//                }
//                
//                // create cluster arc and assign as shape to data element
//                arcClusterData = new DataClusterArc(clusterChild.getDataElement(), arc.getTarget().getDataElement());
//                clusterArc = dataService.createConnection(clusterChild, arc.getTarget(), arcClusterData); // create arc to obtain correct ID
//
//                if (!graphParent.contains(clusterArc)) {
//                    graphParent.add(clusterArc);
//                    dataService.styleElement(clusterArc);
//                } else {
//                    clusterArc = (IGraphArc) graphParent.getConnection(clusterArc.getId());
//                }
//                System.out.println("Erstelle connection von Cluster zu Target! -> " + clusterArc.getId() + " | " + clusterArc.getDataElement().getId());
//
//                arcClusterData = (DataClusterArc) clusterArc.getDataElement();
//                arcClusterData.getShapes().add(clusterArc);
//
//                // add reference to cluster arc
//                arcData.getShapes().clear();
//                arcData.getShapes().add(clusterArc);
//            }
//
//            // check if node is in scene. if not -> find cluster that contains node
//            // if source outside -> cluster.getDataElement().getArcsFromOutside().add arc
//            // else target outside -> cluster.getDataElement().getArcsToOutside().add arc
//        }
//
//        for (IGraphArc arc : cluster.getDataElement().getArcsToOutside()) {
//
//            arcData = arc.getDataElement();
//            clusterChild = null;
//
//            if (graphParent.contains(arc.getTarget())) { // should be implicit
//                arcData.getShapes().clear();
//                arcData.getShapes().add(arc);
//                graphParent.add(arc);
//            } else {
//                System.out.println("Target ist NICHT im parent graph layer! -> Arc: " + arc.getId() + " | " + arcData.getId());
//                clusterArc = (IGraphArc) arcData.getShapes().iterator().next();
//                System.out.println("Source ist ersetzt durch Cluster! -> Node: " + clusterArc.getTarget().getId() + " | " + clusterArc.getTarget().getDataElement().getId());
//                
//                System.out.println("Target Cluster wird entfernt - finde embedded Cluster der Node enthält!");
//                for (IGravisCluster embeddedCluster : graph.getClusters()) {
//                    if (graphContainsNode(embeddedCluster.getGraph(), arc.getTarget())) {
//                        clusterChild = (IGraphCluster) embeddedCluster;
//                        break;
//                    }
//                }
//                if (clusterChild == null) {
//                    throw new DataServiceException("Clustered node is nowhere to be found! Data integritiy breached!");
//                } else {
//                    System.out.println("Embedded Cluster gefunden! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());
//                }
//                System.out.println("Embedded Cluster gefunden! -> Node: " + clusterChild.getId() + " | " + clusterChild.getDataElement().getId());
//
//                // create cluster arc and assign as shape to data element
//                // create cluster arc and assign as shape to data element
//                arcClusterData = new DataClusterArc(arc.getSource().getDataElement(), clusterChild.getDataElement());
//                clusterArc = dataService.createConnection(arc.getSource(), clusterChild, arcClusterData); // create arc to obtain correct ID
//
//                if (!graphParent.contains(clusterArc)) {
//                    graphParent.add(clusterArc);
//                    dataService.styleElement(clusterArc);
//                } else {
//                    clusterArc = (IGraphArc) graphParent.getConnection(clusterArc.getId());
//                }
//                System.out.println("Erstelle connection von Source zu Cluster! -> " + clusterArc.getId() + " | " + clusterArc.getDataElement().getId());
//
//                arcClusterData = (DataClusterArc) clusterArc.getDataElement();
//                arcClusterData.getShapes().add(clusterArc);
//
//                // add reference to cluster arc
//                arcData.getShapes().clear();
//                arcData.getShapes().add(clusterArc);
//            }
//        }

        // remove cluster from parent graph and reassign hierarchy
        graphParent.remove(cluster);
        graphParent.getChildGraphs().remove(graph);
        while (!graph.getChildGraphs().isEmpty()) {
            graph.getChildGraphs().get(0).setParentGraph(graphParent);
        }

        return cluster;
    }
    
    private boolean graphContainsNode(Graph graph, IGraphNode node) {
        if (graph.contains(node)) {
            return true;
        } else {
            if (graph.getClusters().stream()
                    .anyMatch(cluster -> graphContainsNode(cluster.getGraph(), node))) {
                return true;
            }
        }
        return false;
    }
}
