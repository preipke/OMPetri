package main;

import edu.unibi.agbi.editor.Main;
import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphCluster;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.HierarchyService;
import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.gravisfx.entity.root.connection.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
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

//    private final static boolean HEADLESS = true;
    
    @Autowired protected ModelService dataService;
    @Autowired protected HierarchyService hierarchyService;
    @Autowired protected SelectionService selectionService;

    @BeforeClass
    public static void setupHeadlessMode() {
//        if (HEADLESS) {
//            System.setProperty("java.awt.headless", "true");
//            System.setProperty("testfx.robot", "glass");
//            System.setProperty("testfx.headless", "true");
//            System.setProperty("prism.order", "sw");
//            System.setProperty("prism.text", "t2k");
//        }
//        bundle = ResourceBundle.getBundle("Bundle");
    }

    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class); // verifies that MainApp is a JavaFX application (extends Application)
        dataService.setDao(dataService.CreateDao());
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

    protected void ConnectNodes(List<IGraphNode> places, List<IGraphNode> transitions) throws DataException {

        final Object monitor = new Object();
        Platform.runLater(() -> {
            try {
                for (IGraphNode place : places) {
                    for (IGraphNode transition : transitions) {
                        CreateArc(place, transition);
                        CreateArc(transition, place);
                    }
                }
            } catch (DataException ex) {
                System.out.println(ex.toString());
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        System.out.println("ConnectNodes");
        waitForFxThread(monitor);
    }

    protected List<IGraphNode> CreatePlaces(int count) {

        final List<IGraphNode> places = new ArrayList();

        final Object monitor = new Object();
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    places.add(CreatePlace());
                }
            } catch (DataException ex) {
                System.out.println(ex.getMessage());
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        System.out.println("CreatePlaces");
        waitForFxThread(monitor);

        return places;
    }

    protected List<IGraphNode> CreateTransitions(int count) {

        final List<IGraphNode> transitions = new ArrayList();

        final Object monitor = new Object();
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    transitions.add(CreateTransition());
                }
            } catch (DataException ex) {
                System.out.println(ex.getMessage());
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        System.out.println("CreateTransitions");
        waitForFxThread(monitor);

        return transitions;
    }
    
    protected IGraphCluster ClusterNodes(List<IGraphNode> places, List<IGraphNode> transitions, int nodesToCluster) throws DataException {
        
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
        
        final Object monitor = new Object();
        Platform.runLater(() -> {
            try {
                cluster.add(hierarchyService.cluster(dataService.getDao(), elements, dataService.getClusterId(dataService.getDao())));
            } catch (DataException ex) {
                System.out.println(ex.toString());
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        System.out.println("ClusterNodes");
        waitForFxThread(monitor);
        
        return cluster.get(0);
    }
    
    protected void UngroupCluster(IGraphCluster cluster) {
        
        final List<IGraphElement> clusters = new ArrayList();
        clusters.add(cluster);
        
        final Object monitor = new Object();
        Platform.runLater(() -> {
            hierarchyService.restore(clusters);
            synchronized (monitor) {
                monitor.notifyAll();
            }
        });
        System.out.println("UngroupCluster");
        waitForFxThread(monitor);
    }
    
    protected int getRandomIndex(Collection list) {
        return (int) Math.floor(Math.random() * list.size());
    }
    
    protected void RemoveArc(IGraphArc arc) {
        
        final Object monitor = new Object();
        Platform.runLater(() -> {
            try {
                dataService.remove(arc);
            } catch (DataException ex) {
                System.out.println(ex.toString());
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        waitForFxThread(monitor);
    }

    protected void RemoveNode(IGraphNode node) {
        
        final Object monitor = new Object();
        Platform.runLater(() -> {
            try {
                dataService.remove(node);
            } catch (DataException ex) {
                System.out.println(ex.toString());
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        waitForFxThread(monitor);
    }

    private IGraphArc CreateArc(IGraphNode source, IGraphNode target) throws DataException {
        return dataService.connect(dataService.getDao(), source, target);
    }

    private IGraphNode CreatePlace() throws DataException {
        return dataService.CreateNode(dataService.getDao(), DataType.PLACE, Math.random() * 1000, Math.random() * 800);
    }

    private IGraphNode CreateTransition() throws DataException {
        return dataService.CreateNode(dataService.getDao(), DataType.TRANSITION, Math.random() * 1000, Math.random() * 800);
    }

    private void waitForFxThread(Object monitor) {
        synchronized (monitor) {
            try {
                System.out.println("Waiting...");
                monitor.wait();
            } catch (InterruptedException ex) {
                System.out.println(ex.toString());
            }
        }
    }
}
