/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.simulation.Simulation;
import edu.unibi.agbi.gnius.core.model.entity.simulation.SimulationData;
import edu.unibi.agbi.gnius.core.service.ResultsService;
import edu.unibi.agbi.gnius.core.exception.ResultsServiceException;
import edu.unibi.agbi.gnius.util.XmlExporter;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ResultsController implements Initializable
{
    @Autowired private ResultsService resultsService;
    @Autowired private XmlExporter xmlExporter;

    @FXML private ChoiceBox simulationChoices;
    @FXML private ChoiceBox elementChoices;
    @FXML private ChoiceBox valueChoices;
    @FXML private TextField simulationFilterInput;
    @FXML private TextField elementFilterInput;
    @FXML private TextField valueFilterInput;
    @FXML private Button buttonAddChoice;
    @FXML private Button buttonClearFilter;
    @FXML private Label statusMessageLabel;
    
    @FXML private Button buttonExportData;
    @FXML private Button buttonImportData;

    @FXML private TableView<SimulationData> tableView;
    @FXML private TableColumn<SimulationData, String> columnSimulation;
    @FXML private TableColumn<SimulationData, String> columnElementId;
    @FXML private TableColumn<SimulationData, String> columnElementName;
    @FXML private TableColumn<SimulationData, String> columnValueName;
    @FXML private TableColumn<SimulationData, String> columnValueStart;
    @FXML private TableColumn<SimulationData, String> columnValueEnd;
    @FXML private TableColumn<SimulationData, String> columnValueMin;
    @FXML private TableColumn<SimulationData, String> columnValueMax;
    @FXML private TableColumn<SimulationData, CheckBox> columnEnable;
    @FXML private TableColumn<SimulationData, Button> columnDrop;
    
    @FXML private LineChart lineChart;
    @FXML private TextField inputChartTitle;
    @FXML private TextField inputChartLabelX;
    @FXML private TextField inputChartLabelY;

    @Value("${results.tableview.decimalplaces}") private Integer decimalPlaces;
    @Value("${results.linechart.default.title}") private String defaultTitle;
    @Value("${results.linechart.default.xlabel}") private String defaultXLabel;
    @Value("${results.linechart.default.ylabel}") private String defaultYLabel;
    
    @Value("${format.datetime}") private String formatDateTime;
    @Value("${format.time}") private String formatTime;
    
    private DateTimeFormatter formatSimulationDateTime;
    private DateTimeFormatter formatStatusTime;
    private Stage stage;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void ShowWindow() {
        stage.show();
        stage.setIconified(false);
        stage.toFront();
    }

    public synchronized void RefreshSimulationChoices() {

        int index = simulationChoices.getSelectionModel().getSelectedIndex();

        ObservableList<Object> choices = FXCollections.observableArrayList();
        for (Simulation simulation : resultsService.getSimulations()) {
            choices.add(new SimulationChoice(simulation));
        }

        simulationChoices.setItems(choices);
        simulationChoices.getSelectionModel().select(index);

        FilterChoices(simulationChoices, simulationFilterInput.getText());
    }

    private synchronized void RefreshElementChoices() {

        SimulationChoice simulationChoice = (SimulationChoice) simulationChoices.getSelectionModel().getSelectedItem();
        if (simulationChoice != null) {

            int index = elementChoices.getSelectionModel().getSelectedIndex();
            int oldSize = elementChoices.getItems().size();

            Set<IElement> elements = simulationChoice.getSimulation().getElementFilterReferences().keySet();
            ObservableList<Object> choices = FXCollections.observableArrayList();
            for (IElement element : elements) {
                if (element.getElementType() == Element.Type.ARC) {
                    continue;
                }
                choices.add(new ElementChoice(element));
            }

            elementChoices.setItems(choices);
            if (oldSize == choices.size()) {
                elementChoices.getSelectionModel().select(index);
            }
        }
        FilterChoices(elementChoices, elementFilterInput.getText());
    }

    private synchronized void RefreshValueChoices() {

        SimulationChoice simulationChoice = (SimulationChoice) simulationChoices.getSelectionModel().getSelectedItem();
        ElementChoice elementChoice = (ElementChoice) elementChoices.getSelectionModel().getSelectedItem();
        if (simulationChoice != null && elementChoice != null) {

            int index = valueChoices.getSelectionModel().getSelectedIndex();
            int oldSize = valueChoices.getItems().size();

            List<String> values = simulationChoice.getSimulation().getElementFilterReferences().get(elementChoice.getElement());
            ObservableList<Object> choices = FXCollections.observableArrayList();
            for (String value : values) {
                choices.add(new ValueChoice(value));
            }

            valueChoices.setItems(choices);
            if (oldSize == choices.size()) {
                valueChoices.getSelectionModel().select(index);
            }
        }
        FilterChoices(valueChoices, valueFilterInput.getText());
    }

    private void addSelectedChoiceToChart() {

        SimulationChoice simulationChoice = (SimulationChoice) simulationChoices.getSelectionModel().getSelectedItem();
        ElementChoice elementChoice = (ElementChoice) elementChoices.getSelectionModel().getSelectedItem();
        ValueChoice valueChoice = (ValueChoice) valueChoices.getSelectionModel().getSelectedItem();
        
        if (simulationChoice == null) {
            setStatus("Select a simulation before adding!", true);
            return;
        } else if (elementChoice == null) {
            setStatus("Select an element before adding!", true);
            return;
        } else if (valueChoice == null) {
            setStatus("Select a value before adding!", true);
            return;
        }
        
        SimulationData data = new SimulationData(simulationChoice.getSimulation(), elementChoice.getElement(), valueChoice.getValue());

        if (resultsService.add(lineChart, data)) {
            resultsService.UpdateSeries(data);
            resultsService.show(lineChart, data);
            setStatus("The selected data has been added!", false);
        } else {
            setStatus("The selected data has already been added! Please check the table below.", true);
        }
    }
    
    private void clearFilterInputs() {
        simulationFilterInput.clear();
        elementFilterInput.clear();
        valueFilterInput.clear();
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

        text = text.toLowerCase();
        
        for (Object choice : choices) {
            if (choice.toString().toLowerCase().contains(text)) {
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
    
    private void setStatus(String msg, boolean isError) {
        statusMessageLabel.setText("[" + LocalDateTime.now().format(formatStatusTime) + "] " + msg);
        if (isError) {
            statusMessageLabel.setTextFill(Color.RED);
        } else {
            statusMessageLabel.setTextFill(Color.GREEN);
        }
    }
    
    private void exportData() {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save selected data to XML file");
        
        File file = fileChooser.showSaveDialog(stage);
        
        try {
            xmlExporter.exportXml(file, resultsService.getChartData(lineChart));
            setStatus("Data successfully exported!", false);
        } catch (Exception ex) {
            setStatus("Export to XML file failed! [" + ex.getMessage() + "]", true);
        }
    }
    
    private void importData() {
        System.out.println("TODO!");
    }
    
    private String getStartValueString(List<Data> data) {
        return String.valueOf(
                round(
                parseDouble(
                        data.get(0).getYValue()
                )));
    }
    
    private String getEndValueString(List<Data> data) {
        return String.valueOf(
                round(
                parseDouble(
                        data.get(data.size() - 1).getYValue()
                )));
    }
    
    private String getMinValueString(List<Data> data) {
        double value, min = parseDouble(data.get(0).getYValue());
        for (Data d : data) {
            value = parseDouble(d.getYValue());
            if (value < min) {
                min = value;
            }
        }
        return String.valueOf(round(min));
    }
    
    private String getMaxValueString(List<Data> data) {
        double value, max = parseDouble(data.get(0).getYValue());
        for (Data d : data) {
            value = parseDouble(d.getYValue());
            if (value > max) {
                max = value;
            }
        }
        return String.valueOf(round(max));
    }
    
    private double parseDouble(Object o) {
        return Double.parseDouble(o.toString());
    }
    
    private double round(double value) {
        for (int i = 0; i < decimalPlaces; i++) {
            value = value * 10;
        }
        value = Math.floor(value);
        for (int i = 0; i < decimalPlaces; i++) {
            value = value / 10;
        }
        return value;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        formatSimulationDateTime = DateTimeFormatter.ofPattern(formatDateTime);
        formatStatusTime = DateTimeFormatter.ofPattern(formatTime);

        /**
         * Data selection and filtering.
         */
        resultsService.getSimulations().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                RefreshSimulationChoices();
                setStatus("New simulation(s) added!", false);
            }
        });
        simulationFilterInput.setOnKeyReleased(e -> {
            RefreshSimulationChoices();
            simulationChoices.show();
            elementChoices.hide();
            valueChoices.hide();
        });

        simulationChoices.valueProperty().addListener((ObservableValue obs, Object o, Object n) -> RefreshElementChoices());
        elementFilterInput.setOnKeyReleased(e -> {
            RefreshElementChoices();
            simulationChoices.hide();
            elementChoices.show();
            valueChoices.hide();
        });

        elementChoices.valueProperty().addListener((ObservableValue obs, Object o, Object n) -> RefreshValueChoices());
        valueFilterInput.setOnKeyReleased(e -> {
            RefreshValueChoices();
            simulationChoices.hide();
            elementChoices.hide();
            valueChoices.show();
        });
        
        buttonClearFilter.setOnAction(e -> clearFilterInputs());
        buttonAddChoice.setOnAction(e -> addSelectedChoiceToChart());
        
        /**
         * Import export buttons.
         */
        buttonImportData.setOnAction(e -> importData());
        buttonExportData.setOnAction(e -> exportData());

        /**
         * LineChart.
         */
        lineChart.createSymbolsProperty().set(false);
        lineChart.titleProperty().bind(inputChartTitle.textProperty());
        lineChart.getXAxis().labelProperty().bind(inputChartLabelX.textProperty());
        lineChart.getYAxis().labelProperty().bind(inputChartLabelY.textProperty());
        inputChartTitle.setText(defaultTitle);
        inputChartLabelX.setText(defaultXLabel);
        inputChartLabelY.setText(defaultYLabel);

        /**
         * TableView.
         */
        Callback<TableColumn<SimulationData, Button>, TableCell<SimulationData, Button>> columnDropCellFactory;
        Callback<TableColumn<SimulationData, CheckBox>, TableCell<SimulationData, CheckBox>> columnEnableCellFactory;
        
        tableView.setItems(FXCollections.observableArrayList());
        try {
            resultsService.add(lineChart, tableView.getItems());
        } catch (ResultsServiceException ex) {
            System.out.println(ex.getMessage());
        }

        columnSimulation.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().getDateTime().format(formatSimulationDateTime) + " " + cellData.getValue().getSimulation().getModelName()));
        columnElementId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElementId()));
        columnElementName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElementName()));
        columnValueName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getVariable()));
        columnValueStart.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getStartValueString(cellData.getValue().getSeries().getData())));
        columnValueEnd.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getEndValueString(cellData.getValue().getSeries().getData())));
        columnValueMin.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getMinValueString(cellData.getValue().getSeries().getData())));
        columnValueMax.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getMaxValueString(cellData.getValue().getSeries().getData())));
        
        columnEnable.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setSelected(cellData.getValue().isShown());
            cb.selectedProperty().addListener(e -> {
                // wait for animations to finish or LineChart breaks if data is added/removed too fast
                if ((System.currentTimeMillis() - cellData.getValue().timeMilliSecondLastStatusChange()) < 1000) {
                    if (cellData.getValue().isShown()) {
                        if (!cb.isSelected()) {
                            cb.setSelected(true);
                        }
                    } else {
                        if (cb.isSelected()) {
                            cb.setSelected(false);
                        }
                    }
                    return;
                }
                cellData.getValue().updateMilliSecondLastStatusChange();
                if (cb.selectedProperty().getValue()) {
                    resultsService.UpdateSeries(cellData.getValue());
                    resultsService.show(lineChart, cellData.getValue());
                } else {
                    resultsService.hide(lineChart, cellData.getValue());
                }
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        columnEnableCellFactory = columnEnable.getCellFactory();
        columnEnable.setCellFactory(c -> {
            TableCell cell = columnEnableCellFactory.call(c);
            Tooltip tooltip = new Tooltip("Disables or enables data in the chart");
            cell.setTooltip(tooltip);
            return cell;
        });
        columnDrop.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setAllowIndeterminate(true);
            cb.setIndeterminate(true);
            cb.setOnAction(e -> {
                resultsService.drop(lineChart, cellData.getValue());
                setStatus("Data has been dropped!", false);
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        columnDropCellFactory = columnDrop.getCellFactory();
        columnDrop.setCellFactory(c -> {
            TableCell cell = columnDropCellFactory.call(c);
            Tooltip tooltip = new Tooltip("Removes data from chart and table");
            cell.setTooltip(tooltip);
            return cell;
        });
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
            return simulation.getDateTime().format(formatSimulationDateTime) + " " + simulation.getModelName();
        }
    }
}
