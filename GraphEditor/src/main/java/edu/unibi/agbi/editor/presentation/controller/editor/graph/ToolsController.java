/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.graph;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.service.*;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Component
public class ToolsController implements Initializable
{
    @Autowired private ModelService modelService;
    @Autowired private HierarchyService hierarchyService;
    @Autowired private SelectionService selectionService;
    @Autowired private MessengerService messengerService;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private HierarchyController hierarchyController;
    
    @FXML private MenuItem buttonCreatePlace;
    @FXML private MenuItem buttonCreateTransition;
    @FXML private Button buttonRemove;
    @FXML private Button buttonClusterCreate;
    @FXML private Button buttonClusterRemove;
    
    private DataType createNodeType;
    
    public DataType getCreateNodeType() {
        return createNodeType;
    }
    
    public void EnableCreatingNodes() {
        mouseEventHandler.setNodeCreationMode();
    }
    
    private void CreateCluster() {
        try {
            hierarchyService.cluster(modelService.getDao(), selectionService.getSelectedElements());
            hierarchyController.update();
        } catch (DataException ex) {
            messengerService.addException(ex);
        }
    }
    
    private void RemoveCluster() {
        hierarchyService.restore(selectionService.getSelectedElements());
        hierarchyController.update();
    }
    
    private void RemoveSelected() {
        modelService.remove(selectionService.getSelectedElements());
        selectionService.unselectAll();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        buttonCreatePlace.setOnAction(e -> { 
            createNodeType = DataType.PLACE; 
            EnableCreatingNodes();
        });
        buttonCreateTransition.setOnAction(e -> { 
            createNodeType = DataType.TRANSITION; 
            EnableCreatingNodes();
        });
        buttonRemove.setOnAction(e -> RemoveSelected());
        buttonClusterCreate.setOnAction(e -> CreateCluster());
        buttonClusterRemove.setOnAction(e -> RemoveCluster());
    }
}
