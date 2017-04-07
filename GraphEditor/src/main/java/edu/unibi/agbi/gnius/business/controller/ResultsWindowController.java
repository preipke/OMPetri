/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.service.SimulationService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class ResultsWindowController implements Initializable {

    @Autowired private SimulationService simulationService;

    @FXML private ChoiceBox simulationChoiceBox;
    @FXML private ChoiceBox elementChoiceBox;
    @FXML private ChoiceBox valueChoiceBox;
    @FXML private TextField simulationChoiceFilter;
    @FXML private TextField elementChoiceFilter;
    @FXML private TextField valueChoiceFilter;

    @FXML private TableView tableView;
    @FXML private TableColumn<LineChartData, String> columnSimulation;
    @FXML private TableColumn<LineChartData, String> columnElementId;
    @FXML private TableColumn<LineChartData, String> columnElementName;
    @FXML private TableColumn<LineChartData, String> columnValue;
    @FXML private TableColumn<LineChartData, CheckBox> columnEnable;
    @FXML private TableColumn<LineChartData, Button> columnDrop;
    
    @FXML private TextField xLabelInput;
    @FXML private TextField yLabelInput;

    @FXML private LineChart lineChart;
    
    @Value("${results.window.choice.dateformat}")
    private String dateFormat;
    @Value("${results.window.linechart.y.label}")
    private String xAxisLabel;
    @Value("${results.window.linechart.x.label}")
    private String yAxisLabel;
    private DateTimeFormatter simulationChoiceDateTimeFormatter;

    private final Map<Simulation, Map<IElement, Map<String, XYChart.Series>>> lineChartDataMap;
    private final ObservableList<LineChartData> lineChartDataList;

    public ResultsWindowController() {
        lineChartDataMap = new HashMap();
        lineChartDataList = FXCollections.observableArrayList();
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
        addToLineChart(data);
    }

    public synchronized void UpdateSimulationChoices() {

        int index = simulationChoiceBox.getSelectionModel().getSelectedIndex();

        ObservableList<Object> simulationChoices = FXCollections.observableArrayList();
        for (Simulation simulation : simulationService.getSimulations()) {
            simulationChoices.add(new SimulationChoice(simulation));
        }

        simulationChoiceBox.setItems(simulationChoices);
        simulationChoiceBox.getSelectionModel().select(index);

        FilterChoices(simulationChoiceBox, simulationChoiceFilter.getText());
    }

    private synchronized void UpdateElementChoices() {

        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        if (simulationChoice != null) {

            int index = elementChoiceBox.getSelectionModel().getSelectedIndex();
            int oldSize = elementChoiceBox.getItems().size();

            Set<IElement> elements = simulationChoice.getSimulation().getElementFilterReferences().keySet();
            ObservableList<Object> elementChoices = FXCollections.observableArrayList();
            for (IElement element : elements) {
                if (element.getElementType() == Element.Type.ARC) {
                    continue;
                }
                elementChoices.add(new ElementChoice(element));
            }

            elementChoiceBox.setItems(elementChoices);
            if (oldSize == elementChoices.size()) {
                elementChoiceBox.getSelectionModel().select(index);
            }
        }
        FilterChoices(elementChoiceBox, elementChoiceFilter.getText());
    }

    private synchronized void RefreshValueChoices() {

        SimulationChoice simulationChoice = (SimulationChoice) simulationChoiceBox.getSelectionModel().getSelectedItem();
        ElementChoice elementChoice = (ElementChoice) elementChoiceBox.getSelectionModel().getSelectedItem();
        if (simulationChoice != null && elementChoice != null) {

            int index = valueChoiceBox.getSelectionModel().getSelectedIndex();
            int oldSize = valueChoiceBox.getItems().size();

            List<String> values = simulationChoice.getSimulation().getElementFilterReferences().get(elementChoice.getElement());
            ObservableList<Object> valueChoices = FXCollections.observableArrayList();
            for (String value : values) {
                valueChoices.add(new ValueChoice(value));
            }

            valueChoiceBox.setItems(valueChoices);
            if (oldSize == valueChoices.size()) {
                valueChoiceBox.getSelectionModel().select(index);
            }
        }
        FilterChoices(valueChoiceBox, valueChoiceFilter.getText());
    }

    private void FilterChoices(ChoiceBox choiceBox, String text) {

        if (text == null || text.matches("")) {
            return;
        }
        
        Object selectedChoice = choiceBox.getSelectionModel().getSelectedItem();
        
        int selectedIndex = -1;
        int index = 0;
        
        ObservableList choices = choiceBox.getItems();
        ObservableList choicesFiltered = FXCollections.observableArrayList();
        
        for (Object choice : choices) {
            if (choice.toString().contains(text)) {
                choicesFiltered.add(choice);
                if (choice.equals(selectedChoice)) {
                    selectedIndex = index;
                }
                index++;
            }
        }
        
        choiceBox.setItems(choicesFiltered);
        if (selectedIndex != -1) {
            choiceBox.getSelectionModel().select(selectedIndex);
        }
    }

    /**
     * Prints the given data's series to the line chart.
     *
     * @param data
     */
    private synchronized void addToLineChart(LineChartData data) {
        if (!lineChart.getData().contains(data.getSeries())) {
            lineChart.getData().add(data.getSeries());
        }
    }

    /**
     * Removes data from the line chart.
     *
     * @param data
     */
    private synchronized void removeFromLineChart(LineChartData data) {
        lineChart.getData().remove(data.getSeries());
    }

    /**
     * Updates the series for the given data object. Loads data from the 
     * simulation and adds all additional entries to the series.
     *
     * @param data
     */
    private synchronized void UpdateSeries(LineChartData data) {

        Simulation simulation = data.getSimulation();
        String value = data.getValue();

        List<Object>[] results = simulation.getResults();
        String[] variables = simulation.getVariables();

        XYChart.Series series = data.getSeries();
        series.setName("'" + data.getElement().getId() + " '");

        int index = 0;
        for (String variable : variables) {
            if (variable.matches(value)) {
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
     * Stores data within the map and list if it is not already inside.
     *
     * @param data
     * @return indicates wether data has been stored
     */
    private synchronized boolean storeLineChartData(LineChartData data) {

        Simulation simulation = data.getSimulation();

        // Simulation not in map?
        if (!lineChartDataMap.containsKey(simulation)) {

            Map<String, XYChart.Series> valueSerie = new HashMap();
            valueSerie.put(data.getValue(), data.getSeries());

            Map<IElement, Map<String, XYChart.Series>> elementMap = new HashMap();
            elementMap.put(data.getElement(), valueSerie);

            lineChartDataMap.put(simulation, elementMap);

        } else {

            IElement element = data.getElement();

            // Element not in map?
            if (!lineChartDataMap.get(simulation).containsKey(element)) {

                Map<String, XYChart.Series> valueSet = new HashMap();
                valueSet.put(data.getValue(), data.getSeries());

                lineChartDataMap.get(simulation).put(element, valueSet);

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
     *
     * @param data
     */
    private void deleteLineChartData(LineChartData data) {
        removeFromLineChart(data);
        lineChartDataMap.get(data.getSimulation()).get(data.getElement()).remove(data.getValue());
        lineChartDataList.remove(data);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /**
         * LineChart specs.
         */
        lineChart.createSymbolsProperty().set(false);
        lineChart.getXAxis().labelProperty().bind(xLabelInput.textProperty());
        lineChart.getYAxis().labelProperty().bind(yLabelInput.textProperty());
        xLabelInput.setText(xAxisLabel);
        yLabelInput.setText(yAxisLabel);

        /**
         * ChoiceBox and TextField actions.
         */
        simulationChoiceDateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        simulationService.getSimulations().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                UpdateSimulationChoices();
            }
        });
        simulationChoiceFilter.setOnKeyReleased(e -> {
            UpdateSimulationChoices();
            simulationChoiceBox.show();
            elementChoiceBox.hide();
            valueChoiceBox.hide();
        });

        simulationChoiceBox.valueProperty().addListener((ObservableValue obs, Object o, Object n) -> UpdateElementChoices());
        elementChoiceFilter.setOnKeyReleased(e -> {
            UpdateElementChoices();
            simulationChoiceBox.hide();
            elementChoiceBox.show();
            valueChoiceBox.hide();
        });

        elementChoiceBox.valueProperty().addListener((ObservableValue obs, Object o, Object n) -> RefreshValueChoices());
        valueChoiceFilter.setOnKeyReleased(e -> {
            RefreshValueChoices();
            simulationChoiceBox.hide();
            elementChoiceBox.hide();
            valueChoiceBox.show();
        });

        /**
         * TableView specs.
         */
        tableView.setItems(lineChartDataList);
        
        columnSimulation.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().getTime().format(simulationChoiceDateTimeFormatter) + " " + cellData.getValue().getSimulation().getName()));
        columnElementId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElement().getId()));
        columnElementName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElement().getName()));
        columnValue.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        columnEnable.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setSelected(true);
            cb.selectedProperty().addListener(e -> {
                // wait for animations to finish or LineChart breaks if added/removed too fast
                if ((System.currentTimeMillis() - cellData.getValue().timeMilliSecondLastStatusChange()) < 1000) {
                    return;
                }
                cellData.getValue().updateMilliSecondLastStatusChange();
                if (cb.selectedProperty().getValue()) {
                    UpdateSeries(cellData.getValue());
                    addToLineChart(cellData.getValue());
                } else {
                    removeFromLineChart(cellData.getValue());
                }
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        columnDrop.setCellValueFactory(cellData -> {
            Button btn = new Button();
            btn.setOnAction(e -> deleteLineChartData(cellData.getValue()));
            return new ReadOnlyObjectWrapper(btn);
        });
    }

    /**
     * Additional data storage structure for the LineChart.
     */
    private class LineChartData {

        private final Simulation simulation;
        private final IElement element;
        private final String value;
        private final XYChart.Series series;
        private long timeLastStatusChange;

        private LineChartData(Simulation simulation, IElement element, String value) {
            this.simulation = simulation;
            this.element = element;
            this.value = value;
            this.series = new XYChart.Series();
        }

        private Simulation getSimulation() {
            return simulation;
        }

        private IElement getElement() {
            return element;
        }

        private String getValue() {
            return value;
        }

        private XYChart.Series getSeries() {
            return series;
        }
        
        private void updateMilliSecondLastStatusChange() {
            timeLastStatusChange = System.currentTimeMillis();
        }
        
        private long timeMilliSecondLastStatusChange() {
            return timeLastStatusChange;
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

    private class ValueChoice {
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

    private class ElementChoice {
        private final IElement element;
        private ElementChoice(IElement element) {
            this.element = element;
        }
        private IElement getElement() {
            return element;
        }
        @Override
        public String toString() {
            return "(" + element.getId() + ") " + element.getName();
        }
    }

    private class SimulationChoice {
        private final Simulation simulation;
        private SimulationChoice(Simulation simulation) {
            this.simulation = simulation;
        }
        private Simulation getSimulation() {
            return simulation;
        }
        @Override
        public String toString() {
            return simulation.getTime().format(simulationChoiceDateTimeFormatter) + " " + simulation.getName();
        }
    }
}
