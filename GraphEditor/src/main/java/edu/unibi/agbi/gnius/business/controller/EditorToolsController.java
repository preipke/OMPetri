/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.util.SpringFXMLLoader;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorToolsController implements Initializable
{
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private SpringFXMLLoader springFXMLLoader;
    @Autowired private DataGraphService dataGraphService;
    @Autowired private SelectionService selectionService;
    @Autowired private MessengerService messengerService;
    
    @FXML private MenuItem buttonCreatePlace;
    @FXML private MenuItem buttonCreateTransition;
    @FXML private Button buttonRemove;
    @FXML private Button buttonCopy;
    @FXML private Button buttonClone;
    @FXML private Button buttonClusterCreate;
    @FXML private Button buttonClusterRemove;
    
    @Value("${results.window.fxml}")
    private String resultsWindowFxml;
    @Value("${results.window.stylesheet}")
    private String resultsWindowStylesheet;
    @Value("${results.window.title}")
    private String resultsWindowTitle;
    private ResultsWindowController resultsWindowController;
    private Stage resultsWindow;
    
    private Element.Type createNodeType;
    
    public Element.Type getCreateNodeType() {
        return createNodeType;
    }
    
    public void EnableCreatingNodes() {
        try {
            mouseEventHandler.setNodeCreationMode();
        } catch (Exception ex) {
            messengerService.addToLog(ex.getMessage());
        }
    }
    
    private void CreateCluster() {
        try {
            dataGraphService.cluster(selectionService.getSelectedElements());
        } catch (DataGraphServiceException ex) {
            messengerService.addToLog(ex);
        }
    }
    
    private void RemoveCluster() {
        try {
            dataGraphService.uncluster(selectionService.getSelectedElements());
        } catch (DataGraphServiceException ex) {
            messengerService.addToLog(ex);
        }
    }
    
    @FXML
    public void OpenResultsView() {
        
        if (resultsWindow != null) {
            resultsWindow.show();
            resultsWindow.centerOnScreen();
            resultsWindowController.UpdateSimulationChoices();
            return;
        }
        
        Parent root;
        try {
            root = springFXMLLoader.load(resultsWindowFxml);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add(resultsWindowStylesheet);
        
        resultsWindow = new Stage();
        resultsWindow.setTitle(resultsWindowTitle);
        resultsWindow.setScene(scene);
        resultsWindow.show();
        
        resultsWindowController = (ResultsWindowController) springFXMLLoader.getBean(ResultsWindowController.class);
        resultsWindowController.UpdateSimulationChoices();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        buttonCreatePlace.setOnAction(e -> { 
            createNodeType = Element.Type.PLACE; 
            EnableCreatingNodes();
        });
        buttonCreateTransition.setOnAction(e -> { 
            createNodeType = Element.Type.TRANSITION; 
            EnableCreatingNodes();
        });
        
        buttonClone.setOnAction(e -> OpenResultsView());
        
        buttonClusterCreate.setOnAction(e -> CreateCluster());
        buttonClusterRemove.setOnAction(e -> RemoveCluster());
    }
}
