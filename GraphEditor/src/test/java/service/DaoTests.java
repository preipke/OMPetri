package service;

import main.TestFXBase;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.DataException;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.petrinet.entity.impl.Arc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.entity.impl.Transition;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author PR
 */
public class DaoTests extends TestFXBase {

    int placeCount = 5;
    int transitionCount = 3;
    int nodesInCluster = 3;
    IGraphArc tmpArc;
    IGraphNode tmpNode;

    @Test
    public void CreateNodes() throws DataException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);

        Assert.assertEquals(placeCount, places.size());
        Assert.assertEquals(transitionCount, transitions.size());

        Assert.assertEquals(
                places.size() + transitions.size(),
                dataService.getModel().getNodeIds().size(),
                dataService.getGraph().getNodes().size());
        Assert.assertEquals(
                places.size(),
                dataService.getModel().getPlaces().size());
        Assert.assertEquals(
                transitions.size(),
                dataService.getModel().getTransitions().size());
        Assert.assertEquals(
                0,
                dataService.getModel().getArcs().size(),
                dataService.getGraph().getConnections().size());
    }

    @Test
    public void ConnectAndValidateNodes() throws DataException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        
        ConnectNodes(places, transitions);

        Assert.assertEquals(
                places.size() * transitions.size() * 2,
                dataService.getModel().getArcs().size(),
                dataService.getGraph().getConnections().size());
        
        IGraphArc arc;
        for (IGravisConnection connection : dataService.getGraph().getConnections()) {
            arc = (IGraphArc) connection;
            Assert.assertEquals(true, arc.getSource().getConnections().contains(arc));
            Assert.assertEquals(true, arc.getTarget().getConnections().contains(arc));
            Assert.assertEquals(true, arc.getData().getSource().getArcsOut().contains(arc.getData()));
            Assert.assertEquals(true, arc.getData().getTarget().getArcsIn().contains(arc.getData()));
        }
        
        for (IGraphNode place : places) {
            
            for (IGraphNode transition : transitions) {
                place.getChildren().contains(transition);
                place.getParents().contains(transition);
            }
            
            Assert.assertEquals(
                    place.getConnections().size() / 2,
                    (place.getData()).getArcsIn().size(),
                    transitions.size());
            Assert.assertEquals(
                    place.getConnections().size() / 2,
                    (place.getData()).getArcsOut().size(),
                    transitions.size());
        }
        
        for (IGraphNode transition : transitions) {
            
            for (IGraphNode place : places) {
                transition.getChildren().contains(place);
                transition.getParents().contains(place);
            }
            
            Assert.assertEquals(
                    transition.getConnections().size() / 2,
                    (transition.getData()).getArcsIn().size(),
                    places.size());
            Assert.assertEquals(
                    transition.getConnections().size() / 2,
                    (transition.getData()).getArcsOut().size(),
                    places.size());
        }
    }

    @Test
    public void RemoveNodesAndValidate() throws DataException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        ConnectNodes(places, transitions);

        List<IGravisNode> nodesCopy;
        IGraphNode node;
        IDataNode nodeData;
        
        while (!dataService.getGraph().getNodes().isEmpty()) {
            
            nodesCopy = copyNodes(dataService.getGraph().getNodes());

            node = (IGraphNode) nodesCopy.remove(getRandomIndex(nodesCopy));
            nodeData = node.getData();
            
            RemoveNode(node);

            Assert.assertEquals(false, dataService.getGraph().contains(node));
            Assert.assertEquals(false, dataService.getModel().getPlaces().contains(nodeData));
            Assert.assertEquals(false, dataService.getModel().getTransitions().contains(nodeData));
            Assert.assertEquals(false, dataService.getModel().contains(nodeData.getId()));
            
            // removed node can neither be parent nor child of any other node
            for (IGravisNode parent : node.getParents()) {
                Assert.assertEquals(false, parent.getChildren().contains(node));
            }
            for (IGravisNode child : node.getChildren()) {
                Assert.assertEquals(false, child.getParents().contains(node));
            }

            // no connection to the removed node is to be found!
            Collection<IGravisConnection> connections = dataService.getGraph().getConnections();
            for (IGravisConnection connection : connections) {
                Assert.assertNotEquals(node, connection.getSource());
                Assert.assertNotEquals(node, connection.getTarget());
            }
            Collection<Arc> arcs = dataService.getModel().getArcs();
            for (Arc arc : arcs) {
                Assert.assertNotEquals(nodeData, arc.getSource());
                Assert.assertNotEquals(nodeData, arc.getTarget());
            }
        }
    }

    @Test
    public void RemoveConnectionsAndValidate() throws DataException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        
        ConnectNodes(places, transitions);
        
        List<IGravisConnection> arcsCopy;
        IGraphArc arc;
        IDataArc arcData;
        
        while (!dataService.getGraph().getConnections().isEmpty()) {
            
            arcsCopy = copyConnections(dataService.getGraph().getConnections());
            
            arc = (IGraphArc) arcsCopy.remove(getRandomIndex(arcsCopy));
            arcData = arc.getData();
            
            RemoveArc(arc);

            Assert.assertEquals(false, dataService.getGraph().contains(arc));
            Assert.assertEquals(false, dataService.getModel().getArcs().contains(arcData));
            
            Assert.assertEquals(false, arc.getSource().getConnections().contains(arc));
            Assert.assertEquals(false, arc.getTarget().getConnections().contains(arc));
            
            Assert.assertEquals(false, arcData.getSource().getArcsOut().contains(arcData));
            Assert.assertEquals(false, arcData.getTarget().getArcsIn().contains(arcData));
        }
    }
    
    @Test
    public void ClusteringNodes() throws DataException {
        
        Collection<IGravisNode> nodesAfterClustering, nodesBeforeClustering;
        Collection<IGravisConnection> connectionsAfterCluster, connectionsBeforeCluster;
        
        Collection<Arc> arcsAfterClustering, arcsBeforeClustering;
        Collection<Place> placesAfterClustering, placesBeforeClustering;
        Collection<Transition> transitionsAfterClustering, transitionsBeforeClustering ;
        
        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        ConnectNodes(places, transitions);
        
        nodesBeforeClustering = copyNodes(dataService.getGraph().getNodes());
        connectionsBeforeCluster = copyConnections(dataService.getGraph().getConnections());
        
        arcsBeforeClustering = dataService.getModel().getArcsCopy();
        placesBeforeClustering = dataService.getModel().getPlacesCopy();
        transitionsBeforeClustering = dataService.getModel().getTransitionsCopy();
        
        /**
         * Creating cluster.
         */
        IGraphCluster cluster = ClusterNodes(places, transitions, nodesInCluster);
        
        nodesAfterClustering = dataService.getGraph().getNodes();
        connectionsAfterCluster = dataService.getGraph().getConnections();
        
        arcsAfterClustering = dataService.getModel().getArcs();
        placesAfterClustering = dataService.getModel().getPlaces();
        transitionsAfterClustering = dataService.getModel().getTransitions();
        
        Assert.assertNotEquals(nodesBeforeClustering, nodesAfterClustering);
        Assert.assertNotEquals(connectionsBeforeCluster, connectionsAfterCluster);
        
        // data should not have changed in the progress
        Assert.assertEquals(arcsBeforeClustering.size(), arcsAfterClustering.size());
        Assert.assertEquals(placesBeforeClustering.size(), placesAfterClustering.size());
        Assert.assertEquals(transitionsBeforeClustering.size(), transitionsAfterClustering.size());
        arcsAfterClustering.forEach(arc -> {
            Assert.assertTrue(arcsBeforeClustering.contains(arc));
        });
        placesAfterClustering.forEach(place -> {
            Assert.assertTrue(placesBeforeClustering.contains(place));
        });
        transitionsAfterClustering.forEach(transition -> {
            Assert.assertTrue(transitionsBeforeClustering.contains(transition));
        });
        
        /**
         * Ungrouping cluster.
         */
        UngroupCluster(cluster);
        
        nodesAfterClustering = dataService.getGraph().getNodes();
        connectionsAfterCluster = dataService.getGraph().getConnections();
        
        arcsAfterClustering = dataService.getModel().getArcs();
        placesAfterClustering = dataService.getModel().getPlaces();
        transitionsAfterClustering = dataService.getModel().getTransitions();
        
        // all nodes have to be found
        Assert.assertEquals(nodesBeforeClustering.size(), nodesAfterClustering.size());
        for (IGravisNode node : nodesBeforeClustering) {
            Assert.assertTrue(nodesAfterClustering.contains(node));
        }
        
        // all nodes connections have to be established
        Assert.assertEquals(connectionsBeforeCluster.size(), connectionsAfterCluster.size());
        for (IGravisConnection conn1 : connectionsBeforeCluster) {
            
            boolean found = false;
            IGravisNode source1 = conn1.getSource();
            IGravisNode target1 = conn1.getTarget();
            
            for (IGravisConnection conn2 : connectionsAfterCluster) {
                IGravisNode source2 = conn2.getSource();
                IGravisNode target2 = conn2.getTarget();
                if (source1.equals(source2) && target1.equals(target2)) {
                    if (found) {
                        throw new AssertionError("The same connection has been found twice!");
                    } else {
                        found = true;
                    }
                }
            }
            Assert.assertTrue(found);
        }
        
        // data should not have changed in the progress
        Assert.assertEquals(arcsBeforeClustering.size(), arcsAfterClustering.size());
        Assert.assertEquals(placesBeforeClustering.size(), placesAfterClustering.size());
        Assert.assertEquals(transitionsBeforeClustering.size(), transitionsAfterClustering.size());
        arcsAfterClustering.forEach(arc -> {
            Assert.assertTrue(arcsBeforeClustering.contains(arc));
        });
        placesAfterClustering.forEach(place -> {
            Assert.assertTrue(placesBeforeClustering.contains(place));
        });
        transitionsAfterClustering.forEach(transition -> {
            Assert.assertTrue(transitionsBeforeClustering.contains(transition));
        });
    }
}
