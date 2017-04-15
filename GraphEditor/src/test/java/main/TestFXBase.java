package main;

import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.api.*;

/**
 *
 * @author PR
 */
public class TestFXBase extends ApplicationTest {

    private final static boolean isHeadless = false;
    
    private final String EDITOR_PANE_ID = "#editorPane";
    
    protected DataGraphService dataGraphService;
    protected SelectionService selectionService;
    protected GraphDao graphDao;
    protected DataDao dataDao;

    @BeforeClass
    public static void setupHeadlessMode() {
        if (isHeadless) {
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

        BorderPane editorPane = find(EDITOR_PANE_ID);
        GraphScene graphScene = (GraphScene) editorPane.getCenter();
        
        dataGraphService = (DataGraphService) graphScene.getObjects().get(0);
        selectionService = (SelectionService) graphScene.getObjects().get(1);
        
        dataDao = dataGraphService.getDataDao();
        graphDao = (GraphDao) graphScene.getGraph();
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
    public void freeze(int ms) {
        try {
            synchronized(this){
                wait(ms);
            }
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Helper method to retrieve Java FX GUI component.
     *
     * @param <T>
     * @param fxId
     * @return
     */
    public <T extends Node> T find(final String fxId) {
        return (T) lookup(fxId).queryAll().iterator().next();
    }

    public void CreateConnections(List<IGraphNode> places, List<IGraphNode> transitions) throws DataGraphServiceException {

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (IGraphNode place : places) {
                    for (IGraphNode transition : transitions) {
                        CreateArc(place, transition);
                        CreateArc(transition, place);
                    }
                }
            } catch (DataGraphServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
    }

    public IGraphArc CreateArc(IGraphNode source, IGraphNode target) throws DataGraphServiceException {
        return dataGraphService.connect(source, target);
    }

    public IGraphNode CreatePlace() throws DataGraphServiceException {
        return dataGraphService.create(Element.Type.PLACE, Math.random() * 1000, Math.random() * 800);
    }

    public IGraphNode CreateTransition() throws DataGraphServiceException {
        return dataGraphService.create(Element.Type.TRANSITION, Math.random() * 1000, Math.random() * 800);
    }

    public List<IGraphNode> CreatePlaces(int count) {

        final List<IGraphNode> places = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < count; i++) {
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

    public List<IGraphNode> CreateTransitions(int count) {

        final List<IGraphNode> transitions = new ArrayList();

        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                for (int i = 0; i < count; i++) {
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
    
    public int getRandomIndex(List list) {
        return (int) Math.floor(Math.random() * list.size());
    }
    
    public void RemoveArc(IGraphArc arc) {
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                dataGraphService.remove(arc);
            } catch (DataGraphServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
    }

    public void RemoveNode(IGraphNode node) {
        
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                dataGraphService.remove(node);
            } catch (DataGraphServiceException ex) {
                System.out.println(ex.toString());
            } finally {
                isFinished.set(true);
            }
        });
        waitForFxThread(isFinished);
    }

    public void waitForFxThread(AtomicBoolean isFinished) {
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
