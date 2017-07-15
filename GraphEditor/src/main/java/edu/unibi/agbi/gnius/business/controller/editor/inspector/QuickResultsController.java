/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.inspector;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.ResultsService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class QuickResultsController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private ResultsService resultsService;
    
    @FXML private MenuButton buttonMenuResults;
    @FXML private LineChart lineChartResults;
    
    private IDataElement data;
    
    public void setElement(IDataElement element) {
        
        data = element;
        
        setButtonMenuItems(element);
    }
    
    private void setButtonMenuItems(IDataElement element) {
        
        String modelId;
        List<Simulation> simulations;
        
        buttonMenuResults.getItems().clear();
        
        if (element == null) {
            return;
        }
        
        modelId = dataService.getDao().getModelId();
        simulations = new ArrayList();
        
        /**
         * Grab relevant simulations.
         */
        for (Simulation simulation : resultsService.getSimulations()) {
            if (simulation.getModelId().contentEquals(modelId)) { // check if model is the same
                if (simulation.getElementFilterReferences().get(element) != null) { // check if element is available
                    simulations.add(simulation);
                }
            }
        }
        
        /**
         * Create buttons.
         */
        
        simulations.forEach(simulation -> {
                
            CheckBox checkBox = new CheckBox();
            checkBox.setOnAction(cl -> {
                if (checkBox.isSelected()) {
                    addSimulationData(simulation, element);
                } else {
                    removeSimulationData(simulation, element);
                }
                cl.consume(); // keeps menu open
            });
            
            MenuItem item = new MenuItem();
            item.setText(simulation.toString());
            item.setGraphic(checkBox);
            
            buttonMenuResults.getItems().add(item);
        });
    }
    
    private void addSimulationData(Simulation simulation, IDataElement element) {
        System.out.print("Adding data");
    }
    
    private void removeSimulationData(Simulation simulation, IDataElement element) {
        System.out.print("Removing data");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
}
