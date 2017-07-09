/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor;

import edu.unibi.agbi.gnius.business.controller.editor.details.ConnectionsController;
import edu.unibi.agbi.gnius.business.controller.editor.details.IdentifierController;
import edu.unibi.agbi.gnius.business.controller.editor.details.NodeListController;
import edu.unibi.agbi.gnius.business.controller.editor.details.ParameterController;
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
public class ElementEditorController implements Initializable
{
    @Autowired private DataService dataService;
    
    @Autowired private ConnectionsController connectionsController;
    @Autowired private NodeListController nodeListController;
    @Autowired private IdentifierController identifierController;
    @Autowired private ParameterController parameterController;
    
    @FXML private CheckBox choiceSticky;
    @FXML private Button buttonBack;
    @FXML private Button buttonForward;
    
    private List<IDataElement> elementsRecent;
    private IDataElement elementActive;
    private int elementIndex;
    
    public void clear() {
        elementsRecent = new ArrayList();
        elementActive = null;
        elementIndex = -1;
    }
    
    public void setElement(IDataElement element) {
        
        elementActive = element;

        if (isIndexAvailable(elementIndex) && elementsRecent.get(elementIndex).equals(element)) {
            // list and index has not to be altered
        } else if (isIndexAvailable(elementIndex + 1) && elementsRecent.get(elementIndex + 1).equals(element)) {
            elementIndex++; // list has not to be altered
        } else {
            if (elementsRecent.size() - 1 > elementIndex) {
                elementsRecent = elementsRecent.subList(0, elementIndex + 1);
            }
            elementsRecent.add(element);
            if (elementIndex < 25) { // limit list to 25 elements
                elementIndex++;
            } else {
                elementsRecent.remove(0);
            }
        }
        
        if (isIndexAvailable(elementIndex - 1)) {
            buttonBack.setDisable(false);
        } else {
            buttonBack.setDisable(true);
        }
        
        if (isIndexAvailable(elementIndex + 1)) {
            buttonForward.setDisable(false);
        } else {
            buttonForward.setDisable(true);
        }
        
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
        parameterController.setElement(element);
    }
    
    private void NextDetails() {
        if (isIndexAvailable(elementIndex + 1)) {
            elementIndex++;
            setElement(elementsRecent.get(elementIndex));
        }
    }
    
    private void PreviousDetails() {
        if (isIndexAvailable(elementIndex - 1)) {
            elementIndex--;
            setElement(elementsRecent.get(elementIndex));
        }
    }
    
    private boolean isIndexAvailable(int index) {
        if (index >= 0) {
            if (index < elementsRecent.size()) {
                return true;
            }
        }
        return false;
    }
    
    private void PopulateNodesPane() {
        List nodes = new ArrayList();
        nodes.addAll(dataService.getDao().getModel().getPlaces());
        nodes.addAll(dataService.getDao().getModel().getTransitions());
        nodeListController.setData(nodes, elementActive);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceSticky.selectedProperty().addListener(cl -> {
            if (elementActive != null) {
                elementActive.setSticky(choiceSticky.isSelected());
                PopulateNodesPane();
            }
        });
        buttonBack.setOnAction(eh -> PreviousDetails());
        buttonBack.setPadding(Insets.EMPTY);
        buttonForward.setOnAction(eh -> NextDetails());
        buttonForward.setPadding(Insets.EMPTY);
    }
}
