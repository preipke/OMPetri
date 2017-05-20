/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.business.controller.editor.element.ParameterController;
import edu.unibi.agbi.gnius.business.controller.editor.element.ElementController;
import edu.unibi.agbi.gnius.business.controller.editor.model.ModelController;
import edu.unibi.agbi.gnius.business.controller.menu.FileMenuController;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
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
    @Autowired private DataService dataService;
    
    @Autowired private FileMenuController fileMenuController;
    @Autowired private ElementController elementController;
    @Autowired private ModelController modelController;
    @Autowired private ParameterController parameterController;
    
    @FXML private VBox modelFrame;
    @FXML private VBox elementFrame;
    @FXML private VBox parameterFrame;

    public Stage getStage() {
        return (Stage) elementFrame.getScene().getWindow();
    }
    
    public void HideElementPanel() {
        if (elementFrame.isVisible()) {
            try {
                elementController.StoreElementDetails();
            } catch (DataServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            }
            elementFrame.setVisible(false);
        }
        parameterFrame.setVisible(false);
    }
    
    public void HideModelPanel() {
        modelFrame.setVisible(false);
    }
    
    public void ShowDialogExit(Event event) {
        
        List<DataDao> daos = dataService.getDataDaosWithChanges();

        ButtonType buttonSave = new ButtonType("Save");
        ButtonType buttonQuit = new ButtonType("Exit");
        ButtonType buttonCancel = new ButtonType("Cancel");
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm exit");
        
        if (daos.isEmpty()) {
            alert.setHeaderText("Close the application");
            alert.setContentText("Are you sure you want to close the application?");
            alert.getButtonTypes().setAll(buttonQuit, buttonCancel);
        } else {
            alert.setHeaderText("Close the application and discard latest changes");
            alert.setContentText("You made changes to your model(s). Are you sure you want to close the application and discard any changes?");
            alert.getButtonTypes().setAll(buttonSave, buttonQuit, buttonCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            for (DataDao dao : daos) {
                fileMenuController.SaveAs(dao);
            }
            if (event != null) {
                event.consume();
            }
        } else if (result.get() == buttonQuit) {
            try {
                System.out.println("Closing application...");
                System.exit(0);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else {
            if (event != null) {
                event.consume();
            }
        }
    }
    
    public void ShowElementDetails(IGraphElement element) {
        parameterFrame.setVisible(false);
        elementFrame.setVisible(true);
        elementController.ShowElementDetails(element);
    }
    
    public void ShowModel(DataDao dataDao) {
        modelFrame.setVisible(true);
        modelController.setModel(dataDao);
    }
    
    public void ShowParameters(IDataElement element) {
        elementFrame.setVisible(false);
        parameterFrame.setVisible(true);
        parameterController.ShowParameters(element);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        modelFrame.setVisible(false);
        elementFrame.setVisible(false);
        parameterFrame.setVisible(false);
    }
}
