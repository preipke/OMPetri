/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller;

import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.SimulationService;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.presentation.controller.menu.FileMenuController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Controller
public class MainController implements Initializable
{
    @Autowired private ModelService dataService;
    @Autowired private SimulationService simulationService;

    @Autowired private FileMenuController fileMenuController;

    @FXML private Label statusTop;

    public Stage getStage() {
        return (Stage) statusTop.getScene().getWindow();
    }

    public Label getStatusTop() {
        return statusTop;
    }

    public void ShowDialogExit(Event event) {

        List<ModelDao> daos = dataService.getDataDaosWithChanges();

        ButtonType buttonCancel = new ButtonType("Cancel");
        ButtonType buttonSave = new ButtonType("Save");
        ButtonType buttonQuit = new ButtonType("Exit");

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm exit");

        if (daos.isEmpty()) {
            alert.setHeaderText("Close the application");
            alert.setContentText("Are you sure you want to close the application?");
            alert.getButtonTypes().setAll(buttonCancel, buttonQuit);
        } else {
            alert.setHeaderText("Close the application and discard latest changes");
            alert.setContentText("You made changes to your model(s). Are you sure you want to close the application and discard any changes?");
            alert.getButtonTypes().setAll(buttonCancel, buttonSave, buttonQuit);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            for (ModelDao dao : daos) {
                fileMenuController.SaveAs(dao);
            }
            if (event != null) {
                event.consume();
            }
        } else if (result.get() == buttonQuit) {
            try {
                simulationService.StopSimulation();
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}
