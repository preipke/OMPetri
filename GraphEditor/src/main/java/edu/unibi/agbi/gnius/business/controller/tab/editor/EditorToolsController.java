/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.business.mode.exception.EditorModeLockException;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gnius.util.SpringFXMLLoader;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorToolsController implements Initializable
{
    @Autowired private DataGraphService dataService;
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private SpringFXMLLoader springFXMLLoader;
    
    @FXML private ChoiceBox choicesCreateNode;
    @FXML private SplitMenuButton buttonMenuCreate;
    @FXML private TextArea textLogArea;
    
    public void CreateNode(MouseEvent target) {
        try {
            dataService.create(((NodeTypeChoice) choicesCreateNode.getSelectionModel().getSelectedItem()).getType() , target , Point2D.ZERO);
        } catch (NodeCreationException | AssignmentDeniedException ex) {

        }
    }
    
    public void addToLog(String msg) {
        textLogArea.appendText(msg + "\n");
    }
    
    public void addToLog(Throwable thr) {
        textLogArea.appendText(thr.toString() + "\n");
    }
    
    @FXML
    public void OpenResultsView() {
        
        Parent root;
        
        try {
            root = springFXMLLoader.load("/fxml/Results.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        
//        scene.getStylesheets().add("/styles/Styles.css");
//        scene.getStylesheets().add("/styles/gravis/nodes.css");
        
        stage.setTitle("GraVisFX - Results View");
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    public void EnableCreatingNodes() {
        try {
            mouseEventHandler.setNodeCreationMode();
        } catch (EditorModeLockException ex) {
            addToLog(ex.getMessage());
        }
    }
    
    @FXML
    public void buttonCreateNodeEnable() {
        try {
            mouseEventHandler.setNodeCreationMode();
        } catch (EditorModeLockException ex) {
            addToLog(ex.getMessage());
        }
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        buttonMenuCreate.getItems().clear();
        buttonMenuCreate.getItems().add(new MenuItem("Place"));
        buttonMenuCreate.getItems().add(new MenuItem("Transition"));
        
        ObservableList<NodeTypeChoice> nodeChoices = FXCollections.observableArrayList();
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.PLACE, "Place"));
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.TRANSITION, "Transition"));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }

    private class NodeTypeChoice
    {
        private final PN_Element.Type type;
        private final String name;

        public NodeTypeChoice(PN_Element.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        public PN_Element.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
