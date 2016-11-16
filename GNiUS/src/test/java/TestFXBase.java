
import edu.unibi.agbi.gnius.MainApp;
import java.util.concurrent.TimeoutException;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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
    @BeforeClass
    public static void setupHeadlessMode() {
        if(Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot" , "glass");
            System.setProperty("testfx.headless" , "true");
            System.setProperty("prism.order" , "sw");
            System.setProperty("prism.text" , "t2k");
            System.setProperty("java.awt.headless" , "true");
        }
    }
    
    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(MainApp.class); // verifies that MainApp is a JavaFX application (extends Application)
    }
    
    @Override 
    public void start(Stage stage) throws Exception {
        stage.show();
    }
    
    @After 
    public void afterEachTest() throws TimeoutException {
        FxToolkit.hideStage();
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
