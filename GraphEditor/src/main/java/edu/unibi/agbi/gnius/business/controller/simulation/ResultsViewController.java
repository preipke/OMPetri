/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.simulation;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.service.SimulationService;
import edu.unibi.agbi.petrinet.entity.IPN_Element;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author PR
 */
public class ResultsViewController implements Initializable
{
    private final DateTimeFormatter simulationChoiceNameFormatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
    
    private SimulationService simulationService;
    
    @FXML private ChoiceBox simulationChoiceBox;
    @FXML private ChoiceBox elementChoiceBox;
    @FXML private ChoiceBox valueChoiceBox;
    @FXML private ColorPicker colorPicker;
    
    @FXML private LineChart lineChart;
    
    private boolean isSimulationChanged;
    private boolean isElementChanged;
    
    private Map<Simulation, Map<IPN_Element, Map<String, XYChart.Series>>> lineChartData;
    
    @FXML
    public void SelectingSimulation() {
        if (simulationService.getSimulations().size() != simulationChoiceBox.getItems().size()) {
            RefreshSimulationChoices();
        }
    }
    
    @FXML
    public void SelectingElement() {
        if (isSimulationChanged) {
            RefreshElementChoices();
        }
    }
    
    @FXML
    public void SelectingValue() {
        if (isElementChanged) {
            RefreshValueChoices();
        }
    }
    
    @FXML
    public void AddSelectedToChart() {
        
        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        ElementChoice elementChoice = (ElementChoice) elementChoiceBox.getSelectionModel().getSelectedItem();
        ValueChoice valueChoice = (ValueChoice) valueChoiceBox.getSelectionModel().getSelectedItem();
        
        LineChartData data = new LineChartData(simulationChoice.getSimulation(), elementChoice.getElement(), valueChoice.getValue());
        Color color = colorPicker.getValue();
        
        addToChart(data, color);
    }
    
    private void addToChart(LineChartData data, Color color) {
        
        if (!addData(data)) {
            return;
        }
        
        Simulation simulation = data.getSimulation();
        List<Object>[] results = simulation.getResults();
        String[] variables = simulation.getVariables();
        String variableTarget = data.getValue();
        
        int index = 0;
        for (String variable : variables) {
            if (variable.matches(variableTarget)) {
                System.out.println("Variable: " + variableTarget);
                System.out.println("Index in data[]: " + index);
                break;
            }
            index++;
        }
        
        XYChart.Series serie = lineChartData.get(simulation).get(data.getElement()).get(data.getValue());
        serie.setName("My data for '" + index + "'");
        
        for (int i = 0; i < results.length; i++) {
            serie.getData().add(new XYChart.Data(
                    (Number)results[0].get(i) ,
                    (Number)results[index].get(i)
            ));
        }
        
        lineChart.getData().add(serie);
    }
    
    /**
     * Adds data to the storage if it is not already inside.
     * @param data
     * @return indicates wether data has been stored
     */
    private boolean addData(LineChartData data) {
        
        Simulation simulation = data.getSimulation();
        
        // Simulation not in map?
        if (!lineChartData.containsKey(simulation)) {
            
            Map<String,XYChart.Series> valueSet = new HashMap();
            valueSet.put(data.getValue(), new XYChart.Series());
            
            Map<IPN_Element,Map<String,XYChart.Series>> elementMap = new HashMap();
            elementMap.put(data.getElement(), valueSet);
            
            lineChartData.put(simulation, elementMap);
            
        } else {
            
            IPN_Element element = data.getElement();
            
            // Element not in map?
            if (!lineChartData.get(simulation).containsKey(element)) {
                
                Map<String,XYChart.Series> valueSet = new HashMap();
                valueSet.put(data.getValue(), new XYChart.Series());
                
                lineChartData.get(simulation).put(element , valueSet);
                
            } else {
                
                String value = data.getValue();
                
                // Value not in map?
                if (!lineChartData.get(simulation).get(element).containsKey(value)) {
                    
                    lineChartData.get(simulation).get(element).put(value, new XYChart.Series());
                    
                } else {
                    
                    return false;
                    
                }
                
            }
            
        }
        
        return true;
    }
    
    public void RefreshSimulationChoices() {
        
        List<Simulation> simulations = simulationService.getSimulations();
        
        ObservableList<Object> simulationChoices = FXCollections.observableArrayList();
        for (Simulation simulation : simulations) {
            simulationChoices.add(new SimulationChoice(simulation));
        }
        
        simulationChoiceBox.setItems(simulationChoices);
        
        isSimulationChanged = true;
    }
    
    public void RefreshElementChoices() {
        
        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        Collection<IPN_Element> elements = simulationChoice.getSimulation().getVariableReferences().values();
        
        ObservableList<Object> elementChoices = FXCollections.observableArrayList();
        for (IPN_Element element : elements) {
            
            if (element.getElementType() == PN_Element.Type.ARC) {
                continue;
            }
            
            elementChoices.add(new ElementChoice(element));
        }
        
        elementChoiceBox.setItems(elementChoices);
        
        isSimulationChanged = false;
        isElementChanged = true;
    }
    
    public void RefreshValueChoices() {
        
        ElementChoice elementChoice = (ElementChoice) elementChoiceBox.getSelectionModel().getSelectedItem();
        List<String> values = elementChoice.getElement().getFilterNames();
        
        ObservableList<Object> valueChoices = FXCollections.observableArrayList();
        for (String value : values) {
            valueChoices.add(new ValueChoice(value));
        }
        
        valueChoiceBox.setItems(valueChoices);
        
        isElementChanged = false;
    }
    
    public void setSimulationService(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
    
    private class LineChartData
    {
        private final Simulation simulation;
        private final IPN_Element element;
        private final String value;
        
        private LineChartData(Simulation simulation, IPN_Element element, String value) {
            this.simulation = simulation;
            this.element = element;
            this.value = value;
        }
        
        private Simulation getSimulation() {
            return simulation;
        }
        
        private IPN_Element getElement() {
            return element;
        }
        
        private String getValue() {
            return value;
        }
        
        @Override
        public boolean equals(Object object) {
            
            if (object == null) {
                return false;
            }
            
            if (!(object instanceof LineChartData)) {
                return false;
            }
            
            LineChartData data = (LineChartData) object;
            
            if (!data.getSimulation().equals(simulation)) {
                return false;
            }
            if (!data.getElement().getId().matches(element.getId())) {
                return false;
            }
            
            return data.getValue().matches(value);
        }
    }
    
    private class ValueChoice
    {
        private final String value;
        
        private ValueChoice(String value) {
            this.value = value;
        }
        
        private String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    private class ElementChoice
    {
        private final IPN_Element element;
        
        private ElementChoice(IPN_Element element) {
            this.element = element;
        }
        
        private IPN_Element getElement() {
            return element;
        }
        
        @Override
        public String toString() {
            return "(" + element.getElementType().toString() + ") " + element.getId();
        }
    }

    private class SimulationChoice
    {
        private final Simulation simulation;

        private SimulationChoice(Simulation simulation) {
            this.simulation = simulation;
        }

        private Simulation getSimulation() {
            return simulation;
        }

        @Override
        public String toString() {
            return simulation.getTime().format(simulationChoiceNameFormatter) + " " + simulation.getName();
        }
    }
}
