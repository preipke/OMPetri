/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.panel;

import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.presentation.controller.editor.panel.PropertiesPanelController.ViewType;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ElementPanelController implements Initializable
{
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private MessengerService messengerService;
    
    @Autowired private ClusterPanelController clusterPanelController;
    @Autowired private PropertiesPanelController propertiesPanelController;
    
    @FXML private VBox elementFrame;
    
    @FXML private Button buttonClone;
    @FXML private SplitMenuButton buttonDisable;
    @FXML private MenuItem buttonDisableAll;
    @FXML private MenuItem buttonEnableAll;
    
    private IGraphElement element;
    
    public void setElement(IGraphElement element) throws Exception {
        
        if (element == null) {
            return;
        }
        
        this.element = element;
        
        updateControlButtons(element);
        updateGuiElements(element.getData());
        updateDisplayData(element.getData());
        
    }
    
    private void updateGuiElements(IDataElement data) throws Exception {
        
        elementFrame.getChildren().clear();

        switch (data.getType()) {
            case ARC:
                buttonClone.setDisable(true);
                elementFrame.getChildren().add(propertiesPanelController.getPanel());
                break;

            case CLUSTER:
                buttonClone.setDisable(true);
                elementFrame.getChildren().add(clusterPanelController.getPanel());
                break;

            case CLUSTERARC:
                buttonClone.setDisable(true);
                elementFrame.getChildren().add(clusterPanelController.getPanel());
                break;

            case PLACE:
                buttonClone.setDisable(false);
                elementFrame.getChildren().add(propertiesPanelController.getPanel());
                break;

            case TRANSITION:
                buttonClone.setDisable(false);
                elementFrame.getChildren().add(propertiesPanelController.getPanel());
                break;

            default:
                throw new Exception("Found unhandled data type!");
        }
        
    }
    
    private void updateDisplayData(IDataElement data) throws Exception {

        switch (data.getType()) {
            case ARC:
                propertiesPanelController.setElement(data, ViewType.GRAPH);
                break;

            case CLUSTER:
                clusterPanelController.setElement(data);
                break;

            case CLUSTERARC:
                clusterPanelController.setElement(data);
                break;

            case PLACE:
                propertiesPanelController.setElement(data, ViewType.GRAPH);
                break;

            case TRANSITION:
                propertiesPanelController.setElement(data, ViewType.GRAPH);
                break;

            default:
                throw new Exception("Found unhandled data type!");
        }
    }
    
    public void updateControlButtons(IGraphElement element) {
        buttonDisable.getItems().clear();
        if (element.getData().getType() == DataType.CLUSTER
                || element.getData().getType() == DataType.CLUSTERARC) {
            setClusterControlButton(element);
        } else {
            setElementControlButton(element);
        }
    }
    
    private void setClusterControlButton(IGraphElement element) {
        if (element.getData().isDisabled()) {
            buttonDisable.setText("Enable All");
        } else {
            buttonDisable.setText("Disable All");
        }
    }
    
    private void setElementControlButton(IGraphElement element) {
        if (element.isElementDisabled()) {
            buttonDisable.setText("Enable");
        } else {
            buttonDisable.setText("Disable");
        }
        if (element.getData().getShapes().size() > 1) {
            buttonDisable.getItems().add(buttonDisableAll);
            buttonDisable.getItems().add(buttonEnableAll);
        }
    }
    
    public IGraphElement getSelectedGraphElement() {
        return element;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        buttonClone.setOnAction(eh -> {
            if (element != null) {
                if (element.getData() instanceof IDataNode) {
                    try {
                        IDataNode data = (IDataNode) element.getData();
                        mouseEventHandler.setCloningMode(data);
                    } catch (Exception ex) {
                        messengerService.addException("Cannot clone node!", ex);
                    }
                }
            }
        });
        
        buttonDisable.setOnAction(eh -> {
            if (element.getData().getShapes().size() == 1) {
                element.getData().setDisabled(
                        !element.getData().isDisabled());
            } else {
                element.setElementDisabled(!element.isElementDisabled());
            }
            updateControlButtons(element);
        });
        
        buttonDisableAll.setOnAction(eh -> element.getData().setDisabled(true));
        buttonEnableAll.setOnAction(eh -> element.getData().setDisabled(false));
    }
}
