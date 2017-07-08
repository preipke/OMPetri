/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.service;

import edu.unibi.agbi.gnius.business.controller.editor.graph.HierarchyController;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.IGravisCluster;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.ArrayList;
import java.util.Collection;
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
        List<IGravisConnection> nodeConnections;
        List<IGraphArc> clusterArcs, arcs;

        Graph graph;
        Graph graphChild;
        
        nodes.addAll(clusters);

        /**
         * Create cluster data and shape.
         */
        dataCluster = new DataCluster(dataService.getClusterId(dataService.getDao()));
        cluster = new GraphCluster(dataCluster.getId(), dataCluster);
        clusterArcs = new ArrayList();
        arcs = new ArrayList();

        /**
         * Reassign graph hierarchy.
         */
        graph = dataService.getGraph();
        graphChild = dataCluster.getGraph();
        graphChild.nameProperty().set(dataCluster.getId());
        graphChild.setParentGraph(graph);
        clusters.forEach(c -> c.getGraph().setParentGraph(graphChild));

        /**
         * Add and style the cluster node.
         */
        graph.add(cluster);
        dataService.styleElement(cluster);

        Point2D pos = calculator.getCenterN(nodes);
        if (dataService.isGridEnabled()) {
            pos = calculator.getPositionInGrid(pos, dataService.getGraph());
        }
        cluster.setTranslateX(pos.getX() - cluster.getCenterOffsetX());
        cluster.setTranslateY(pos.getY() - cluster.getCenterOffsetY());

        /**
         * Move nodes and determine wether node is connected to nodes in or 
         * outside of the cluster. All arcs connecting to nodes outside the 
         * cluster will be cluster arcs.
         */
        for (IGraphNode node : nodes) {
            
            nodeConnections = new ArrayList();
            nodeConnections.addAll(node.getConnections());
            
            graph.remove(node);
            graphChild.add(node);
            
            for (IGravisConnection connection : nodeConnections) {
                arc = (IGraphArc) connection;
                
                if (!nodes.contains(arc.getTarget())) { // target OUTSIDE
                    addClusterArcs(clusterArcs, cluster, arc.getTarget(), arc);
                } else if (!nodes.contains(arc.getSource())) { // source OUTSIDE
                    addClusterArcs(clusterArcs, arc.getSource(), cluster, arc);
                } else {
                    arcs.add(arc);
                }
            }
        }
        
        for (IGraphArc conn : arcs) {
            graphChild.add(conn);
        }
        
        for (IGraphArc conn : clusterArcs) {
            graph.add(conn);
            dataService.styleElement(conn);
        }

        return cluster;
    }

    /**
     * Removes the given cluster and un-groups all elements stored inside.
     *
     * @param cluster
     * @return
     */
    private void remove(IGraphCluster cluster) throws DataServiceException {

        Graph graphChild = cluster.getGraph();
        Graph graph = graphChild.getParentGraph();

        DataClusterArc dca;
        IGraphNode source = null, target = null;
        List<IGravisConnection> clusterConnections;
        Set<IGraphArc> clusterArcs, arcs;

        /**
         * Add all nodes and connections from the cluster child graph.
         */
        Point2D pos = calculator.getCenter(graphChild.getNodes());
        if (dataService.isGridEnabled()) {
            pos = calculator.getPositionInGrid(pos, dataService.getGraph());
        }
        double translateX = cluster.getShape().getTranslateX() + cluster.getCenterOffsetX() - pos.getX();
        double translateY = cluster.getShape().getTranslateY() + cluster.getCenterOffsetY() - pos.getY();

        for (IGravisNode n : graphChild.getNodes()) {
            graph.add(n);
            n.translateXProperty().set(n.translateXProperty().get() + translateX);
            n.translateYProperty().set(n.translateYProperty().get() + translateY);
        }
        for (IGravisConnection c : graphChild.getConnections()) {
            graph.add(c);
        }
        graphChild.clear();
        
        /**
         * Create copy of cluster arcs due to modification of original list.
         */
        clusterConnections = new ArrayList();
        clusterConnections.addAll(cluster.getConnections());
        
        /**
         * Remove cluster and reassign graph hierarchy.
         */
        graph.remove(cluster);
        graph.getChildGraphs().remove(graphChild);
        while (!graphChild.getChildGraphs().isEmpty()) {
            graphChild.getChildGraphs().get(0).setParentGraph(graph);
        }
        
        /**
         * Create new cluster arcs.
         */
        clusterArcs = new HashSet();
        arcs = new HashSet();
        
        for (IGravisConnection clusterArc : clusterConnections) {
            
            boolean isSourceRemaining;
            
            if (clusterArc.getSource() == cluster) {
                target = (IGraphNode) clusterArc.getTarget();
                isSourceRemaining = false;
            } else {
                source = (IGraphNode) clusterArc.getSource();
                isSourceRemaining = true;
            }
            
            /**
             * Check if stored arc nodes are present in scene.
             * If so, add arc. If not, create new cluster arc.
             */
            dca = (DataClusterArc) ((IGraphArc) clusterArc).getDataElement();
            
            for (IGraphArc storedArc : dca.getStoredArcs().values()) {
                
                if (!graph.contains(storedArc.getSource()) || !graph.contains(storedArc.getTarget())) {
                    if (isSourceRemaining) {
                        if (graph.contains(storedArc.getTarget())) {
                            target = storedArc.getTarget();
                        } else {
                            target = getClusterContainingNode(graph, storedArc.getTarget());
                        }
                    } else {
                        if (graph.contains(storedArc.getSource())) {
                            source = storedArc.getSource();
                        } else {
                            source = getClusterContainingNode(graph, storedArc.getSource());
                        }
                    }
                    addClusterArcs(clusterArcs, source, target, storedArc);
                } else {
                    arcs.add(storedArc);
                    storedArc.getDataElement().getShapes().clear();
                    storedArc.getDataElement().getShapes().add(storedArc);
                }
            }
        }
        
        for (IGraphArc a : arcs) {
            graph.add(a);
            dataService.styleElement(a);
        }
        
        for (IGraphArc a : clusterArcs) {
            graph.add(a);
            dataService.styleElement(a);
        }
    }
    
    private void addClusterArcs(Collection<IGraphArc> arcs, IGraphNode source, IGraphNode target, IGraphArc arcRelated) throws DataServiceException {
        
        final IGraphArc arcForward, arcBackwards;
        final DataClusterArc dataForward, dataBackwards, dca;
        Optional<IGraphArc> existingArc;
        IGraphArc arc;
        
        if (source.getType() != GravisType.CLUSTER && target.getType() != GravisType.CLUSTER) {
            throw new DataServiceException("Trying to create cluster arc without source or target being a cluster!");
        }
        
        String idShape = dataService.getConnectionId(source, target);
        String idData = dataService.getArcId(source.getDataElement(), target.getDataElement());

        /**
         * Check if connection already exists.
         */
        existingArc = arcs.stream()
                .filter(a -> a.getSource().equals(source) && a.getTarget().equals(target))
                .findFirst();
        
        if (existingArc.isPresent()) {
            
            arcForward = existingArc.get();
            dataForward = (DataClusterArc) arcForward.getDataElement();
            
        } else {

            dataForward = new DataClusterArc(idData, source.getDataElement(), target.getDataElement());
            arcForward = new GraphArc(idShape, source, target, dataForward);

            arcs.add(arcForward);
        }
        
        /**
         * Check if the related arc is also a cluster arc. Add references for 
         * all stored arcs to the new cluster arc.
         */
        if (arcRelated.getDataElement().getElementType() == Element.Type.CLUSTERARC) {
            dca = (DataClusterArc) arcRelated.getDataElement();
            dca.getStoredArcs().values().forEach(storedArc -> {
                dataForward.getStoredArcs().put(storedArc.getId(), storedArc);
            });
            dca.getStoredArcs().clear();
        } else {
            dataForward.getStoredArcs().put(arcRelated.getId(), arcRelated);
        }
        
        dataForward.getShapes().clear();
        dataForward.getShapes().add(arcForward);
        dataForward.getStoredArcs().values().forEach(storedArc -> {
            storedArc.getDataElement().getShapes().clear();
            storedArc.getDataElement().getShapes().add(arcForward);
        });
    }
    
    private IGraphCluster getClusterContainingNode(Graph graph, IGraphNode node) {
        for (IGravisCluster cluster : graph.getClusters()) {
            if (isClusterContainingNode(cluster, node)) {
                return (IGraphCluster) cluster;
            }
        }
        return null;
    }
    
    private boolean isClusterContainingNode(IGravisCluster cluster, IGraphNode node) {
        if (cluster.getGraph().contains(node)) {
            return true;
        } else {
            for (IGravisCluster subCluster : cluster.getGraph().getClusters()) {
                if (isClusterContainingNode(subCluster, node)) {
                    return true;
                }
            }
            return false;
        }
    }
}
