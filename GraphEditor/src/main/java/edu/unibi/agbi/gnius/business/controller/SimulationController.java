/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SimulationService;
import edu.unibi.agbi.gnius.core.exception.SimulationServiceException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class SimulationController implements Initializable
{
    @Autowired private MessengerService messengerService;
    @Autowired private SimulationService simulationService;
    
    @FXML private Button buttonSimStart;
    @FXML private Button buttonSimPause;
    @FXML private Button buttonSimStop;
    
    @FXML private TextField tfSimStartTime;
    @FXML private TextField tfSimStopTime;
    @FXML private TextField tfSimIntervals;
    @FXML private ChoiceBox cbSimIntegrator;
    
    @FXML private ProgressBar simProgress;
    
    @Value("#{'${simulation.integrators}'.split(',')}") private List<String> simIntegratorsList;
    @Value("${simulation.intervals}") private String simIntervals;
    @Value("${simulation.time.start}") private String simStartTime;
    @Value("${simulation.time.stop}") private String simStopTime;
    
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
        return Double.parseDouble(tfSimStopTime.getText());
    }
    
    public int getSimulationIntervals() throws NumberFormatException {
        return Integer.parseInt(tfSimIntervals.getText());
    }
    
    public String getSimulationIntegrator() {
        return cbSimIntegrator.getSelectionModel().getSelectedItem().toString();
    }
    
    public void setSimulationProgress(double progress) {
        simProgress.setProgress(progress);
    }
    
    @FXML
    public void StartSimulation() {
        
        simProgress.setVisible(true);
        simProgress.setProgress(0);
        
        buttonSimStart.setDisable(true);
        
        tfSimStopTime.setDisable(true);
        tfSimIntervals.setDisable(true);
        cbSimIntegrator.setDisable(true);
        
        try {
            simulationService.StartSimulation();
            buttonSimPause.setDisable(false);
            buttonSimStop.setDisable(false);
        } catch (SimulationServiceException ex) {
            messengerService.addToLog(ex);
            StopSimulation();
        }
    }
    
    @FXML
    public void StopSimulation() {
        buttonSimStop.setDisable(true);
        buttonSimPause.setDisable(true);
//        simProgress.setProgress(1);
        simulationService.StopSimulation();
        buttonSimStart.setDisable(false);
        tfSimStopTime.setDisable(false);
        tfSimIntervals.setDisable(false);
        cbSimIntegrator.setDisable(false);
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        buttonSimStart.setOnAction(e -> StartSimulation());
        buttonSimStop.setOnAction(e -> StopSimulation());
        
        tfSimStartTime.setText(simStartTime);
        tfSimStopTime.setText(simStopTime);
        tfSimIntervals.setText(simIntervals);
        
        cbSimIntegrator.getItems().clear();
        for (String integrator : simIntegratorsList) {
            cbSimIntegrator.getItems().add(integrator);
        }
        cbSimIntegrator.getSelectionModel().select(0);
        
        simProgress.setVisible(true);
    }
}
