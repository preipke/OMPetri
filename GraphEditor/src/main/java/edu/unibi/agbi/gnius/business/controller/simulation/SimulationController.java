/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.simulation;

import edu.unibi.agbi.gnius.core.service.SimulationService;
import edu.unibi.agbi.gnius.core.service.exception.SimulationServiceException;
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
    @Autowired private SimulationService simulationService;
    
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
        
        try {
            simulationService.StartSimulation();
            
            simControlsStop.setDisable(false);
            simMenuControlsStop.setDisable(false);
            
        } catch (SimulationServiceException ex) {
            
            System.out.println(ex);
            
            simControlsStart.setDisable(false);
            simMenuControlsStart.setDisable(false);
        }
    }
    
    @FXML
    public void StopSimulation() {
        
        simControlsStop.setDisable(true);
        simMenuControlsStop.setDisable(true);
        
//        simulationService.terminate();
        
        simControlsStart.setDisable(false);
        simMenuControlsStart.setDisable(false);
    }
    
    public String getSimulationAuthor() {
        return "Unknown";
    }
    
    public String getSimulationName() {
        return "Untitled";
    }
    
    public String getSimulationDescription() {
        return "Not available";
    }
    
    public double getSimulationStopTime() throws NumberFormatException {
        return Double.parseDouble(simControlsStopTime.getText());
    }
    
    public int getSimulationIntervals() throws NumberFormatException {
        return Integer.parseInt(simControlsIntervals.getText());
    }
    
    public String getSimulationIntegrator() {
        return simControlsIntegrator.getSelectionModel().getSelectedItem().toString();
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
}
