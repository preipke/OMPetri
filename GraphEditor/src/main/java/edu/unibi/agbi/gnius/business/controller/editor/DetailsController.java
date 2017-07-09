/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor;

import edu.unibi.agbi.gnius.business.controller.editor.details.ConnectionsController;
import edu.unibi.agbi.gnius.business.controller.editor.details.IdentifierController;
import edu.unibi.agbi.gnius.business.controller.editor.details.NodeListController;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.service.DataService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class DetailsController implements Initializable
{
    @Autowired private DataService dataService;
    
    @Autowired private ConnectionsController connectionsController;
    @Autowired private NodeListController nodeListController;
    @Autowired private IdentifierController identifierController;
    
    @FXML private CheckBox choiceSticky;
    @FXML private Button buttonBack;
    @FXML private Button buttonForward;
    
    private IDataElement activeElement;
    
    public void ShowDetails(IDataElement element) {
        
        activeElement = element;
        
        switch (element.getElementType()) {
            case ARC:
                choiceSticky.setDisable(true);
                break;
            default:
                choiceSticky.setDisable(false);
        }
        
        if (!choiceSticky.isDisable() && choiceSticky.isSelected() != element.isSticky()) {
            choiceSticky.setSelected(element.isSticky()); // changelistener populates nodes pane
        } else {
            PopulateNodesPane();
        }
        
        connectionsController.setElement(element);
        identifierController.setElement(element);
    }
    
    private void PopulateNodesPane() {
        List nodes = new ArrayList();
        nodes.addAll(dataService.getDao().getModel().getPlaces());
        nodes.addAll(dataService.getDao().getModel().getTransitions());
        nodeListController.setData(nodes, activeElement);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceSticky.selectedProperty().addListener(cl -> {
            if (activeElement != null) {
                activeElement.setSticky(choiceSticky.isSelected());
                PopulateNodesPane();
            }
        });
//        buttonBack.setOnAction(eh -> graphController.ShowGraphPane());
        buttonBack.setPadding(Insets.EMPTY);
//        buttonForward.setOnAction(eh -> graphController.ShowGraphPane());
        buttonForward.setPadding(Insets.EMPTY);
    }
}
