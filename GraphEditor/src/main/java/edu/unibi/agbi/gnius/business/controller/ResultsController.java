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
import edu.unibi.agbi.gnius.core.io.XmlResultsConverter;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
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
    @Autowired private MessengerService messengerService;
    @Autowired private ResultsService resultsService;
    @Autowired private XmlResultsConverter xmlResultsConverter;

    @FXML private ChoiceBox<Simulation> simulationChoices;
    @FXML private ChoiceBox<IElement> elementChoices;
    @FXML private ChoiceBox<ValueChoice> valueChoices;
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
    
    @Value("${regex.valuechoice.fire}") private String valueChoiceFire;
    @Value("${regex.valuechoice.speed}") private String valueChoiceSpeed;
    @Value("${regex.valuechoice.token}") private String valueChoiceToken;
    @Value("${regex.valuechoice.tokenIn.actual}") private String valueChoiceTokenInActual;
    @Value("${regex.valuechoice.tokenIn.total}") private String valueChoiceTokenInTotal;
    @Value("${regex.valuechoice.tokenOut.actual}") private String valueChoiceTokenOutActual;
    @Value("${regex.valuechoice.tokenOut.total}") private String valueChoiceTokenOutTotal;

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

        ObservableList<Simulation> choices = FXCollections.observableArrayList();
        for (Simulation simulation : resultsService.getSimulations()) {
            choices.add(simulation);
        }

        simulationChoices.setItems(choices);
        simulationChoices.getSelectionModel().select(index);

        FilterChoices(simulationChoices, simulationFilterInput.getText());
    }

    private synchronized void RefreshElementChoices() {

        Simulation simulationChoice = simulationChoices.getSelectionModel().getSelectedItem();
        if (simulationChoice != null) {

            int index = elementChoices.getSelectionModel().getSelectedIndex();
            int oldSize = elementChoices.getItems().size();
            
            Set<IElement> elements = simulationChoice.getElementFilterReferences().keySet();
            ObservableList<IElement> choices = FXCollections.observableArrayList();
            for (IElement element : elements) {
                if (element.getElementType() == Element.Type.ARC) {
                    continue;
                }
                choices.add(element);
            }
            choices.sort(Comparator.comparing(IElement::toString));

            elementChoices.setItems(choices);
            if (oldSize == choices.size()) {
                elementChoices.getSelectionModel().select(index);
            }
        }
        FilterChoices(elementChoices, elementFilterInput.getText());
    }

    private synchronized void RefreshValueChoices() {

        Simulation simulationChoice = simulationChoices.getSelectionModel().getSelectedItem();
        IElement elementChoice = elementChoices.getSelectionModel().getSelectedItem();
        if (simulationChoice != null && elementChoice != null) {

            int index = valueChoices.getSelectionModel().getSelectedIndex();
            int oldSize = valueChoices.getItems().size();

            List<String> values = simulationChoice.getElementFilterReferences().get(elementChoice);
            ObservableList<ValueChoice> choices = FXCollections.observableArrayList();
            ValueChoice choice;
            String name;

            for (String value : values) {
                name = getValueName(value, simulationChoice);
                if (name == null) {
                    messengerService.addToLog("Cannot find choice for undefined value! ['" + simulationChoice.toString() + "', '" + elementChoice.toString() + "', '" + value + "']");
                    continue;
                }
                choice = new ValueChoice(name, value);
                choices.add(choice);
            }
            choices.sort(Comparator.comparing(ValueChoice::toString));

            valueChoices.setItems(choices);
            if (oldSize == choices.size()) {
                valueChoices.getSelectionModel().select(index);
            }
        }
        FilterChoices(valueChoices, valueFilterInput.getText());
    }

    private void addSelectedChoiceToChart() {

        Simulation simulationChoice = simulationChoices.getSelectionModel().getSelectedItem();
        IElement elementChoice = elementChoices.getSelectionModel().getSelectedItem();
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

        SimulationData data = new SimulationData(simulationChoice, elementChoice, valueChoice.getValue());

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
        statusMessageLabel.setText("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + msg);
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
            xmlResultsConverter.exportXml(file, resultsService.getChartData(lineChart));
            setStatus("Data successfully exported!", false);
        } catch (Exception ex) {
            setStatus("Export to XML file failed! [" + ex.getMessage() + "]", true);
        }
    }

    private void importData() {
        System.out.println("TODO!");
    }
    
    private String getValueName(String value, Simulation simulation) {
        if (value.matches(valueChoiceFire)) {
            return "Firing";
        } else if (value.matches(valueChoiceSpeed)) {
            return "Speed";
        } else if (value.matches(valueChoiceToken)) {
            return "Token";
        } else if (value.matches(valueChoiceTokenInActual)) {
            return "Incoming from "
                    + ((DataArc) simulation.getFilterElementReferences().get(value))
                            .getSource().toString()
                    + " [ACTUAL]";
        } else if (value.matches(valueChoiceTokenInTotal)) {
            return "Incoming from "
                    + ((DataArc) simulation.getFilterElementReferences().get(value))
                            .getSource().toString()
                    + " [TOTAL]";
        } else if (value.matches(valueChoiceTokenOutActual)) {
            return "Outgoing to "
                    + ((DataArc) simulation.getFilterElementReferences().get(value))
                            .getTarget().toString()
                    + " [ACTUAL]";
        } else if (value.matches(valueChoiceTokenOutTotal)) {
            return "Outgoing to "
                    + ((DataArc) simulation.getFilterElementReferences().get(value))
                            .getTarget().toString()
                    + " [TOTAL]";
        } else {
            return null;
        }
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

        simulationChoices.valueProperty().addListener(cl -> RefreshElementChoices());
        elementFilterInput.setOnKeyReleased(e -> {
            RefreshElementChoices();
            simulationChoices.hide();
            elementChoices.show();
            valueChoices.hide();
        });

        elementChoices.valueProperty().addListener(cl -> RefreshValueChoices());
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

        columnSimulation.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().toString()));
        columnElementId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElementId()));
        columnElementName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElementName()));
        columnValueName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(getValueName(cellData.getValue().getVariable(), cellData.getValue().getSimulation())));
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
        private final String name;
        private final String value;

        private ValueChoice(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
