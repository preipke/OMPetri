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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class ResultsViewController implements Initializable
{
    @Autowired private SimulationService simulationService;
    
    @FXML private ChoiceBox simulationChoiceBox;
    @FXML private ChoiceBox elementChoiceBox;
    @FXML private ChoiceBox valueChoiceBox;
    
    @FXML private TableView tableView;
    @FXML private TableColumn<LineChartData,String> columnSimulation;
    @FXML private TableColumn<LineChartData,String> columnElementType;
    @FXML private TableColumn<LineChartData,String> columnElementId;
    @FXML private TableColumn<LineChartData,String> columnValue;
    @FXML private TableColumn<LineChartData,CheckBox> columnEnable;
    @FXML private TableColumn<LineChartData,Button> columnDrop;
    
    @FXML private LineChart lineChart;
    
    private final DateTimeFormatter simulationChoiceNameFormatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
    
    private final Map<Simulation, Map<IPN_Element, Map<String, XYChart.Series>>> lineChartDataMap;
    private final ObservableList<LineChartData> lineChartDataList;
    
    public ResultsViewController() {
        lineChartDataMap = new HashMap();
        lineChartDataList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        System.out.println("Initialized results view controller!");
        
        lineChart.createSymbolsProperty().set(false);
        
        simulationService.getSimulations().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                RefreshSimulationChoices();
            }
        });
        
        simulationChoiceBox.valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue != null) {
                RefreshElementChoices();
            }
        });

        elementChoiceBox.valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue != null) {
                RefreshValueChoices();
            }
        });
        
        columnSimulation.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().getName()));
        columnElementType.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElement().getElementType().toString()));
        columnElementId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElement().getId()));
        columnValue.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        columnEnable.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.selectedProperty().bindBidirectional(cellData.getValue().isEnabled());
            cb.selectedProperty().addListener(listener -> {
                if (cb.selectedProperty().getValue()) {
                    UpdateSeries(cellData.getValue());
                    PrintToLineChart(cellData.getValue());
                } else {
                    EraseFromLineChart(cellData.getValue());
                }
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        columnDrop.setCellValueFactory(cellData -> {
            Button btn = new Button();
            btn.setOnAction(event -> {
                deleteLineChartData(cellData.getValue());
            });
            return new ReadOnlyObjectWrapper(btn);
        });
        
        tableView.setItems(lineChartDataList);
    }
    
    @FXML
    public void AddSelectedToChart() {
        
        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        ElementChoice elementChoice = (ElementChoice) elementChoiceBox.getSelectionModel().getSelectedItem();
        ValueChoice valueChoice = (ValueChoice) valueChoiceBox.getSelectionModel().getSelectedItem();
        
        LineChartData data = new LineChartData(simulationChoice.getSimulation(), elementChoice.getElement(), valueChoice.getValue());
        
        if (!storeLineChartData(data)) {
            return;
        }
        
        UpdateSeries(data);
        PrintToLineChart(data);
    }
    
    /**
     * Prints the given data's series to the line chart.
     * @param data 
     */
    private void PrintToLineChart(LineChartData data) {
        if (!lineChart.getData().contains(data.getSeries())) {
            lineChart.getData().add(data.getSeries());
        }
    }
    
    /**
     * Updates the data's corresponding line chart series.
     * @param data 
     */
    private void UpdateSeries(LineChartData data) {
        
        Simulation simulation = data.getSimulation();
        String variableTarget = data.getValue();
        
        List<Object>[] results = simulation.getResults();
        String[] variables = simulation.getVariables();
        
        XYChart.Series series = data.getSeries();
        series.setName("'" + data.getElement().getId() + " '");
        
        int index = 0;
        for (String variable : variables) {
            if (variable.matches(variableTarget)) {
                System.out.println("Variable: " + variableTarget);
                System.out.println("Index in data[]: " + index);
                break;
            }
            index++;
        }
        
        if (results[index].size() > series.getData().size()) {
            for (int i = series.getData().size(); i < results[0].size(); i++) {
                series.getData().add(new XYChart.Data(
                        (Number) results[0].get(i),
                        (Number) results[index].get(i)
                ));
            }
        }
    }
    
    /**
     * Removes data from the line chart only.
     * @param data 
     */
    private void EraseFromLineChart(LineChartData data) {
        lineChart.getData().remove(data.getSeries());
    }
    
    /**
     * Stores data within the map and list if it is not already inside.
     * @param data
     * @return indicates wether data has been stored
     */
    private boolean storeLineChartData(LineChartData data) {
        
        Simulation simulation = data.getSimulation();
        
        // Simulation not in map?
        if (!lineChartDataMap.containsKey(simulation)) {
            
            Map<String,XYChart.Series> valueSerie = new HashMap();
            valueSerie.put(data.getValue(), data.getSeries());
            
            Map<IPN_Element,Map<String,XYChart.Series>> elementMap = new HashMap();
            elementMap.put(data.getElement(), valueSerie);
            
            lineChartDataMap.put(simulation, elementMap);
            
        } else {
            
            IPN_Element element = data.getElement();
            
            // Element not in map?
            if (!lineChartDataMap.get(simulation).containsKey(element)) {
                
                Map<String,XYChart.Series> valueSet = new HashMap();
                valueSet.put(data.getValue(), data.getSeries());
                
                lineChartDataMap.get(simulation).put(element , valueSet);
                
            } else {
                
                String value = data.getValue();
                
                // Value not in map?
                if (!lineChartDataMap.get(simulation).get(element).containsKey(value)) {
                    
                    lineChartDataMap.get(simulation).get(element).put(value, data.getSeries());
                    
                } else {
                    
                    return false;
                    
                }
                
            }
            
        }
        
        lineChartDataList.add(data);
        
        return true;
    }
    
    /**
     * Removes data from both the chart and the storage.
     * @param data 
     */
    private void deleteLineChartData(LineChartData data) {
        EraseFromLineChart(data);
        lineChartDataMap.get(data.getSimulation()).get(data.getElement()).remove(data.getValue());
        lineChartDataList.remove(data);
    }
    
    public void UpdateChoices() {
        RefreshSimulationChoices();
    }
    
    private synchronized void RefreshSimulationChoices() {
        
        int index;
        
        ObservableList<Object> simulationChoices = FXCollections.observableArrayList();
        for (Simulation simulation : simulationService.getSimulations()) {
            simulationChoices.add(new SimulationChoice(simulation));
        }
        
        index = simulationChoiceBox.getSelectionModel().getSelectedIndex();
        simulationChoiceBox.setItems(simulationChoices);
        simulationChoiceBox.getSelectionModel().select(index);
    }
    
    private synchronized void RefreshElementChoices() {
        
        int index, oldSize;
        
        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        Set<IPN_Element> elements = simulationChoice.getSimulation().getElementFilterReferences().keySet();
        
        ObservableList<Object> elementChoices = FXCollections.observableArrayList();
        for (IPN_Element element : elements) {
            
            if (element.getElementType() == PN_Element.Type.ARC) {
                continue;
            }
            
            elementChoices.add(new ElementChoice(element));
        }
        
        index = elementChoiceBox.getSelectionModel().getSelectedIndex();
        oldSize = elementChoiceBox.getItems().size();
        elementChoiceBox.setItems(elementChoices);
        if (oldSize == elementChoices.size()) {
            elementChoiceBox.getSelectionModel().select(index);
        }
    }
    
    private synchronized void RefreshValueChoices() {
        
        int index, oldSize;
        
        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        ElementChoice elementChoice = (ElementChoice) elementChoiceBox.getSelectionModel().getSelectedItem();
        List<String> values = simulationChoice.getSimulation().getElementFilterReferences().get(elementChoice.getElement());
        
        ObservableList<Object> valueChoices = FXCollections.observableArrayList();
        for (String value : values) {
            valueChoices.add(new ValueChoice(value));
        }
        
        index = valueChoiceBox.getSelectionModel().getSelectedIndex();
        oldSize = valueChoiceBox.getItems().size();
        valueChoiceBox.setItems(valueChoices);
        if (oldSize == valueChoices.size()) {
            valueChoiceBox.getSelectionModel().select(index);
        }
    }
    
    private class LineChartData
    {
        private final Simulation simulation;
        private final IPN_Element element;
        private final String value;
        private final XYChart.Series series;
        private final BooleanProperty enabled;
        
        private LineChartData(Simulation simulation, IPN_Element element, String value) {
            this.simulation = simulation;
            this.element = element;
            this.value = value;
            this.series = new XYChart.Series();
            this.enabled = new SimpleBooleanProperty(true);
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
        
        private XYChart.Series getSeries() {
            return series;
        }
        
        private BooleanProperty isEnabled() {
            return enabled;
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
