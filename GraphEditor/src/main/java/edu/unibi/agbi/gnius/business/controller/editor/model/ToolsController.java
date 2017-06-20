/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.model;

import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.service.HierarchyService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class ToolsController implements Initializable
{
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private HierarchyController hierarchyController;
    @Autowired private HierarchyService hierarchyService;
    @Autowired private DataService dataService;
    @Autowired private SelectionService selectionService;
    @Autowired private MessengerService messengerService;
    
    @FXML private MenuItem buttonCreatePlace;
    @FXML private MenuItem buttonCreateTransition;
    @FXML private Button buttonRemove;
    @FXML private Button buttonClusterCreate;
    @FXML private Button buttonClusterRemove;
    
    private Element.Type createNodeType;
    
    public Element.Type getCreateNodeType() {
        return createNodeType;
    }
    
    public void EnableCreatingNodes() {
        try {
            mouseEventHandler.setNodeCreationMode();
        } catch (Exception ex) {
            messengerService.addException("Cannot switch to node creation mode!", ex);
        }
    }
    
    private void CreateCluster() {
        try {
            hierarchyService.cluster(selectionService.getSelectedElements());
            hierarchyController.update();
        } catch (DataServiceException ex) {
            messengerService.addException(ex);
        }
    }
    
    private void RemoveCluster() {
        hierarchyService.restore(selectionService.getSelectedElements());
        hierarchyController.update();
    }
    
    private void RemoveSelected() {
        dataService.remove(selectionService.getSelectedElements());
        selectionService.unselectAll();
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
        buttonRemove.setOnAction(e -> RemoveSelected());
        buttonClusterCreate.setOnAction(e -> CreateCluster());
        buttonClusterRemove.setOnAction(e -> RemoveCluster());
    }
}
