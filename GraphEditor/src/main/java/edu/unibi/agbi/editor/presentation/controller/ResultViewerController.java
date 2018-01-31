/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller;

import edu.unibi.agbi.editor.core.data.entity.result.Simulation;
import edu.unibi.agbi.editor.core.data.entity.result.ResultSet;
import edu.unibi.agbi.editor.business.service.ResultService;
import edu.unibi.agbi.editor.business.exception.ResultsException;
import edu.unibi.agbi.editor.core.io.ResultsXmlConverter;
import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.entity.IElement;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyDoubleWrapper;
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
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
public class ResultViewerController implements Initializable
{
    @Autowired private ModelService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private ResultService resultsService;
    @Autowired private ResultsXmlConverter xmlResultsConverter;

    @FXML private Menu menuExportModel;
    @FXML private MenuItem itemExportAll;
    @FXML private MenuItem itemExportSelected;
    @FXML private MenuItem itemImport;

    @FXML private MenuItem itemDropAll;
    @FXML private MenuItem itemDisableAll;
    @FXML private MenuItem itemEnableAll;
    @FXML private MenuItem itemRefresh;

    @FXML private CustomMenuItem itemChartTitle;
    @FXML private CustomMenuItem itemChartLabelX;
    @FXML private CustomMenuItem itemChartLabelY;
    @FXML private TextField inputChartTitle;
    @FXML private TextField inputChartLabelX;
    @FXML private TextField inputChartLabelY;

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

    @FXML private TableView<ResultSet> tableView;
    @FXML private TableColumn<ResultSet, CheckBox> columnAutoAdd;
    @FXML private TableColumn<ResultSet, String> columnDateTime;
    @FXML private TableColumn<ResultSet, String> columnModel;
    @FXML private TableColumn<ResultSet, String> columnElementId;
    @FXML private TableColumn<ResultSet, String> columnValueName;
    @FXML private TableColumn<ResultSet, Number> columnValueStart;
    @FXML private TableColumn<ResultSet, Number> columnValueEnd;
    @FXML private TableColumn<ResultSet, Number> columnValueMin;
    @FXML private TableColumn<ResultSet, Number> columnValueMax;
    @FXML private TableColumn<ResultSet, CheckBox> columnEnable;
    @FXML private TableColumn<ResultSet, Button> columnDrop;

    @FXML private LineChart lineChart;

    @Value("${results.tableview.decimalplaces}") private Integer decimalPlaces;
    @Value("${results.linechart.default.title}") private String defaultTitle;
    @Value("${results.linechart.default.xlabel}") private String defaultXLabel;
    @Value("${results.linechart.default.ylabel}") private String defaultYLabel;

    private final FileChooser fileChooser;
    private Stage stage;

    @Autowired
    public ResultViewerController() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("XML file(s) (*.xml)", "*.xml", "*.XML"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void RefreshExportMenu() {

        Menu menu;
        MenuItem item;

        menuExportModel.getItems().clear();
        menuExportModel.getItems().add(itemExportAll);
        menuExportModel.getItems().add(new SeparatorMenuItem());

        for (ModelDao dao : dataService.getDaos()) {

            List<Simulation> simulationResults = new ArrayList();

            resultsService.getSimulationResults().forEach(result -> {
                if (result.getDao().getModelId().contentEquals(dao.getModelId())) {
                    simulationResults.add(result);
                }
            });
            simulationResults.sort((r1, r2) -> r1.getDateTime().compareTo(r2.getDateTime()));

            item = new MenuItem("< ALL Results >");
            item.setOnAction(e -> exportResults(simulationResults));

            menu = new Menu(dao.getModelName() + " - " + dao.getAuthor());
            menu.getItems().add(item);
            menu.getItems().add(new SeparatorMenuItem());

            for (Simulation simulationResult : simulationResults) {

                item = new MenuItem(simulationResult.toString());
                item.setOnAction(e -> exportResult(simulationResult));
                menu.getItems().add(item);
            }

            menuExportModel.getItems().add(menu);
        }
    }

    private synchronized void RefreshModelChoices() {

        Choice choiceModel = getModelChoice();
        String modelId;
        int index = 0;
        boolean found;

        ObservableList<Choice> choices = FXCollections.observableArrayList();
        Choice choice;

        for (Simulation sim : resultsService.getSimulationResults()) {

            found = false;
            for (Choice ch : choices) {
                modelId = ((Simulation) ch.getValue()).getDao().getModelId();
                if (modelId.contentEquals(sim.getDao().getModelId())) {
                    found = true;
                    break;
                }
            }

            if (found) {
                continue;
            }

            choice = new Choice(sim.getDao().getModelName(), sim);
            choices.add(choice);
        }
        choices.sort(Comparator.comparing(Choice::toString));

        found = false;
        if (choiceModel != null) {
            for (Choice ch : choices) {
                if (((Simulation) ch.getValue()).getDao().getModelId()
                        .contentEquals(((Simulation) choiceModel.getValue()).getDao().getModelId())) {
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

    private synchronized void RefreshSimulationChoices() {

        ObservableList choices = FXCollections.observableArrayList();
        boolean found = false;
        int index = 0;

        Choice modelChoice = getModelChoice();
        Simulation simulationChoicePrev;

        if (modelChoice != null) {

            resultsService.getSimulationResults().stream()
                    .filter(sim -> (sim.getDao().getModelId()
                    .contentEquals(((Simulation) modelChoice.getValue())
                            .getDao().getModelId())))
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
            choicesSimulation.getSelectionModel().select(index);
        } else {
            choicesSimulation.getSelectionModel().select(choicesSimulation.getItems().size() - 1);
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

            simulationChoice.getElements().stream()
                    .filter(elem -> (elem.getElementType() != Element.Type.ARC))
                    .forEach(elem -> {
                        if (elem.getElementType() == Element.Type.PLACE) {
                            places.add(elem);
                        } else {
                            transitions.add(elem);
                        }
                        Choice choice = new Choice(elem.toString(), new ArrayList());
                        ((List) choice.getValue()).add(elem);
                        choices.add(choice);
                    });
            choices.sort(Comparator.comparing(Choice::toString));
            if (!places.isEmpty()) {
                choices.add(0, new Choice("<ALL PLACES>", places));
            }
            if (!transitions.isEmpty()) {
                if (places.isEmpty()) {
                    choices.add(0, new Choice("<ALL TRANSITIONS>", transitions));
                } else {
                    choices.add(1, new Choice("<ALL TRANSITIONS>", transitions));
                }
            }

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
            List values = (List) elementChoice.getValue();

            if (values.size() == 1) {

                Set<String> valueStings = simulationChoice.getElementFilter((IElement) values.get(0));

                for (String value : valueStings) {
                    name = resultsService.getValueName(value, simulationChoice);
                    if (name == null) {
                        messengerService.addWarning("Unhandled value choice will not be available for displaying: '" + simulationChoice.toString() + "', '" + elementChoice.toString() + "', '" + value + "'");
                        continue;
                    }
                    choice = new Choice(name, new ArrayList());
                    ((List) choice.getValue()).add(value);
                    choices.add(choice);
                }

            } else {

                Map<String, List<String>> valuesMap = resultsService.getSharedValues(simulationChoice, values);

                if (valuesMap != null) {
                    for (String key : valuesMap.keySet()) {
                        name = "<ALL> " + key;
                        choice = new Choice(name, new ArrayList());
                        ((List) choice.getValue()).addAll(valuesMap.get(key));
                        choices.add(choice);
                    }
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
        } else {
            choicesValue.getSelectionModel().select(0);
        }
        FilterChoices(choicesValue, inputValueFilter.getText());
    }

    private Choice getModelChoice() {
        return (Choice) choicesModel.getSelectionModel().getSelectedItem();
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

        ResultSet result;
        String variable;
        IElement element;

        boolean exceptions = false;
        boolean duplicates = false;
        boolean success = false;

        for (Object var : (List) valueChoice.getValue()) {

            result = null;

            try {
                variable = (String) var;
                element = simulationChoice.getFilterElement(variable);
                result = new ResultSet(simulationChoice, element, variable);

                resultsService.add(lineChart, result);
                try {
                    resultsService.show(lineChart, result);
                } catch (ResultsException ex) {
                    exceptions = true;
                    messengerService.addException("Adding data to chart failed!", ex);
                    continue;
                }
                success = true;
            } catch (ResultsException ex) {
                duplicates = true;
            }

            if (result != null && checkboxAutoAddSelected.isSelected()) {
                resultsService.addForAutoAdding(lineChart, result);
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
        statusMessageLabel.setText("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg);
    }

    private void clearFilterInputs() {
        inputModelFilter.clear();
        inputSimulationFilter.clear();
        inputElementFilter.clear();
        inputValueFilter.clear();
    }

    private void exportResult(Simulation result) {
        List<Simulation> results = new ArrayList();
        results.add(result);
        exportResults(results);
    }

    private void exportResults(List<Simulation> results) {
        try {
            fileChooser.setTitle("Export model results to XML file");
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }
            xmlResultsConverter.ExportSimulationResults(file, results);
            setStatus("Simulation data export successful!", Status.SUCCESS);
        } catch (Exception ex) {
            setStatus("Simulation data export failed!", Status.FAILURE);
            ex.printStackTrace();
        }
    }

    private void exportData(List<ResultSet> results) {
        try {
            fileChooser.setTitle("Export selected data to XML file");
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }
            xmlResultsConverter.ExportResultSets(file, results);
            setStatus("Simulation data export successful!", Status.SUCCESS);
        } catch (Exception ex) {
            setStatus("Simulation data export failed!", Status.FAILURE);
        }
    }

    private void importData() {
        
        File file;
        List<Simulation> simulationResults;
        boolean skipped = false;
        
        try {
            fileChooser.setTitle("Import results data from XML file");
            file = fileChooser.showOpenDialog(stage);
            if (file == null) {
                return;
            }
            simulationResults = xmlResultsConverter.importXml(file);
            for (Simulation result : simulationResults) {
                if (!resultsService.add(result)) {
                    System.out.println("Skipped import!");
                    skipped = true;
                }
            }
            if (skipped) {
                setStatus("Simulation data import successful but skipped already present results!", Status.WARNING);
            } else {
                setStatus("Simulation data import successful!", Status.SUCCESS);
            }
        } catch (Exception ex) {
            setStatus("Simulation data import failed!", Status.FAILURE);
        }
    }

    private Double getStartValue(List<Data> data) {
        if (data.isEmpty()) {
            return 0d;
        }
        return round(parseDouble(data.get(0).getYValue()));
    }

    private Double getEndValue(List<Data> data) {
        if (data.isEmpty()) {
            return 0d;
        }
        return round(parseDouble(data.get(data.size() - 1).getYValue()));
    }

    private Double getMinValue(List<Data> data) {
        double value, min;
        if (data.isEmpty()) {
            return 0d;
        }
        min = parseDouble(data.get(0).getYValue());
        for (Data d : data) {
            value = parseDouble(d.getYValue());
            if (value < min) {
                min = value;
            }
        }
        return round(min);
    }

    private Double getMaxValue(List<Data> data) {
        double value, max;
        if (data.isEmpty()) {
            return 0d;
        }
        max = parseDouble(data.get(0).getYValue());
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
            } catch (ResultsException ex) {
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
        resultsService.getSimulationResults().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                RefreshExportMenu();
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
         * Menu Items.
         */
        itemExportAll.setOnAction(e -> exportResults(resultsService.getSimulationResults()));
        itemExportSelected.setOnAction(e -> exportData(resultsService.getChartData(getLineChart())));
        itemImport.setOnAction(e -> importData());

        itemDropAll.setOnAction(e -> ClearAllItems());
        itemDisableAll.setOnAction(e -> DisableAllItems());
        itemEnableAll.setOnAction(e -> EnableAllItems());
        itemRefresh.setOnAction(e -> getTableView().refresh());

        /**
         * LineChart.
         */
        lineChart.createSymbolsProperty().set(false);
        lineChart.titleProperty().bind(inputChartTitle.textProperty());
        lineChart.getXAxis().labelProperty().bind(inputChartLabelX.textProperty());
        lineChart.getYAxis().labelProperty().bind(inputChartLabelY.textProperty());
        itemChartTitle.setHideOnClick(false);
        itemChartLabelX.setHideOnClick(false);
        itemChartLabelY.setHideOnClick(false);
        inputChartTitle.setText(defaultTitle);
        inputChartLabelX.setText(defaultXLabel);
        inputChartLabelY.setText(defaultYLabel);

        /**
         * TableView.
         */
        Callback<TableColumn<ResultSet, Button>, TableCell<ResultSet, Button>> columnDropCellFactory;
        Callback<TableColumn<ResultSet, CheckBox>, TableCell<ResultSet, CheckBox>> columnAutoAddCellFactory, columnEnableCellFactory;

        tableView.setItems(FXCollections.observableArrayList());
        try {
            resultsService.add(lineChart, tableView);
        } catch (ResultsException ex) {
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
        columnModel.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSimulation().getDao().getModelName()));
        columnElementId.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElement().getId()));
//        columnElementName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getElement().getName()));
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
                    } catch (ResultsException ex) {
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

        RefreshExportMenu();
        RefreshModelChoices();
    }

    public LineChart getLineChart() {
        return lineChart;
    }

    public TableView<ResultSet> getTableView() {
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
        private final Object value;

        private Choice(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        private Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private enum Status
    {
        SUCCESS, WARNING, FAILURE;
    }
}
