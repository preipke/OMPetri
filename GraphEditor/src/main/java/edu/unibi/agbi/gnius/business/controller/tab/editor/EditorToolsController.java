/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.business.controller.simulation.ResultsViewController;
import edu.unibi.agbi.gnius.business.mode.exception.EditorModeLockException;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.util.SpringFXMLLoader;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
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
    
    private Stage resultsView;
    private ResultsViewController resultsViewController;
    
    public Element.Type getSelectedNodeType() {
        return ((NodeTypeChoice) choicesCreateNode.getSelectionModel().getSelectedItem()).getType();
    }
    
    public void addToLog(String msg) {
        textLogArea.appendText(msg + "\n");
    }
    
    public void addToLog(Throwable thr) {
        textLogArea.appendText(thr.toString() + "\n");
    }
    
    @FXML
    public void OpenResultsView() {
        
        if (resultsView != null) {
            resultsView.show();
            resultsView.centerOnScreen();
            resultsViewController.UpdateChoices();
            return;
        }
        
        Parent root;
        
        try {
            root = springFXMLLoader.load("/fxml/Results.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        resultsView = new Stage();
        Scene scene = new Scene(root);
        
        resultsView.setTitle("GraVisFX - Results View");
        resultsView.setScene(scene);
        resultsView.show();
        
        resultsViewController = (ResultsViewController) springFXMLLoader.getBean(ResultsViewController.class);
        resultsViewController.UpdateChoices();
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
        nodeChoices.add(new NodeTypeChoice(Element.Type.PLACE, "Place"));
        nodeChoices.add(new NodeTypeChoice(Element.Type.TRANSITION, "Transition"));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }

    private class NodeTypeChoice
    {
        private final Element.Type type;
        private final String name;

        public NodeTypeChoice(Element.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        public Element.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
