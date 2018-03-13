/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.menu;

import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.core.util.GuiFactory;
import edu.unibi.agbi.editor.presentation.controller.LogController;
import edu.unibi.agbi.editor.presentation.controller.editor.GraphController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Controller
public class ViewMenuController implements Initializable
{
    @Autowired private GuiFactory guiFactory;
    @Autowired private GraphController graphController;
    @Autowired private LogController logController;
    @Autowired private MessengerService messengerService;
    
    @FXML
    public void ShowElementEditor() {
        graphController.ShowInspector(null);
    }
    
    @FXML
    public void ShowGraphEditor() {
        graphController.ShowGraph();
    }
    
    @FXML
    public void OpenLogWindow() {
        logController.ShowWindow();
    }
    
    @FXML
    public void OpenResultsWindow() {
        try {
            guiFactory.BuildResultsViewer();
        } catch (IOException ex) {
            messengerService.addException("Cannot open results viewer!", ex);
        }
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
}
