package service;

import main.TestFXBase;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
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
    int nodesInCluster = 5;
    IGraphArc tmpArc;
    IGraphNode tmpNode;

    @Test
    public void CreateNodes() throws DataServiceException {

//        List<IGraphNode> places = CreatePlaces(placeCount);
//        List<IGraphNode> transitions = CreateTransitions(transitionCount);
//
//        Assert.assertEquals(placeCount, places.size());
//        Assert.assertEquals(transitionCount, transitions.size());
//
//        Assert.assertEquals(
//                places.size() + transitions.size(),
//                dataDao.getNodeIds().size(),
//                graphDao.getNodes().size());
//        Assert.assertEquals(
//                places.size(),
//                dataDao.getPlaces().size());
//        Assert.assertEquals(
//                transitions.size(),
//                dataDao.getTransitions().size());
//        Assert.assertEquals(
//                0,
//                dataDao.getArcs().size(),
//                graphDao.getConnections().size());
    }

    @Test
    public void ConnectAndValidateNodes() throws DataServiceException {

//        List<IGraphNode> places = CreatePlaces(placeCount);
//        List<IGraphNode> transitions = CreateTransitions(transitionCount);
//        
//        ConnectNodes(places, transitions);
//
//        Assert.assertEquals(
//                places.size() * transitions.size() * 2,
//                dataDao.getArcs().size(),
//                graphDao.getConnections().size());
//        
//        IGraphArc arc;
//        for (IGravisConnection connection : graphDao.getConnections()) {
//            arc = (IGraphArc) connection;
//            Assert.assertEquals(true, arc.getSource().getConnections().contains(arc));
//            Assert.assertEquals(true, arc.getTarget().getConnections().contains(arc));
//            Assert.assertEquals(true, arc.getDataElement().getSource().getArcsOut().contains(arc.getDataElement()));
//            Assert.assertEquals(true, arc.getDataElement().getTarget().getArcsIn().contains(arc.getDataElement()));
//        }
//        
//        for (IGraphNode place : places) {
//            
//            for (IGraphNode transition : transitions) {
//                place.getChildren().contains(transition);
//                place.getParents().contains(transition);
//            }
//            
//            Assert.assertEquals(
//                    place.getConnections().size() / 2,
//                    (place.getDataElement()).getArcsIn().size(),
//                    transitions.size());
//            Assert.assertEquals(
//                    place.getConnections().size() / 2,
//                    (place.getDataElement()).getArcsOut().size(),
//                    transitions.size());
//        }
//        
//        for (IGraphNode transition : transitions) {
//            
//            for (IGraphNode place : places) {
//                transition.getChildren().contains(place);
//                transition.getParents().contains(place);
//            }
//            
//            Assert.assertEquals(
//                    transition.getConnections().size() / 2,
//                    (transition.getDataElement()).getArcsIn().size(),
//                    places.size());
//            Assert.assertEquals(
//                    transition.getConnections().size() / 2,
//                    (transition.getDataElement()).getArcsOut().size(),
//                    places.size());
//        }
    }

    @Test
    public void RemoveNodesAndValidate() throws DataServiceException {

//        List<IGraphNode> places = CreatePlaces(placeCount);
//        List<IGraphNode> transitions = CreateTransitions(transitionCount);
//        ConnectNodes(places, transitions);
//
//        IGraphNode node;
//        IDataNode nodeData;
//        
//        while (!graphDao.getNodes().isEmpty()) {
//
//            node = (IGraphNode) graphDao.getNodes().get(getRandomIndex(graphDao.getNodes()));
//            nodeData = node.getDataElement();
//            RemoveNode(node);
//
//            Assert.assertEquals(false, graphDao.contains(node));
//            Assert.assertEquals(false, dataDao.getPlaces().contains(nodeData));
//            Assert.assertEquals(false, dataDao.getTransitions().contains(nodeData));
//            Assert.assertEquals(false, dataDao.getNodeIds().contains(nodeData.getId()));
//            
//            // removed node can neither be parent nor child of any other node
//            for (IGravisNode parent : node.getParents()) {
//                Assert.assertEquals(false, parent.getChildren().contains(node));
//            }
//            for (IGravisNode child : node.getChildren()) {
//                Assert.assertEquals(false, child.getParents().contains(node));
//            }
//
//            // no connection to the removed node is to be found!
//            List<IGravisConnection> connections = graphDao.getConnections();
//            for (IGravisConnection connection : connections) {
//                Assert.assertNotEquals(node, connection.getSource());
//                Assert.assertNotEquals(node, connection.getTarget());
//            }
//            Collection<Arc> arcs = dataDao.getArcs();
//            for (Arc arc : arcs) {
//                Assert.assertNotEquals(nodeData, arc.getSource());
//                Assert.assertNotEquals(nodeData, arc.getTarget());
//            }
//        }
    }

    @Test
    public void RemoveConnectionsAndValidate() throws DataServiceException {

//        List<IGraphNode> places = CreatePlaces(placeCount);
//        List<IGraphNode> transitions = CreateTransitions(transitionCount);
//        
//        ConnectNodes(places, transitions);
//        
//        IGraphArc arc;
//        DataArc arcData;
//        
//        while (!graphDao.getConnections().isEmpty()) {
//            
//            arc = (IGraphArc) graphDao.getConnections().remove(getRandomIndex(graphDao.getConnections()));
//            arcData = arc.getDataElement();
//            RemoveArc(arc);
//
//            Assert.assertEquals(false, graphDao.contains(arc));
//            Assert.assertEquals(false, dataDao.getArcs().contains(arcData));
//            
//            Assert.assertEquals(false, arc.getSource().getConnections().contains(arc));
//            Assert.assertEquals(false, arc.getTarget().getConnections().contains(arc));
//            
//            Assert.assertEquals(false, arcData.getSource().getArcsOut().contains(arcData));
//            Assert.assertEquals(false, arcData.getTarget().getArcsIn().contains(arcData));
//        }
    }
    
    @Test
    public void ClusteringNodes() throws DataServiceException {
        
        List<IGravisNode> nodesAfterClustering, nodesBeforeClustering;
        List<IGravisConnection> connectionsAfterCluster, connectionsBeforeCluster;
        
        Collection<Arc> arcsAfterClustering, arcsBeforeClustering;
        Collection<Place> placesAfterClustering, placesBeforeClustering;
        Collection<Transition> transitionsAfterClustering, transitionsBeforeClustering ;
        
//        List<IGraphNode> places = CreatePlaces(placeCount);
//        List<IGraphNode> transitions = CreateTransitions(transitionCount);
//        ConnectNodes(places, transitions);
//        
//        nodesBeforeClustering = graphDao.getNodes();
//        connectionsBeforeCluster = graphDao.getConnections();
//        
//        arcsBeforeClustering = dataDao.getArcs();
//        placesBeforeClustering = dataDao.getPlaces();
//        transitionsBeforeClustering = dataDao.getTransitions();
//        
//        /**
//         * Creating cluster.
//         */
//        GraphCluster cluster = ClusterNodes(places, transitions, nodesInCluster);
//        
//        nodesAfterClustering = graphDao.getNodes();
//        connectionsAfterCluster = graphDao.getConnections();
//        
//        arcsAfterClustering = dataDao.getArcs();
//        placesAfterClustering = dataDao.getPlaces();
//        transitionsAfterClustering = dataDao.getTransitions();
//        
//        Assert.assertEquals(graphDao.getNodes().size() - 1 + cluster.getDataElement().getClusteredNodes().size(), nodesBeforeClustering.size());
//        Assert.assertNotEquals(nodesBeforeClustering, nodesAfterClustering);
//        Assert.assertNotEquals(connectionsBeforeCluster, connectionsAfterCluster);
//        
//        // data should not have changed in the progress
//        Assert.assertEquals(arcsBeforeClustering, arcsAfterClustering);
//        Assert.assertEquals(placesBeforeClustering, placesAfterClustering);
//        Assert.assertEquals(transitionsBeforeClustering, transitionsAfterClustering);
//        
//        /**
//         * Removing cluster.
//         */
//        RemoveCluster(cluster);
//        
//        nodesAfterClustering = graphDao.getNodes();
//        connectionsAfterCluster = graphDao.getConnections();
//        
//        arcsAfterClustering = dataDao.getArcs();
//        placesAfterClustering = dataDao.getPlaces();
//        transitionsAfterClustering = dataDao.getTransitions();
//        
//        // all nodes have to be found
//        Assert.assertEquals(nodesBeforeClustering.size(), nodesAfterClustering.size());
//        for (IGravisNode node : nodesBeforeClustering) {
//            Assert.assertTrue(nodesAfterClustering.contains(node));
//        }
//        
//        // all nodes connections have to be established
//        Assert.assertEquals(connectionsBeforeCluster.size(), connectionsAfterCluster.size());
//        for (IGravisConnection conn1 : connectionsBeforeCluster) {
//            
//            boolean found = false;
//            IGravisNode source1 = conn1.getSource();
//            IGravisNode target1 = conn1.getTarget();
//            
//            for (IGravisConnection conn2 : connectionsAfterCluster) {
//                IGravisNode source2 = conn2.getSource();
//                IGravisNode target2 = conn2.getTarget();
//                if (source1.equals(source2) && target1.equals(target2)) {
//                    if (found) {
//                        throw new AssertionError("The same connection has been found twice!");
//                    } else {
//                        found = true;
//                    }
//                }
//            }
//            Assert.assertTrue(found);
//        }
//        
//        // data should not have changed in the progress
//        Assert.assertEquals(arcsBeforeClustering, arcsAfterClustering);
//        Assert.assertEquals(placesBeforeClustering, placesAfterClustering);
//        Assert.assertEquals(transitionsBeforeClustering, transitionsAfterClustering);
    }
}
