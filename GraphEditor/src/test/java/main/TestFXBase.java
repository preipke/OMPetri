package main;

import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.HierarchyService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.api.*;

/**
 *
 * @author PR
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=Main.class)
public class TestFXBase extends ApplicationTest {

    private final static boolean HEADLESS = true;
    
    @Autowired protected DataService dataService;
    @Autowired protected HierarchyService hierarchyService;
    @Autowired protected SelectionService selectionService;

    @BeforeClass
    public static void setupHeadlessMode() {
        if (HEADLESS) {
            System.setProperty("java.awt.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
        }
//        bundle = ResourceBundle.getBundle("Bundle");
    }

    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class); // verifies that MainApp is a JavaFX application (extends Application)
        dataService.setDao(dataService.createDao());
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    @After
    public void afterEachTest() throws TimeoutException {
        FxToolkit.hideStage();
        // release button strokes so they dont get stuck, resource management
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
    
    /**
     * Freezes the thread for the given amount of time (in milliseconds).
     * @param ms 
     */
    protected void freeze(int ms) {
        try {
            synchronized(this){
                wait(ms);
            }
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }

    protected List<IGravisConnection> copyConnections(Collection<IGravisConnection> connections) {
        List<IGravisConnection> listCopy = new ArrayList();
        for (IGravisConnection connection : connections) {
            listCopy.add(connection);
        }
        return listCopy;
    }

    protected List<IGravisNode> copyNodes(Collection<IGravisNode> nodes) {
        List<IGravisNode> listCopy = new ArrayList();
        for (IGravisNode node : nodes) {
            listCopy.add(node);
        }
        return listCopy;
    }

    /**
     * Helper method to retrieve Java FX GUI component.
     *
     * @param <T>
     * @param fxId
     * @return
     */
    protected <T extends Node> T find(final String fxId) {
        return (T) lookup(fxId).queryAll().iterator().next();
    }

    protected void ConnectNodes(List<IGraphNode> places, List<IGraphNode> transitions) throws DataServiceException {

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (IGraphNode place : places) {
                    for (IGraphNode transition : transitions) {
                        CreateArc(place, transition);
                        CreateArc(transition, place);
                    }
                }
            } catch (DataServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
    }

    protected List<IGraphNode> CreatePlaces(int count) {

        final List<IGraphNode> places = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    places.add(CreatePlace());
                }
            } catch (DataServiceException ex) {
                System.out.println(ex.getMessage());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);

        return places;
    }

    protected List<IGraphNode> CreateTransitions(int count) {

        final List<IGraphNode> transitions = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    transitions.add(CreateTransition());
                }
            } catch (DataServiceException ex) {
                System.out.println(ex.getMessage());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);

        return transitions;
    }
    
    protected IGraphCluster ClusterNodes(List<IGraphNode> places, List<IGraphNode> transitions, int nodesToCluster) throws DataServiceException {
        
        List<IGraphElement> elements = new ArrayList();
        
        while (elements.size() != nodesToCluster) {
            if (Math.random() > 0.5) {
                if (places.size() > 0) {
                    elements.add(places.remove(getRandomIndex(places)));
                }
            } else {
                if (transitions.size() > 0) {
                    elements.add(transitions.remove(getRandomIndex(transitions)));
                }
            }
        }
        
        final List<IGraphCluster> cluster = new ArrayList();
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                cluster.add(hierarchyService.cluster(elements));
            } catch (DataServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
        
        return cluster.get(0);
    }
    
    protected void UngroupCluster(IGraphCluster cluster) {
        
        final List<IGraphElement> clusters = new ArrayList();
        clusters.add(cluster);
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            hierarchyService.restore(clusters);
            isFinished.set(true);
        });
        waitForFxThread(isFinished);
    }
    
    protected int getRandomIndex(Collection list) {
        return (int) Math.floor(Math.random() * list.size());
    }
    
    protected void RemoveArc(IGraphArc arc) {
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                dataService.remove(arc);
            } catch (DataServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
    }

    protected void RemoveNode(IGraphNode node) {
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                dataService.remove(node);
            } catch (DataServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
    }

    private IGraphArc CreateArc(IGraphNode source, IGraphNode target) throws DataServiceException {
        return dataService.connect(source, target);
    }

    private IGraphNode CreatePlace() throws DataServiceException {
        return dataService.create(Element.Type.PLACE, Math.random() * 1000, Math.random() * 800);
    }

    private IGraphNode CreateTransition() throws DataServiceException {
        return dataService.create(Element.Type.TRANSITION, Math.random() * 1000, Math.random() * 800);
    }

    private void waitForFxThread(AtomicBoolean isFinished) {
        while (!isFinished.get()) {
            try {
                synchronized (this) {
                    System.out.println("Waiting...");
                    wait(50);
                }
            } catch (InterruptedException ex) {
                System.out.println(ex.toString());
            }
        }
    }
}
