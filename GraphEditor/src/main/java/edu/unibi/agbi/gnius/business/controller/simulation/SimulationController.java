/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.simulation;

import edu.unibi.agbi.gnius.core.dao.PetriNetDao;
import edu.unibi.agbi.gnius.core.simulation.OpenModelicaExecuter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class SimulationController implements Initializable
{
    @Autowired private OpenModelicaExecuter openModelicaExecuter;
    
    @FXML private Button simControlsStart;
    @FXML private Button simControlsStop;
    
    @FXML private MenuItem simMenuControlsStart;
    @FXML private MenuItem simMenuControlsStop;
    
    @FXML private ProgressBar simControlsProgress;
    
    @FXML private TextField simControlsStopTime;
    @FXML private TextField simControlsIntervals;
    @FXML private ChoiceBox simControlsIntegrator;
    
    @FXML
    public void StartSimulation() {
        
        simControlsStart.setDisable(true);
        simMenuControlsStart.setDisable(true);
        
        boolean success = openModelicaExecuter.simulate();
        
        if (success) {
            simControlsStop.setDisable(false);
            simMenuControlsStop.setDisable(false);
        } else {
            simControlsStart.setDisable(false);
            simMenuControlsStart.setDisable(false);
        }
    }
    
    @FXML
    public void StopSimulation() {
        
        simControlsStop.setDisable(true);
        simMenuControlsStop.setDisable(true);
        
        openModelicaExecuter.terminate();
        
        simControlsStart.setDisable(false);
        simMenuControlsStart.setDisable(false);
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
    
    public String getSimulationName() {
        return "Untitled";
    }
    
    public double getStopTime() throws NumberFormatException {
        return Double.parseDouble(simControlsStopTime.getText());
    }
    
    public int getIntervals() throws NumberFormatException {
        return Integer.parseInt(simControlsIntervals.getText());
    }
    
    public String getIntegrator() {
        return simControlsIntegrator.getSelectionModel().getSelectedItem().toString();
    }
}
