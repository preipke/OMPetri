/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class MainController implements Initializable {
    
    @Autowired private MessengerService messengerService;
    @Autowired private ElementController elementController;
    @Autowired private ParameterController parameterController;
    
    @FXML private VBox elementFrame;
    @FXML private VBox parameterFrame;

    public Stage getStage() {
        return (Stage) elementFrame.getScene().getWindow();
    }
    
    public void HideSidePanel() {
        if (elementFrame.isVisible()) {
            try {
                elementController.StoreElementDetails();
            } catch (DataGraphServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            }
            elementFrame.setVisible(false);
        }
        parameterFrame.setVisible(false);
    }
    
    public void ShowDetails(IGraphElement element) {
        elementFrame.setVisible(true);
        parameterFrame.setVisible(false);
        elementController.ShowElementDetails(element);
    }
    
    public void ShowParameters(IDataElement element) {
        elementFrame.setVisible(false);
        parameterFrame.setVisible(true);
        parameterController.ShowParameterDetails(element);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        elementFrame.setVisible(false);
        parameterFrame.setVisible(false);
    }
}
