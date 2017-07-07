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
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ResultsController implements Initializable
{
    private final String resultsFxml = "/fxml/Results.fxml";
    private final String resultsTitle = "Application - Results Viewer";
    private final String mainCss = "/styles/main.css";
    
    @Autowired private ApplicationContext applicationContext;
    @Autowired private MessengerService messengerService;
    @Autowired private ResultsService resultsService;
    @Autowired private XmlResultsConverter xmlResultsConverter;

    @FXML private ChoiceBox choicesModel;
    @FXML private ChoiceBox choicesSimulation;
    @FXML private ChoiceBox choicesElement;
    @FXML private ChoiceBox choicesValue;
    @FXML private TextField inputModelFilter;
    @FXML private TextField inputSimulationFilter;
    @FXML private TextField inputElementFilter;
    @FXML private TextField inputValueFilter;
    @FXML private Button buttonAddSelected;
    @FXML private Button buttonClearFilter;
    @FXML private CheckBox checkboxAutoAddSelected;
    @FXML private Label statusMessageLabel;

    @FXML private Button buttonExportData;
    @FXML private Button buttonImportData;
    @FXML private Button buttonEnableAll;
    @FXML private Button buttonDisableAll;
    @FXML private Button buttonDropAll;
    @FXML private Button buttonRefresh;

    @FXML private TableView<SimulationData> tableView;
    @FXML private TableColumn<SimulationData, CheckBox> columnAutoAdd;
    @FXML private TableColumn<SimulationData, String> columnDateTime;
    @FXML private TableColumn<SimulationData, String> columnModel;
    @FXML private TableColumn<SimulationData, String> columnElementId;
    @FXML private TableColumn<SimulationData, String> columnElementName;
    @FXML private TableColumn<SimulationData, String> columnValueName;
    @FXML private TableColumn<SimulationData, Number> columnValueStart;
    @FXML private TableColumn<SimulationData, Number> columnValueEnd;
    @FXML private TableColumn<SimulationData, Number> columnValueMin;
    @FXML private TableColumn<SimulationData, Number> columnValueMax;
    @FXML private TableColumn<SimulationData, CheckBox> columnEnable;
    @FXML private TableColumn<SimulationData, Button> columnDrop;

    @FXML private LineChart lineChart;
    @FXML private TextField inputChartTitle;
    @FXML private TextField inputChartLabelX;
    @FXML private TextField inputChartLabelY;
//    @FXML private TextField inputChartResolution;
//    @FXML private CheckBox checkboxChartAnimated;

    @Value("${results.tableview.decimalplaces}") private Integer decimalPlaces;
    @Value("${results.linechart.default.title}") private String defaultTitle;
    @Value("${results.linechart.default.xlabel}") private String defaultXLabel;
    @Value("${results.linechart.default.ylabel}") private String defaultYLabel;

    private final FileChooser fileChooser;
    private Stage stage;
    
    @Autowired
    public ResultsController() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Save selected data to XML file");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("XML file(s) (*.xml)", "*.xml", "*.XML"));
    }

    public void OpenWindow() {
        
        ResultsController controller = new ResultsController();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(controller);
        
        // init results window
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(resultsFxml));
        loader.setController(controller);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(mainCss);
        
        Stage stg= new Stage();
        stg.setScene(scene);
        stg.setTitle(resultsTitle);
        stg.show();
        stg.setIconified(false);
        stg.toFront();
        stg.setOnCloseRequest(e -> {
            resultsService.drop(controller.getLineChart());
        });
        
        controller.setStage(stg);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public synchronized void RefreshModelChoices() {

        String modelChoicePrev = getModelChoice();
        int index = 0;
        boolean found = false;

        ObservableList choices = FXCollections.observableArrayList();
        resultsService.getSimulations().forEach(sim -> {
            if (!choices.contains(sim.getModelName())) {
                choices.add(sim.getModelName());
            }
        });
        choices.sort(Comparator.naturalOrder());
        
        if (modelChoicePrev != null) {
            for (Object n : choices) {
                if (n.toString().contentEquals(modelChoicePrev)) {
                    found = true;
                    break;
                }
                index++;
            }
        }
        choicesModel.setItems(choices);
        if (found) {
            choicesModel.getSelectionModel().select(index);
        }

        FilterChoices(choicesModel, inputModelFilter.getText());
    }

    public synchronized void RefreshSimulationChoices() {
        
        ObservableList choices = FXCollections.observableArrayList();
        boolean found = false;
        int index = 0;

        String modelNameChoice = getModelChoice();
        Simulation simulationChoicePrev;
        
        if (modelNameChoice != null) {

            resultsService.getSimulations().stream()
                    .filter(sim -> (sim.getModelName().contentEquals(modelNameChoice)))
                    .forEach(sim -> {
                        choices.add(sim);
                    });
            choices.sort(Comparator.comparing(Simulation::toString));
            
            simulationChoicePrev = getSimulationChoice();
            if (simulationChoicePrev != null) {
                for (Object s : choices) {
                    if (s.equals(simulationChoicePrev)) {
                        found = true;
                        break;
                    }
                    index++;
                }
            }
        }
        choicesSimulation.setItems(choices);
        if (found) {
            choicesElement.getSelectionModel().select(index);
        }
        FilterChoices(choicesSimulation, inputSimulationFilter.getText());
    }

    private synchronized void RefreshElementChoices() {
        
        ObservableList choices = FXCollections.observableArrayList();
        boolean found = false;
        int index = 0;

        Simulation simulationChoice = getSimulationChoice();
        Choice elementChoicePrev;
        
        if (simulationChoice != null) {
            
            List<Object> places = new ArrayList();
            List<Object> transitions = new ArrayList();
            
            simulationChoice.getElementFilterReferences()
                    .keySet().stream()
                    .filter(elem -> (elem.getElementType() != Element.Type.ARC))
                    .forEach(elem -> {
                        if (elem.getElementType() == Element.Type.PLACE) {
                            places.add(elem);
                        } else {
                            transitions.add(elem);
                        }
                        Choice choice = new Choice(elem.toString());
                        choice.getValues().add(elem);
                        choices.add(choice);
                    });
            choices.sort(Comparator.comparing(Choice::toString));
            choices.add(0, new Choice("<ALL PLACES>", places));
            choices.add(1, new Choice("<ALL TRANSITIONS>", transitions));
            
            elementChoicePrev = getElementChoice();
            if (elementChoicePrev != null) {
                for (Object e : choices) {
                    if (e.toString().contentEquals(elementChoicePrev.toString())) {
                        found = true;
                        break;
                    }
                    index++;
                }
            }
        }
        choicesElement.setItems(choices);
        if (found) {
            choicesElement.getSelectionModel().select(index);
        }
        FilterChoices(choicesElement, inputElementFilter.getText());
    }

    private synchronized void RefreshValueChoices() {

        ObservableList choices = FXCollections.observableArrayList();
        boolean found = false;
        int index = 0;
        
        Simulation simulationChoice = getSimulationChoice();
        Choice elementChoice = getElementChoice();
        Choice valueChoicePrev;
        
        if (simulationChoice != null && elementChoice != null) {
            
            String name;
            Choice choice;
            
            if (elementChoice.getValues().size() == 1) {

                List<String> values = simulationChoice.getElementFilterReferences().get((IElement) elementChoice.getValues().get(0));
                
                for (String value : values) {
                    name = resultsService.getValueName(value, simulationChoice);
                    if (name == null) {
                        messengerService.addWarning("Unhandled value choice will not be available for displaying: '" + simulationChoice.toString() + "', '" + elementChoice.toString() + "', '" + value + "'");
                        continue;
                    }
                    choice = new Choice(name);
                    choice.getValues().add(value);
                    choices.add(choice);
                }
                
            } else {
                
                Map<String,List<String>> valuesMap = resultsService.getSharedValues(simulationChoice, elementChoice.getValues());
                
                for (String key : valuesMap.keySet()) {
                    name = "<ALL> " + key;
                    choice = new Choice(name);
                    choice.getValues().addAll(valuesMap.get(key));
                    choices.add(choice);
                }
            }

            choices.sort(Comparator.comparing(Choice::toString));

            valueChoicePrev = getValueChoice();
            if (valueChoicePrev != null) {
                for (Object vc : choices) {
                    if (vc.toString().contentEquals(valueChoicePrev.toString())) {
                        found = true;
                        break;
                    }
                    index++;
                }
            }
        }
        choicesValue.setItems(choices);
        if (found) {
            choicesValue.getSelectionModel().select(index);
        }
        FilterChoices(choicesValue, inputValueFilter.getText());
    }
    
    private String getModelChoice() {
        return (String) choicesModel.getSelectionModel().getSelectedItem();
    }
    
    private Simulation getSimulationChoice() {
        if (choicesSimulation.getSelectionModel().getSelectedItem() != null) {
            return (Simulation) choicesSimulation.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }
    
    private Choice getElementChoice() {
        if (choicesElement.getSelectionModel().getSelectedItem() != null) {
            return (Choice) choicesElement.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }
    
    private Choice getValueChoice() {
        if (choicesValue.getSelectionModel().getSelectedItem() != null) {
            return (Choice) choicesValue.getSelectionModel().getSelectedItem();
        } else {
            return null;
        }
    }

    private void addSelectedChoiceToChart() {

        Simulation simulationChoice = getSimulationChoice();
        Choice elementChoice = getElementChoice();
        Choice valueChoice = getValueChoice();

        if (simulationChoice == null) {
            setStatus("Select a simulation before adding!", Status.WARNING);
            return;
        } else if (elementChoice == null) {
            setStatus("Select an element before adding!", Status.WARNING);
            return;
        } else if (valueChoice == null) {
            setStatus("Select a value before adding!", Status.WARNING);
            return;
        }
        
        List<SimulationData> dataList = new ArrayList();
        String variable;
        IElement element;
        
        for (Object var : valueChoice.getValues()) {
            
            variable = (String) var;
            element = simulationChoice.getFilterElementReferences().get(variable);
            
            dataList.add(new SimulationData(simulationChoice, element, variable));
        }
        
        boolean exceptions = false;
        boolean duplicates = false;
        boolean success = false;
        
        for (SimulationData data : dataList) {
            
            try {
                resultsService.add(lineChart, data);
                try {
                    resultsService.show(lineChart, data);
                } catch (ResultsServiceException ex) {
                    exceptions = true;
                    messengerService.addException("Adding data to chart failed!", ex);
                    continue;
                }
                success = true;
            } catch (ResultsServiceException ex) {
                duplicates = true;
            }
            
            if (checkboxAutoAddSelected.isSelected()) {
                resultsService.addForAutoAdding(lineChart, data);
            }
        }
        
        if (exceptions) {
            if (success) {
                setStatus("Failed to add some data to the chart!", Status.WARNING);
            } else {
                setStatus("Adding data to chart failed!", Status.FAILURE);
            }
        } else if (duplicates) {
            if (success) {
                setStatus("Added data but skipped duplicates!", Status.WARNING);
            } else {
                setStatus("The selected data has already been added! Please check the table below.", Status.WARNING);
            }
        } else if (success) {
            setStatus("The selected data has been added!", Status.SUCCESS);
        } else {
            setStatus("No action performed!", Status.WARNING);
        }
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

    private void setStatus(String msg, Status type) {
        switch (type) {
            case SUCCESS:
                statusMessageLabel.setTextFill(Color.GREEN);
                break;
            case WARNING:
                statusMessageLabel.setTextFill(Color.ORANGE);
                break;
            case FAILURE:
                statusMessageLabel.setTextFill(Color.RED);
                break;
        }
        statusMessageLabel.setText("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "] " + msg);
    }

    private void clearFilterInputs() {
        inputModelFilter.clear();
        inputSimulationFilter.clear();
        inputElementFilter.clear();
        inputValueFilter.clear();
    }

    private void exportData() {
        try {
            xmlResultsConverter.exportXml(
                    fileChooser.showSaveDialog(stage), 
                    resultsService.getChartData(lineChart)
            );
            setStatus("Data successfully exported!", Status.SUCCESS);
        } catch (Exception ex) {
            setStatus("Export to XML file failed!", Status.FAILURE);
        }
    }

    private void importData() {
        System.out.println("TODO!");
    }

    private Double getStartValue(List<Data> data) {
        if (data.isEmpty()) {
            return null;
        }
        return round(parseDouble(data.get(0).getYValue()));
    }

    private Double getEndValue(List<Data> data) {
        if (data.isEmpty()) {
            return null;
        }
        return round(parseDouble(data.get(data.size() - 1).getYValue()));
    }

    private Double getMinValue(List<Data> data) {
        if (data.isEmpty()) {
            return null;
        }
        double value, min = parseDouble(data.get(0).getYValue());
        for (Data d : data) {
            value = parseDouble(d.getYValue());
            if (value < min) {
                min = value;
            }
        }
        return round(min);
    }

    private Double getMaxValue(List<Data> data) {
        if (data.isEmpty()) {
            return null;
        }
        double value, max = parseDouble(data.get(0).getYValue());
        for (Data d : data) {
            value = parseDouble(d.getYValue());
            if (value > max) {
                max = value;
            }
        }
        return round(max);
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
    
    private void ClearAllItems() {
        while (!tableView.getItems().isEmpty()) {
            resultsService.drop(getLineChart(), getTableView().getItems().get(0));
        }
    }
    
    private void DisableAllItems() {
        tableView.getItems().forEach(data -> {
            if ((System.currentTimeMillis() - data.getTimeLastShownStatusChange()) < 1000) {
                return;
            }
            resultsService.hide(getLineChart(), data);
        });
        tableView.refresh();
    }
    
    private void EnableAllItems() {
        tableView.getItems().forEach(data -> {
            if ((System.currentTimeMillis() - data.getTimeLastShownStatusChange()) < 1000) {
                return;
            }
            try {
                resultsService.show(getLineChart(), data);
            } catch (ResultsServiceException ex) {
                setStatus("Failed enabling item(s)!", Status.FAILURE);
                messengerService.addException("Adding data to chart failed!", ex);
            }
        });
        tableView.refresh();
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
                RefreshModelChoices();
                change.next();
                if (change.wasAdded()) {
                    setStatus("New simulation(s) added!", Status.SUCCESS);
                }
            }
        });
        
        inputModelFilter.setOnKeyReleased(e -> {
            RefreshModelChoices();
            getModelChoices().show();
            getSimulationChoices().hide();
            getElementChoices().hide();
            getValueChoices().hide();
        });
        
        choicesModel.valueProperty().addListener(cl -> RefreshSimulationChoices());
        inputSimulationFilter.setOnKeyReleased(e -> {
            RefreshSimulationChoices();
            getModelChoices().hide();
            getSimulationChoices().show();
            getElementChoices().hide();
            getValueChoices().hide();
        });

        choicesSimulation.valueProperty().addListener(cl -> RefreshElementChoices());
        inputElementFilter.setOnKeyReleased(e -> {
            RefreshElementChoices();
            getModelChoices().hide();
            getSimulationChoices().hide();
            getElementChoices().show();
            getValueChoices().hide();
        });

        choicesElement.valueProperty().addListener(cl -> RefreshValueChoices());
        inputValueFilter.setOnKeyReleased(e -> {
            RefreshValueChoices();
            getModelChoices().hide();
            getSimulationChoices().hide();
            getElementChoices().hide();
            getValueChoices().show();
        });

        buttonClearFilter.setOnAction(e -> clearFilterInputs());
        buttonAddSelected.setOnAction(e -> addSelectedChoiceToChart());

        /**
         * IO buttons.
         */
        buttonImportData.setOnAction(e -> importData());
        buttonExportData.setOnAction(e -> exportData());
        
        buttonEnableAll.setOnAction(e -> EnableAllItems());
        buttonDisableAll.setOnAction(e -> DisableAllItems());
        buttonDropAll.setOnAction(e -> ClearAllItems());
        buttonRefresh.setOnAction(e -> getTableView().refresh());

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
        Callback<TableColumn<SimulationData, CheckBox>, TableCell<SimulationData, CheckBox>> columnAutoAddCellFactory, columnEnableCellFactory;

        tableView.setItems(FXCollections.observableArrayList());
        try {
            resultsService.add(lineChart, tableView);
        } catch (ResultsServiceException ex) {
            System.out.println(ex.getMessage());
        }

        columnAutoAdd.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setSelected(resultsService.containsForAutoAdding(getLineChart(), cellData.getValue()));
            cb.selectedProperty().addListener(e -> {
                if (cb.selectedProperty().getValue()) {
                    resultsService.addForAutoAdding(getLineChart(), cellData.getValue());
                } else {
                    resultsService.removeFromAutoAdding(getLineChart(), cellData.getValue());
                }
                getTableView().refresh();
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        columnAutoAddCellFactory = columnAutoAdd.getCellFactory();
        columnAutoAdd.setCellFactory(c -> {
            TableCell cell = columnAutoAddCellFactory.call(c);
            Tooltip tooltip = new Tooltip("Disables or enables auto adding of following data");
            cell.setTooltip(tooltip);
            return cell;
        });
        
        columnDateTime.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().getDateTime().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"))));
        columnModel.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().getModelName()));
        columnElementId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElementId()));
        columnElementName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElementName()));
        columnValueName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(resultsService.getValueName(cellData.getValue().getVariable(), cellData.getValue().getSimulation())));
        columnValueStart.setCellValueFactory(cellData -> new ReadOnlyDoubleWrapper(getStartValue(cellData.getValue().getSeries().getData())));
        columnValueEnd.setCellValueFactory(cellData -> new ReadOnlyDoubleWrapper(getEndValue(cellData.getValue().getSeries().getData())));
        columnValueMin.setCellValueFactory(cellData -> new ReadOnlyDoubleWrapper(getMinValue(cellData.getValue().getSeries().getData())));
        columnValueMax.setCellValueFactory(cellData -> new ReadOnlyDoubleWrapper(getMaxValue(cellData.getValue().getSeries().getData())));

        columnEnable.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setSelected(cellData.getValue().isShown());
            cb.selectedProperty().addListener(e -> {
                // wait for animations to finish or LineChart breaks if data is added/removed too fast
                if ((System.currentTimeMillis() - cellData.getValue().getTimeLastShownStatusChange()) < 1000) {
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
                if (cb.selectedProperty().getValue()) {
                    try {
                        resultsService.show(getLineChart(), cellData.getValue());
                    } catch (ResultsServiceException ex) {
                        messengerService.addException("Updating data failed!", ex);
                    }
                } else {
                    resultsService.hide(getLineChart(), cellData.getValue());
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
                resultsService.drop(getLineChart(), cellData.getValue());
                setStatus("Data has been dropped!", Status.SUCCESS);
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
        
        RefreshModelChoices();
    }

    public LineChart getLineChart() {
        return lineChart;
    }

    public TableView<SimulationData> getTableView() {
        return tableView;
    }
    
    public ChoiceBox getElementChoices() {
        return choicesElement;
    }
    
    public ChoiceBox getModelChoices() {
        return choicesModel;
    }
    
    public ChoiceBox getSimulationChoices() {
        return choicesSimulation;
    }
    
    public ChoiceBox getValueChoices() {
        return choicesValue;
    }

    private class Choice
    {
        private final String name;
        private final List values;

        private Choice(String name) {
            this(name, new ArrayList());
        }

        private Choice(String name, List values) {
            this.name = name;
            this.values = values;
        }

        private List getValues() {
            return values;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    private enum Status {
        SUCCESS, WARNING, FAILURE;
    }
}
