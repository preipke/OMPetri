/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class MainController implements Initializable {
    
    @Autowired private ElementController elementController;
    @Autowired private ParameterController parameterController;
    
    @FXML private VBox elementBox;
    @FXML private VBox detailsContainer;
    @FXML private VBox parameterContainer;
    
    public void HideElementBox() {
        elementBox.setVisible(false);
    }
    
    public void ShowDetails(IGraphElement element) {
        elementBox.setVisible(true);
        elementBox.getChildren().clear();
        elementBox.getChildren().add(detailsContainer);
        elementController.ShowElementDetails(element);
    }
    
    public void ShowParameter(IDataElement element) {
        elementBox.setVisible(true);
        elementBox.getChildren().clear();
        elementBox.getChildren().add(parameterContainer);
        parameterController.ShowParameterDetails(element);
    }
    
    public void StoreElementProperties() throws DataGraphServiceException {
        elementController.StoreElementProperties();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        HideElementBox();
    }
}
