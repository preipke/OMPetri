
import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author PR
 */
public class TestFXBase extends ApplicationTest {

    private Stage primaryStage;

    private final String EDITOR_PANE_ID = "#editorPane";
    
    protected DataGraphService dataGraphService;
    protected SelectionService selectionService;
    protected GraphDao graphDao;
    protected DataDao dataDao;

    @BeforeClass
    public static void setupHeadlessMode() {
        
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
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
        primaryStage = stage;
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
     * Helper method to retrieve Java FX GUI component.
     *
     * @param <T>
     * @param fxId
     * @return
     */
    public <T extends Node> T find(final String fxId) {
        return (T) lookup(fxId).queryAll().iterator().next();
    }
}
