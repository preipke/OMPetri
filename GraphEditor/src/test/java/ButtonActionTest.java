
import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;

import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.testfx.api.FxAssert;
import org.testfx.api.FxRobotException;
import static org.testfx.api.FxAssert.verifyThat;
import org.testfx.matcher.base.NodeMatchers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author PR
 */
public class ButtonActionTest extends TestFXBase
{
    final String CREATE_NODE_BUTTON = "#buttonCreate";
    final String CREATE_NODE_CHOICEBOX = "#choicesCreateNode";
    
    final String EDITOR_PANE_ID = "#editorPane";
    protected Graph graphDao;
    
    /**
     * Expects the given ID not to exist.
     */
    //@Test(expected = FxRobotException.class)
    public void clickOnButton() {
        clickOn("#sector9");
    }
    
    //@Test
    public void testCreatingNodes() {
        
        BorderPane editorPane = find(EDITOR_PANE_ID);
        GraphScene graphScene = (GraphScene) editorPane.getCenter();
        graphDao = graphScene.getGraph();
        
        ChoiceBox nodeChoices = find(CREATE_NODE_CHOICEBOX);
        nodeChoices.getSelectionModel().select(0);
        
        clickOn(CREATE_NODE_BUTTON);
        
        moveTo(400 , 200);
        clickOn(MouseButton.PRIMARY);
        
        moveTo(400 , 400);
        clickOn(MouseButton.PRIMARY);
        
        moveTo(600 , 300);
        clickOn(MouseButton.PRIMARY);
        
        press(KeyCode.ESCAPE);
        release(KeyCode.ESCAPE);
        
        Platform.runLater(() -> {
            nodeChoices.getSelectionModel().select(1);
        });
        
        while (!nodeChoices.getSelectionModel().isSelected(1)) {
            try {
                synchronized(this) {
                    wait(10);
                }
                System.out.println("Waiting...");
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
        
        clickOn(CREATE_NODE_BUTTON);
        
        moveTo(500 , 300);
        clickOn(MouseButton.PRIMARY);
        release(MouseButton.PRIMARY);
        
        press(KeyCode.ESCAPE);
        release(KeyCode.ESCAPE);
        
//        Assert.assertEquals(4 , graphDao.getNodes().length);
//        Assert.assertEquals(3 , petriNetDao.getPlaces().size());
//        Assert.assertEquals(1 , petriNetDao.getTransitions().size());
//        Assert.assertEquals(4 , petriNetDao.getPlacesAndTransitions().size());
    }
    
//    @Test
//    public void ensureButtonConnectsNodes() {
//        
//        
//    }
    
    /*
    @Test
    public void ensureButtonLoadsGraph() {
        sleep(1000);
        FxAssert.verifyThat(buttonLoad, (Label label) -> { // Hamcrest Matcher
            return label.getText().contains("Press the button!");
        });
        clickOn(TEST_BUTTON_FXID);
        sleep(1000); // just for visualization
        FxAssert.verifyThat(TEST_LABEL_FXID, (Label label) -> {
            return label.getText().contains("Added test nodes!");
        });
    }
    
    @Test
    public void verifyGraphViewIsEmpty() {
        verifyThat(viewPane, NodeMatchers.isNotNull());
        
        clickOn(TEST_BUTTON_FXID);
        
        Pane graphView = find(GRAPH_VIEW_FXID);
        
        Assert.assertEquals(0 , graphView.getChildren().size());
    }
    
    //@Test
    public void ensureMouseIsMoving() {
        clickOn("");
        moveTo(""); // moves the mouse
        sleep(1000); // just for visualization
    }*/
}
