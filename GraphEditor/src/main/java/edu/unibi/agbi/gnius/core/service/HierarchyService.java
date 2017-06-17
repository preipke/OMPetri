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
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCurve;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
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
import java.util.Optional;
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
        dataCluster.getStoredNodes().addAll(nodes);
        
        for (IGraphNode node : nodes) {
            for (IGravisConnection connection : node.getConnections()) {
                arc = (IGraphArc) connection;
                if (!nodes.contains(arc.getTarget())) { // target OUTSIDE
                    addClusterArc(cluster, arc.getTarget(), arc);
                } else if (!nodes.contains(arc.getSource())) { // source OUTSIDE
                    addClusterArc(arc.getSource(), cluster, arc);
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
         * Move stored nodes and arcs. All elements inside the new cluster
         * have to be moved from the old graph to the new graph.
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
        for (IGraphArc clusterArc : dataCluster.getClusterArcs()) {
            if (!graph.contains(clusterArc)) {
                graph.add(clusterArc);
                dataService.styleElement(clusterArc);
//                addClusterArc(dataCluster, clusterArc);
            }
        }

        return cluster;
    }
    
    /**
     *
     * @param source      source node
     * @param target      target node
     * @param arcRelated  arc that will be stored in the cluster arc
     * @return
     * @throws DataServiceException 
     */
    private String addClusterArc(IGraphNode source, IGraphNode target, IGraphArc arcRelated) throws DataServiceException {
        
        final IGraphArc arcForward, arcBackwards;
        final DataClusterArc dataForward, dataBackwards;
        List<DataCluster> dataClusters = new ArrayList();
        Optional<IGraphArc> existingArc;
        IGraphArc tmp;
        
        if (source.getType() == GravisType.CLUSTER) {
            dataClusters.add((DataCluster) source.getDataElement());
        }
        if (target.getType() == GravisType.CLUSTER) {
            dataClusters.add((DataCluster) target.getDataElement());
        }
        if (dataClusters.isEmpty()) {
            throw new DataServiceException("Trying to create cluster arc without source or target being a cluster!");
        }
        
        String id = source.getId() + target.getId();

        /**
         * Check if connection already exists.
         */
        existingArc = dataClusters.get(0).getClusterArcs().stream()
                .filter(a -> a.getSource().equals(source) && a.getTarget().equals(target))
                .findFirst();
        
        if (existingArc.isPresent()) {
            
            arcForward = existingArc.get();
            dataForward = (DataClusterArc) arcForward.getDataElement();
            
        } else {

            dataForward = new DataClusterArc(source.getDataElement(), target.getDataElement());

            // Check if reverse connection already exists. If so, any related cluster should store it.
            existingArc = dataClusters.get(0).getClusterArcs().stream()
                    .filter(a -> a.getSource().equals(target) && a.getTarget().equals(source))
                    .findFirst();

            if (!existingArc.isPresent()) {

                arcForward = new GraphEdge(id, source, target, dataForward);

            } else {

                arcForward = new GraphCurve(id, source, target, dataForward);

                /**
                 * Convert the reversly connecting arc from edge to curve.
                 */
                tmp = existingArc.get();
                dataBackwards = (DataClusterArc) tmp.getDataElement();
                arcBackwards = new GraphCurve(tmp.getId(), target, source, dataBackwards);

                dataBackwards.getShapes().clear();
                dataBackwards.getShapes().add(arcBackwards);
                dataBackwards.getStoredArcs().forEach(storedArc -> {
                    storedArc.getDataElement().getShapes().clear();
                    storedArc.getDataElement().getShapes().add(arcBackwards);
                });

                dataClusters.forEach(cluster -> cluster.getClusterArcs().remove(tmp));
                dataClusters.forEach(cluster -> cluster.getClusterArcs().add(arcBackwards));
            }

            dataForward.getShapes().clear();
            dataForward.getShapes().add(arcForward);
            dataForward.getStoredArcs().forEach(storedArc -> {
                storedArc.getDataElement().getShapes().clear();
                storedArc.getDataElement().getShapes().add(arcForward);
            });

            dataClusters.forEach(cluster -> cluster.getClusterArcs().add(arcForward));
        }
        
        /**
         * Check if the related arc is also a cluster arc.
         */
        if (arcRelated.getDataElement().getElementType() == Element.Type.CLUSTERARC) {
            /**
             * Move references for all stored arcs to the new cluster arc. The
             * currently related arc will be removed.
             */
            DataClusterArc dca = (DataClusterArc) arcRelated.getDataElement();
            for (IGraphArc arcRel : dca.getStoredArcs()) {
                dataForward.getStoredArcs().add(arcRel);
                arcRel.getDataElement().getShapes().clear();
                arcRel.getDataElement().getShapes().add(arcForward);
            }
            dca.getShapes().clear();
            dca.getStoredArcs().clear();

            DataCluster dc;
            if (arcRelated.getSource().getType() == GravisType.CLUSTER) {
                dc = (DataCluster) arcRelated.getSource().getDataElement();
                dc.getClusterArcs().remove(arcRelated);
            }
            if (arcRelated.getTarget().getType() == GravisType.CLUSTER) {
                dc = (DataCluster) arcRelated.getTarget().getDataElement();
                dc.getClusterArcs().remove(arcRelated);
            }
        } else {
            dataForward.getStoredArcs().add(arcRelated);
            arcRelated.getDataElement().getShapes().clear();
            arcRelated.getDataElement().getShapes().add(arcForward);
        }
        
        return null;
    }
    
    private void removeClusterArc(Graph graph, IGraphArc arc) {
        // check for double linked connection
        // if so, recreate shapes
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

        DataArc dataArc;
        DataCluster dataCluster;
        DataClusterArc dataClusterArc;
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
        
        dataCluster = cluster.getDataElement();
        
        for (IGraphArc arc : dataCluster.getClusterArcs()) {
            
        }
        
        for (IGraphArc arc : dataCluster.getStoredArcs()) {
            
        }

//        for (IGraphArc arc : cluster.getDataElement().getArcsFromOutside()) {
//
//            dataArc = arc.getDataElement();
//            clusterChild = null;
//
//            if (graphParent.contains(arc.getSource())) { // should be implicit
//                dataArc.getShapes().clear();
//                dataArc.getShapes().add(arc);
//                graphParent.add(arc);
//                
//            } else {
//                System.out.println("Source ist NICHT im parent graph layer! -> Arc: " + arc.getId() + " | " + dataArc.getId());
//                clusterArc = (IGraphArc) dataArc.getShapes().iterator().next();
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
//                dataClusterArc = new DataClusterArc(clusterChild.getDataElement(), arc.getTarget().getDataElement());
//                clusterArc = dataService.createConnection(clusterChild, arc.getTarget(), dataClusterArc); // create arc to obtain correct ID
//
//                if (!graphParent.contains(clusterArc)) {
//                    graphParent.add(clusterArc);
//                    dataService.styleElement(clusterArc);
//                } else {
//                    clusterArc = (IGraphArc) graphParent.getConnection(clusterArc.getId());
//                }
//                System.out.println("Erstelle connection von Cluster zu Target! -> " + clusterArc.getId() + " | " + clusterArc.getDataElement().getId());
//
//                dataClusterArc = (DataClusterArc) clusterArc.getDataElement();
//                dataClusterArc.getShapes().add(clusterArc);
//
//                // add reference to cluster arc
//                dataArc.getShapes().clear();
//                dataArc.getShapes().add(clusterArc);
//            }
//
//            // check if node is in scene. if not -> find cluster that contains node
//            // if source outside -> cluster.getDataElement().getArcsFromOutside().add arc
//            // else target outside -> cluster.getDataElement().getArcsToOutside().add arc
//        }
//
//        for (IGraphArc arc : cluster.getDataElement().getArcsToOutside()) {
//
//            dataArc = arc.getDataElement();
//            clusterChild = null;
//
//            if (graphParent.contains(arc.getTarget())) { // should be implicit
//                dataArc.getShapes().clear();
//                dataArc.getShapes().add(arc);
//                graphParent.add(arc);
//            } else {
//                System.out.println("Target ist NICHT im parent graph layer! -> Arc: " + arc.getId() + " | " + dataArc.getId());
//                clusterArc = (IGraphArc) dataArc.getShapes().iterator().next();
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
//                dataClusterArc = new DataClusterArc(arc.getSource().getDataElement(), clusterChild.getDataElement());
//                clusterArc = dataService.createConnection(arc.getSource(), clusterChild, dataClusterArc); // create arc to obtain correct ID
//
//                if (!graphParent.contains(clusterArc)) {
//                    graphParent.add(clusterArc);
//                    dataService.styleElement(clusterArc);
//                } else {
//                    clusterArc = (IGraphArc) graphParent.getConnection(clusterArc.getId());
//                }
//                System.out.println("Erstelle connection von Source zu Cluster! -> " + clusterArc.getId() + " | " + clusterArc.getDataElement().getId());
//
//                dataClusterArc = (DataClusterArc) clusterArc.getDataElement();
//                dataClusterArc.getShapes().add(clusterArc);
//
//                // add reference to cluster arc
//                dataArc.getShapes().clear();
//                dataArc.getShapes().add(clusterArc);
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
    
//    private IGraphArc createClusterArc(IGraphNode source, IGraphNode target, IGraphArc arcRelated) throws DataServiceException {
//        
//        DataClusterArc data;
//        IGraphArc shape;
//        String id;
//        
//        if (source.getType() != GravisType.CLUSTER && target.getType() != GravisType.CLUSTER) {
//            throw new DataServiceException("Trying to create cluster arc without source or target being a cluster!");
//        }
//        
//        id = source.getId() + target.getId();
//        data = new DataClusterArc(source.getDataElement(), target.getDataElement());
//        shape = new GraphEdge(id, source, target, data);
//        
//        if (arcRelated.getDataElement().getElementType() == Element.Type.CLUSTERARC) {
//            /**
//             * Move references for all stored arcs to the new cluster arc. The
//             * currently related arc will be removed.
//             */
//            for (IGraphArc arcRel : ((DataClusterArc) arcRelated.getDataElement()).getStoredArcs()) {
//                data.getStoredArcs().add(arcRel);
//                arcRel.getDataElement().getShapes().clear();
//                arcRel.getDataElement().getShapes().add(shape);
//            }
////            arcRelated.getDataElement().getShapes().clear();
////            ((DataClusterArc) arcRelated.getDataElement()).getStoredArcs().clear();
//        } else {
//            data.getStoredArcs().add(arcRelated);
//            arcRelated.getDataElement().getShapes().clear();
//            arcRelated.getDataElement().getShapes().add(shape);
//        }
//        
//        return shape;
//    }
//    
//    private void addClusterArc(Graph graph, DataCluster cluster, IGraphArc arc) throws DataServiceException {
//
//        if (arc.getDataElement().getElementType() == Element.Type.CLUSTERARC) {
//
//            final IGraphArc arcReverse, arcDirect;
//            final DataClusterArc dataReverse, dataDirect;
//            IGraphNode source = arc.getSource();
//            IGraphNode target = arc.getTarget();
//
//            /**
//             * If target has source as child, double linked arc will be
//             * established. Reshape arcs.
//             */
//            if (target.getChildren().contains(source)) {
//                
//                // convert the directly connecting arc
//                dataDirect = (DataClusterArc) arc.getDataElement();
//                arcDirect = new GraphCurve(arc.getId(), source, target, dataDirect);
//                
//                dataDirect.getShapes().clear();
//                dataDirect.getShapes().add(arcDirect);
//                dataDirect.getStoredArcs().forEach(storedArc -> {
//                    storedArc.getDataElement().getShapes().clear();
//                    storedArc.getDataElement().getShapes().add(arcDirect);
//                });
//                
//                graph.add(arcDirect);
//                dataService.styleElement(arcDirect);
//
//                // convert the reversly connecting arc
//                arc = (IGraphArc) target.getConnections().stream()
//                        .filter(connection -> connection.getTarget().equals(source))
//                        .findFirst().get();
//                dataReverse = (DataClusterArc) arc.getDataElement();
//                arcReverse = new GraphCurve(arc.getId(), target, source, dataReverse);
//
//                dataReverse.getShapes().clear();
//                dataReverse.getShapes().add(arcReverse);
//                dataReverse.getStoredArcs().forEach(storedArc -> {
//                    storedArc.getDataElement().getShapes().clear();
//                    storedArc.getDataElement().getShapes().add(arcReverse);
//                });
//                
//                graph.remove(arc);
//                graph.add(arcReverse);
//                dataService.styleElement(arcReverse);
//                
//            } else {
//
//                graph.add(arc);
//                dataService.styleElement(arc);
//            }
//
//        } else {
//
//            graph.add(arc);
//            dataService.styleElement(arc);
//        }
//    }
}
