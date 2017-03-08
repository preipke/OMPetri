
import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
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
public class TestFXBase extends ApplicationTest
{
    private Stage primaryStage;
//    protected static ResourceBundle bundle;
    
    @BeforeClass
    public static void setupHeadlessMode() {
        if(Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot" , "glass");
            System.setProperty("testfx.headless" , "true");
            System.setProperty("prism.order" , "sw");
            System.setProperty("prism.text" , "t2k");
            System.setProperty("java.awt.headless" , "true");
            
            System.out.println(">> Headless mode:");
            System.out.println(">> enabled");
        } else {
            System.out.println(">> Headless mode:");
            System.out.println(">> disabled");
        }
//        bundle = ResourceBundle.getBundle("Bundle");
    }
    
    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(Main.class); // verifies that MainApp is a JavaFX application (extends Application)
    }
    
    @Override 
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.show();
    }
    
    @After 
    public void afterEachTest() throws TimeoutException {
        FxToolkit.hideStage();
        // release button strokes so they dont get stuck ("resource management")
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }
    
    /**
     * Helper method to retrieve Java FX GUI component.
     * @param <T>
     * @param fxId
     * @return 
     */
    public <T extends Node> T find(final String fxId) {
        return (T) lookup(fxId).queryAll().iterator().next();
    }
}
