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

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);

        Assert.assertEquals(placeCount, places.size());
        Assert.assertEquals(transitionCount, transitions.size());

        Assert.assertEquals(
                places.size() + transitions.size(),
                dataService.getActiveModel().getNodeIds().size(),
                dataService.getActiveGraph().getNodes().size());
        Assert.assertEquals(
                places.size(),
                dataService.getActiveModel().getPlaces().size());
        Assert.assertEquals(
                transitions.size(),
                dataService.getActiveModel().getTransitions().size());
        Assert.assertEquals(
                0,
                dataService.getActiveModel().getArcs().size(),
                dataService.getActiveGraph().getConnections().size());
    }

    @Test
    public void ConnectAndValidateNodes() throws DataServiceException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        
        ConnectNodes(places, transitions);

        Assert.assertEquals(
                places.size() * transitions.size() * 2,
                dataService.getActiveModel().getArcs().size(),
                dataService.getActiveGraph().getConnections().size());
        
        IGraphArc arc;
        for (IGravisConnection connection : dataService.getActiveGraph().getConnections()) {
            arc = (IGraphArc) connection;
            Assert.assertEquals(true, arc.getSource().getConnections().contains(arc));
            Assert.assertEquals(true, arc.getTarget().getConnections().contains(arc));
            Assert.assertEquals(true, arc.getDataElement().getSource().getArcsOut().contains(arc.getDataElement()));
            Assert.assertEquals(true, arc.getDataElement().getTarget().getArcsIn().contains(arc.getDataElement()));
        }
        
        for (IGraphNode place : places) {
            
            for (IGraphNode transition : transitions) {
                place.getChildren().contains(transition);
                place.getParents().contains(transition);
            }
            
            Assert.assertEquals(
                    place.getConnections().size() / 2,
                    (place.getDataElement()).getArcsIn().size(),
                    transitions.size());
            Assert.assertEquals(
                    place.getConnections().size() / 2,
                    (place.getDataElement()).getArcsOut().size(),
                    transitions.size());
        }
        
        for (IGraphNode transition : transitions) {
            
            for (IGraphNode place : places) {
                transition.getChildren().contains(place);
                transition.getParents().contains(place);
            }
            
            Assert.assertEquals(
                    transition.getConnections().size() / 2,
                    (transition.getDataElement()).getArcsIn().size(),
                    places.size());
            Assert.assertEquals(
                    transition.getConnections().size() / 2,
                    (transition.getDataElement()).getArcsOut().size(),
                    places.size());
        }
    }

    @Test
    public void RemoveNodesAndValidate() throws DataServiceException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        ConnectNodes(places, transitions);

        IGraphNode node;
        IDataNode nodeData;
        
        while (!dataService.getActiveGraph().getNodes().isEmpty()) {

            node = (IGraphNode) dataService.getActiveGraph()
                    .getNodes()
                    .get(getRandomIndex(dataService.getActiveGraph().getNodes()));
            nodeData = node.getDataElement();
            RemoveNode(node);

            Assert.assertEquals(false, dataService.getActiveGraph().contains(node));
            Assert.assertEquals(false, dataService.getActiveModel().getPlaces().contains(nodeData));
            Assert.assertEquals(false, dataService.getActiveModel().getTransitions().contains(nodeData));
            Assert.assertEquals(false, dataService.getActiveModel().contains(nodeData.getId()));
            
            // removed node can neither be parent nor child of any other node
            for (IGravisNode parent : node.getParents()) {
                Assert.assertEquals(false, parent.getChildren().contains(node));
            }
            for (IGravisNode child : node.getChildren()) {
                Assert.assertEquals(false, child.getParents().contains(node));
            }

            // no connection to the removed node is to be found!
            List<IGravisConnection> connections = dataService.getActiveGraph().getConnections();
            for (IGravisConnection connection : connections) {
                Assert.assertNotEquals(node, connection.getSource());
                Assert.assertNotEquals(node, connection.getTarget());
            }
            Collection<Arc> arcs = dataService.getActiveModel().getArcs();
            for (Arc arc : arcs) {
                Assert.assertNotEquals(nodeData, arc.getSource());
                Assert.assertNotEquals(nodeData, arc.getTarget());
            }
        }
    }

    @Test
    public void RemoveConnectionsAndValidate() throws DataServiceException {

        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        
        ConnectNodes(places, transitions);
        
        IGraphArc arc;
        DataArc arcData;
        
        while (!dataService.getActiveGraph().getConnections().isEmpty()) {
            
            arc = (IGraphArc) dataService.getActiveGraph()
                    .getConnections()
                    .remove(getRandomIndex(dataService.getActiveGraph().getConnections()));
            arcData = arc.getDataElement();
            RemoveArc(arc);

            Assert.assertEquals(false, dataService.getActiveGraph().contains(arc));
            Assert.assertEquals(false, dataService.getActiveModel().getArcs().contains(arcData));
            
            Assert.assertEquals(false, arc.getSource().getConnections().contains(arc));
            Assert.assertEquals(false, arc.getTarget().getConnections().contains(arc));
            
            Assert.assertEquals(false, arcData.getSource().getArcsOut().contains(arcData));
            Assert.assertEquals(false, arcData.getTarget().getArcsIn().contains(arcData));
        }
    }
    
    @Test
    public void ClusteringNodes() throws DataServiceException {
        
        List<IGravisNode> nodesAfterClustering, nodesBeforeClustering;
        List<IGravisConnection> connectionsAfterCluster, connectionsBeforeCluster;
        
        Collection<Arc> arcsAfterClustering, arcsBeforeClustering;
        Collection<Place> placesAfterClustering, placesBeforeClustering;
        Collection<Transition> transitionsAfterClustering, transitionsBeforeClustering ;
        
        List<IGraphNode> places = CreatePlaces(placeCount);
        List<IGraphNode> transitions = CreateTransitions(transitionCount);
        ConnectNodes(places, transitions);
        
        nodesBeforeClustering = dataService.getActiveGraph().getNodes();
        connectionsBeforeCluster = dataService.getActiveGraph().getConnections();
        
        arcsBeforeClustering = dataService.getActiveModel().getArcs();
        placesBeforeClustering = dataService.getActiveModel().getPlaces();
        transitionsBeforeClustering = dataService.getActiveModel().getTransitions();
        
        /**
         * Creating cluster.
         */
        GraphCluster cluster = ClusterNodes(places, transitions, nodesInCluster);
        
        nodesAfterClustering = dataService.getActiveGraph().getNodes();
        connectionsAfterCluster = dataService.getActiveGraph().getConnections();
        
        arcsAfterClustering = dataService.getActiveModel().getArcs();
        placesAfterClustering = dataService.getActiveModel().getPlaces();
        transitionsAfterClustering = dataService.getActiveModel().getTransitions();
        
        Assert.assertEquals(dataService.getActiveGraph().getNodes().size() - 1 + cluster.getDataElement().getClusteredNodes().size(), nodesBeforeClustering.size());
        Assert.assertNotEquals(nodesBeforeClustering, nodesAfterClustering);
        Assert.assertNotEquals(connectionsBeforeCluster, connectionsAfterCluster);
        
        // data should not have changed in the progress
        Assert.assertEquals(arcsBeforeClustering, arcsAfterClustering);
        Assert.assertEquals(placesBeforeClustering, placesAfterClustering);
        Assert.assertEquals(transitionsBeforeClustering, transitionsAfterClustering);
        
        /**
         * Removing cluster.
         */
        RemoveCluster(cluster);
        
        nodesAfterClustering = dataService.getActiveGraph().getNodes();
        connectionsAfterCluster = dataService.getActiveGraph().getConnections();
        
        arcsAfterClustering = dataService.getActiveModel().getArcs();
        placesAfterClustering = dataService.getActiveModel().getPlaces();
        transitionsAfterClustering = dataService.getActiveModel().getTransitions();
        
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
        Assert.assertEquals(arcsBeforeClustering, arcsAfterClustering);
        Assert.assertEquals(placesBeforeClustering, placesAfterClustering);
        Assert.assertEquals(transitionsBeforeClustering, transitionsAfterClustering);
    }
}
