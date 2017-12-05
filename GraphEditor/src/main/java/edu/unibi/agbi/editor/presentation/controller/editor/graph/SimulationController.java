/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.graph;

import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.core.util.GuiFactory;
import edu.unibi.agbi.editor.business.service.SimulationService;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
    @Autowired private GuiFactory guiFactory;
    @Autowired private MessengerService messengerService;
    @Autowired private SimulationService simulationService;
    
    @FXML private Button buttonOpenResultsViewer;
    @FXML private Button buttonStart;
    @FXML private Button buttonStop;
    
    @FXML private TextField inputStartTime;
    @FXML private TextField inputStopTime;
    @FXML private TextField inputIntervals;
    @FXML private ChoiceBox choicesIntegrator;
    @FXML private TextField inputCompilerArgs;
    @FXML private TextField inputSimulationArgs;
    
    @FXML private ProgressBar progressBar;
    @FXML private Label progressStatus;
    
    @Value("#{'${simulation.integrators}'.split(',')}") private List<String> simIntegratorsList;
    @Value("${simulation.default.intervals}") private String simIntervals;
    @Value("${simulation.default.start}") private String simStartTime;
    @Value("${simulation.default.stop}") private String simStopTime;
    
    public double getSimulationStopTime() throws NumberFormatException {
        return Double.parseDouble(inputStopTime.getText());
    }
    
    public int getSimulationIntervals() throws NumberFormatException {
        return Integer.parseInt(inputIntervals.getText());
    }
    
    public String getSimulationIntegrator() {
        return choicesIntegrator.getSelectionModel().getSelectedItem().toString();
    }
    
    public String getCompilerArgs() {
        return inputCompilerArgs.getText();
    }
    
    public String getSimulationArgs() {
        return inputSimulationArgs.getText();
    }
    
    public void setSimulationProgress(double progress) {
        this.progressBar.setProgress(progress);
    }
    
    public void setSimulationStatus(String txt) {
        this.progressStatus.setText(txt);
    }
    
    public void StopSimulation() {
        buttonStop.setDisable(true);
        simulationService.StopSimulation();
    }
    
    public void Unlock() {
        progressBar.setProgress(1);
        buttonStop.setDisable(true);
        buttonStart.setDisable(false);
        inputStopTime.setDisable(false);
        inputIntervals.setDisable(false);
        choicesIntegrator.setDisable(false);
    }
    
    private void StartSimulationAndLock() {
        progressBar.setProgress(0);
        progressStatus.setText("Preparing...");
        buttonStart.setDisable(true);
        inputStopTime.setDisable(true);
        inputIntervals.setDisable(true);
        choicesIntegrator.setDisable(true);
        simulationService.StartSimulation();
        buttonStop.setDisable(false);
    }
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        buttonOpenResultsViewer.setOnAction(e -> {
            try {
                guiFactory.BuildResultsViewer();
            } catch (IOException ex) {
                messengerService.addException("Cannot open results viewer!", ex);
            }
        });
        
        buttonStart.setOnAction(e -> StartSimulationAndLock());
        buttonStop.setOnAction(e -> StopSimulation());
        buttonStop.setDisable(true);
        
        inputStartTime.setText(simStartTime);
        inputStopTime.setText(simStopTime);
        inputIntervals.setText(simIntervals);
        
        choicesIntegrator.getItems().clear();
        for (String integrator : simIntegratorsList) {
            choicesIntegrator.getItems().add(integrator);
        }
        choicesIntegrator.getSelectionModel().select(0);
        
        progressBar.setVisible(true);
    }
}
