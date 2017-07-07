/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.menu;

import edu.unibi.agbi.gnius.business.controller.LogController;
import edu.unibi.agbi.gnius.business.controller.ResultsController;
import edu.unibi.agbi.gnius.business.controller.editor.GraphController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ViewMenuController implements Initializable
{
    @Autowired private GraphController graphController;
    @Autowired private LogController logController;
    @Autowired private ResultsController resultsController;
    
    @FXML
    public void ShowParams() {
        graphController.ShowParameterPane(null);
    }
    
    @FXML
    public void OpenLogWindow() {
        logController.ShowWindow();
    }
    
    @FXML
    public void OpenNewResultsWindow() {
        resultsController.OpenWindow();
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
}
