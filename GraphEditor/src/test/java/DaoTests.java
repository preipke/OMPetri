/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import edu.unibi.agbi.gnius.core.model.entity.data.IDataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author PR
 */
public class DaoTests extends TestFXBase {

    int placeCount = 5;
    int transitionCount = 3;
    IGraphArc tmpArc;
    IGraphNode tmpNode;

    @Test
    public void CreateNodes() throws DataGraphServiceException {

        List<IGraphNode> places = CreatePlaces();
        List<IGraphNode> transitions = CreateTransitions();

        Assert.assertEquals(placeCount, places.size());
        Assert.assertEquals(transitionCount, transitions.size());

        Assert.assertEquals(
                places.size() + transitions.size(),
                dataDao.getPlacesAndTransitions().size(),
                graphDao.getNodes().size());
        Assert.assertEquals(
                places.size(),
                dataDao.getPlaces().size());
        Assert.assertEquals(
                transitions.size(),
                dataDao.getTransitions().size());
        Assert.assertEquals(
                0,
                dataDao.getArcs().size(),
                graphDao.getConnections().size());
    }

    @Test
    public void ConnectAndValidateNodes() throws DataGraphServiceException {

        List<IGraphNode> places = CreatePlaces();
        List<IGraphNode> transitions = CreateTransitions();
        List<IGraphArc> arcs = CreateConnections(places, transitions);

        Assert.assertEquals(
                arcs.size(),
                graphDao.getConnections().size());
        Assert.assertEquals(
                arcs.size(),
                dataDao.getArcs().size());
        
        arcs = getConnections();
        
        for (IGraphArc arc : arcs) {
            Assert.assertEquals(true, arc.getSource().getGraphConnections().contains(arc));
            Assert.assertEquals(true, arc.getTarget().getGraphConnections().contains(arc));
            Assert.assertEquals(true, arc.getDataElement().getSource().getArcsOut().contains(arc.getDataElement()));
            Assert.assertEquals(true, arc.getDataElement().getTarget().getArcsIn().contains(arc.getDataElement()));
        }
        
        for (IGraphNode place : places) {
            
            for (IGraphNode transition : transitions) {
                place.getChildren().contains(transition);
                place.getParents().contains(transition);
            }
            
            Assert.assertEquals(
                    place.getGraphConnections().size() / 2,
                    place.getDataElement().getArcsIn().size(),
                    transitions.size());
            Assert.assertEquals(
                    place.getGraphConnections().size() / 2,
                    place.getDataElement().getArcsOut().size(),
                    transitions.size());
        }
        
        for (IGraphNode transition : transitions) {
            
            for (IGraphNode place : places) {
                transition.getChildren().contains(place);
                transition.getParents().contains(place);
            }
            
            Assert.assertEquals(
                    transition.getGraphConnections().size() / 2,
                    transition.getDataElement().getArcsIn().size(),
                    places.size());
            Assert.assertEquals(
                    transition.getGraphConnections().size() / 2,
                    transition.getDataElement().getArcsOut().size(),
                    places.size());
        }
    }

    @Test
    public void RemoveNodesAndValidate() throws DataGraphServiceException {

        CreateTransitions();
        CreatePlaces();
        
        List<IGraphNode> nodes = getNodes();

        IGraphNode removedGraphNode;
        IDataNode removedDataNode;
        
        while (!nodes.isEmpty()) {

            removedGraphNode = RemoveRandomNode(nodes);
            removedDataNode = removedGraphNode.getDataElement();

            Assert.assertEquals(false, graphDao.contains(removedGraphNode));
            Assert.assertEquals(false, dataDao.getPlaces().contains(removedDataNode));
            Assert.assertEquals(false, dataDao.getTransitions().contains(removedDataNode));
            Assert.assertEquals(false, dataDao.getPlacesAndTransitions().contains(removedDataNode));
            
            // removed node can neither be parent nor child of any other node
            for (IGravisNode node : removedGraphNode.getParents()) {
                Assert.assertEquals(false, node.getChildren().contains(removedGraphNode));
            }
            for (IGravisNode node : removedGraphNode.getChildren()) {
                Assert.assertEquals(false, node.getParents().contains(removedGraphNode));
            }

            // no connection to the removed node is to be found!
            List<IGravisConnection> connections = graphDao.getConnections();
            for (IGravisConnection connection : connections) {
                Assert.assertNotEquals(removedGraphNode, connection.getSource());
                Assert.assertNotEquals(removedGraphNode, connection.getTarget());
            }
            Collection<IArc> arcs = dataDao.getArcs();
            for (IArc arc : arcs) {
                Assert.assertNotEquals(removedDataNode, arc.getSource());
                Assert.assertNotEquals(removedDataNode, arc.getTarget());
            }
        }
    }

    @Test
    public void RemoveConnectionsAndValidate() throws DataGraphServiceException {

        List<IGraphNode> places = CreatePlaces();
        List<IGraphNode> transitions = CreateTransitions();
        List<IGraphArc> arcs = CreateConnections(places, transitions);
        
        IGraphArc removedArc;
        IDataArc removedDataArc;
        
        while (!arcs.isEmpty()) {
            
            removedArc = RemoveRandomArc(arcs);
            removedDataArc = removedArc.getDataElement();

            Assert.assertEquals(false, graphDao.contains(removedArc));
            Assert.assertEquals(false, dataDao.getArcs().contains(removedDataArc));
            
            Assert.assertEquals(false, removedArc.getSource().getGraphConnections().contains(removedArc));
            Assert.assertEquals(false, removedArc.getTarget().getGraphConnections().contains(removedArc));
            
            Assert.assertEquals(false, removedDataArc.getSource().getArcsOut().contains(removedDataArc));
            Assert.assertEquals(false, removedDataArc.getTarget().getArcsIn().contains(removedDataArc));
        }
    }

    private List<IGraphArc> CreateConnections(List<IGraphNode> places, List<IGraphNode> transitions) throws DataGraphServiceException {

        List<IGraphArc> arcs = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (IGraphNode place : places) {
                    for (IGraphNode transition : transitions) {
                        arcs.add(CreateArc(place, transition));
                        arcs.add(CreateArc(transition, place));
                    }
                }
            } catch (DataGraphServiceException ex) {
                System.out.println(ex.getMessage());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);

        return arcs;
    }

    private List<IGraphNode> CreatePlaces() {

        final List<IGraphNode> places = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < placeCount; i++) {
                    places.add(CreatePlace());
                }
            } catch (DataGraphServiceException ex) {
                System.out.println(ex.getMessage());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);

        return places;
    }

    private List<IGraphNode> CreateTransitions() {

        final List<IGraphNode> transitions = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < transitionCount; i++) {
                    transitions.add(CreateTransition());
                }
            } catch (DataGraphServiceException ex) {
                System.out.println(ex.getMessage());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);

        return transitions;
    }

    private IGraphNode CreatePlace() throws DataGraphServiceException {
        return dataGraphService.create(Element.Type.PLACE, Math.random() * 1000, Math.random() * 800);
    }

    private IGraphNode CreateTransition() throws DataGraphServiceException {
        return dataGraphService.create(Element.Type.TRANSITION, Math.random() * 1000, Math.random() * 800);
    }

    private IGraphArc CreateArc(IGraphNode source, IGraphNode target) throws DataGraphServiceException {
        return dataGraphService.connect(source, target, null);
    }
    
    private List<IGraphNode> getNodes() {
        List<IGravisNode> nodes = graphDao.getNodes();
        List<IGraphNode> nodesTmp = new ArrayList();
        for (IGravisNode node : nodes) {
            nodesTmp.add((IGraphNode) node);
        }
        return nodesTmp;
    }
    
    private List<IGraphArc> getConnections() {
        List<IGravisConnection> connections = graphDao.getConnections();
        List<IGraphArc> connectionsTmp = new ArrayList();
        for (IGravisConnection connection : connections) {
            connectionsTmp.add((IGraphArc) connection);
        }
        return connectionsTmp;
    }
    
    private IGraphArc RemoveRandomArc(List<IGraphArc> arcs) {
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            int index = (int) Math.floor(Math.random() * arcs.size());
            tmpArc = dataGraphService.removeShape(arcs.remove(index));
            isFinished.set(true);
        });
        waitForFxThread(isFinished);
        
        return tmpArc;
    }

    private IGraphNode RemoveRandomNode(List<IGraphNode> nodes) {
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            int index = (int) Math.floor(Math.random() * nodes.size());
            tmpNode = dataGraphService.removeShape(nodes.remove(index));
            isFinished.set(true);
        });
        waitForFxThread(isFinished);
        
        return tmpNode;
    }

    private void waitForFxThread(AtomicBoolean isFinished) {
        while (!isFinished.get()) {
            try {
                synchronized (this) {
                    System.out.println("Waiting...");
                    wait(50);
                }
            } catch (InterruptedException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
    }
}
