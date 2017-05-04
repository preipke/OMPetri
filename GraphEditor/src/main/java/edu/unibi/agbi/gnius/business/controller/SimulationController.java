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
    @Autowired private ResultsController resultsController;
    
    @FXML private Button buttonOpenResultsViewer;
    @FXML private Button buttonSimStart;
    @FXML private Button buttonSimPause;
    @FXML private Button buttonSimStop;
    
    @FXML private TextField inputSimStartTime;
    @FXML private TextField inputSimStopTime;
    @FXML private TextField inputSimIntervals;
    @FXML private ChoiceBox choicesSimIntegrator;
    
    @FXML private ProgressBar simProgress;
    
    @Value("#{'${simulation.integrators}'.split(',')}") private List<String> simIntegratorsList;
    @Value("${simulation.value.intervals}") private String simIntervals;
    @Value("${simulation.value.start}") private String simStartTime;
    @Value("${simulation.value.stop}") private String simStopTime;
    
    public String getSimulationAuthorName() {
        return "Unknown";
    }
    
    public String getSimulationName() {
        return "Untitled";
    }
    
    public String getSimulationDescription() {
        return "Not available";
    }
    
    public double getSimulationStopTime() throws NumberFormatException {
        return Double.parseDouble(inputSimStopTime.getText());
    }
    
    public int getSimulationIntervals() throws NumberFormatException {
        return Integer.parseInt(inputSimIntervals.getText());
    }
    
    public String getSimulationIntegrator() {
        return choicesSimIntegrator.getSelectionModel().getSelectedItem().toString();
    }
    
    public void setSimulationProgress(double progress) {
        simProgress.setProgress(progress);
    }
    
    public void StopSimulation() {
        
        buttonSimStop.setDisable(true);
        buttonSimPause.setDisable(true);
        simProgress.setProgress(1);

        simulationService.StopSimulation();
        
        buttonSimStart.setDisable(false);
        inputSimStopTime.setDisable(false);
        inputSimIntervals.setDisable(false);
        choicesSimIntegrator.setDisable(false);
    }
    
    private void PauseResumeSimulation() {
        
    }
    
    private void StartSimulation() {
        
        simProgress.setVisible(true);
        simProgress.setProgress(0);
        
        buttonSimStart.setDisable(true);
        
        inputSimStopTime.setDisable(true);
        inputSimIntervals.setDisable(true);
        choicesSimIntegrator.setDisable(true);
        
        try {
            simulationService.StartSimulation();
            buttonSimPause.setDisable(false);
            buttonSimStop.setDisable(false);
        } catch (SimulationServiceException ex) {
            messengerService.addToLog(ex);
            StopSimulation();
        }
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        buttonOpenResultsViewer.setOnAction(e -> resultsController.ShowWindow());
        
        buttonSimStart.setOnAction(e -> StartSimulation());
        buttonSimPause.setOnAction(e -> PauseResumeSimulation());
        buttonSimPause.setDisable(true);
        buttonSimStop.setOnAction(e -> StopSimulation());
        buttonSimStop.setDisable(true);
        
        inputSimStartTime.setText(simStartTime);
        inputSimStopTime.setText(simStopTime);
        inputSimIntervals.setText(simIntervals);
        
        choicesSimIntegrator.getItems().clear();
        for (String integrator : simIntegratorsList) {
            choicesSimIntegrator.getItems().add(integrator);
        }
        choicesSimIntegrator.getSelectionModel().select(0);
        
        simProgress.setVisible(true);
    }
}
