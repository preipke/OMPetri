/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ParameterController implements Initializable
{
    @Autowired private ParameterService parameterService;

    @FXML private TextField inputParamName;
    @FXML private TextField inputParamNote;
    @FXML private TextField inputParamValue;
    @FXML private ChoiceBox<Parameter.Type> choiceParamScope;
    @FXML private Button buttonParamCreate;

    @FXML private TableView paramTableLocal;
    @FXML private TableColumn<Parameter, String> paramNameLocal;
    @FXML private TableColumn<Parameter, String> paramValueLocal;
    @FXML private TableColumn<Parameter, String> paramNoteLocal;
    @FXML private TableColumn<Parameter, Button> paramDeleteLocal;

    @FXML private TableView paramTableGlobal;
    @FXML private TableColumn<Parameter, String> paramNameGlobal;
    @FXML private TableColumn<Parameter, String> paramValueGlobal;
    @FXML private TableColumn<Parameter, String> paramNoteGlobal;
    @FXML private TableColumn<Parameter, Number> paramReferencesGlobal;
    @FXML private TableColumn<Parameter, Button> paramDeleteGlobal;

    @FXML private Label statusParamCreate;
    @FXML private Label statusParamGlobal;
    @FXML private Label statusParamLocal;

    @Value("${regex.function.number}") private String regexFunctionNumber;
    @Value("${regex.function.parameter}") private String regexFunctionParameter;

    @Value("${param.name.reference.fire}") private String referenceFireName;
    @Value("${param.name.reference.speed}") private String referenceSpeedName;
    @Value("${param.name.reference.token}") private String referenceTokenName;
    @Value("${param.name.reference.tokenflow}") private String referenceTokenflowName;

    private final ObservableList<Parameter> parametersLocal;
    private final ObservableList<Parameter> parametersGlobal;
    private DataTransition transitionSelected;

    public ParameterController() {
        this.parametersLocal = FXCollections.observableArrayList();
        this.parametersGlobal = FXCollections.observableArrayList();
    }

    /**
     * Shows and lists available parameters.
     * 
     * @param element 
     */
    public void ShowParameterDetails(IDataElement element) {
        if (element instanceof DataTransition) {
            transitionSelected = (DataTransition) element;
        } else {
            transitionSelected = null;
        }
        ClearInputsAndLabels();
        RefreshLocalParameters(transitionSelected);
        RefreshGlobalParameters();
    }

    /**
     * Clears inputs and sets the choices for places to choose from as value
     * references.
     */
    private void ClearInputsAndLabels() {
        inputParamName.clear();
        inputParamNote.clear();
        inputParamValue.clear();
        statusParamCreate.setStyle("");
        statusParamLocal.setStyle("");
        statusParamGlobal.setStyle("");
        statusParamCreate.setText("");
        statusParamLocal.setText("");
        statusParamGlobal.setText("");
        choiceParamScope.getSelectionModel().select(0);
    }

    /**
     * Lists the available local parameters for an element.
     *
     * @param transition
     */
    private void RefreshLocalParameters(DataTransition transition) {
        parametersLocal.clear();
        if (transition != null) {
            parametersLocal.addAll(parameterService.getLocalParameters(transition));
            paramTableLocal.setDisable(false);
        } else {
            paramTableLocal.setDisable(true);
        }
    }

    /**
     * Lists the available global parameters.
     *
     * @param elem
     */
    private void RefreshGlobalParameters() {
        parametersGlobal.clear();
        parametersGlobal.addAll(parameterService.getGlobalParameters());
    }

    /**
     * Creates a new parameter. Uses the given values to create either a local
     * or global parameter, getting name and value from the input textfields.
     * Checks the given inputs to be valid.
     *
     * @param id
     * @param type
     * @return
     * @throws DataGraphServiceException 
     */
    private void CreateParameter(DataTransition transition) {

        String id = inputParamName.getText();
        String note = inputParamNote.getText();
        String value = inputParamValue.getText().replace(",", ".");
        Parameter.Type type = choiceParamScope.getSelectionModel().getSelectedItem();

        // Validate id
        if (id.isEmpty() | !id.matches(regexFunctionParameter)) {
            inputParamName.setStyle("-fx-border-color: red");
            setStatusLabel(statusParamCreate, "Invalid parameter name!", true);
            return;
        }
        String[] restrictedSequences = new String[]{
            referenceFireName, referenceSpeedName,
            referenceTokenName, referenceTokenflowName
        };
        for (String sequence : restrictedSequences) {
            if (id.length() >= sequence.length()) {
                if (id.substring(0, sequence.length()).matches(sequence)) {
                    inputParamName.setStyle("-fx-border-color: red");
                    setStatusLabel(statusParamCreate, "'" + sequence + "' is a restricted identifier!", true);
                    return;
                }
            }
        }
        inputParamName.setStyle("");

        if (value.isEmpty() | !value.matches(regexFunctionNumber)) {
            inputParamValue.setStyle("-fx-border-color: red");
            setStatusLabel(statusParamCreate, "Invalid parameter value!", true);
            return;
        } else {
            inputParamValue.setStyle("");
        }

        Parameter param = new Parameter(id, note, value, type);
        try {
            parameterService.addParameter(param, transition);
            if (param.getType() == Parameter.Type.LOCAL) {
                parametersLocal.add(param);
            } else if (param.getType() == Parameter.Type.GLOBAL) {
                parametersGlobal.add(param);
            }
            inputParamName.clear();
            inputParamNote.clear();
            inputParamValue.clear();
            setStatusLabel(statusParamCreate, "Created parameter '" + param.getId() + "'! [" + param.getType() + "]", false);
        } catch (ParameterServiceException ex) {
            setStatusLabel(statusParamCreate, ex.getMessage(), true);
        }
    }

    /**
     * Deletes a parameter.
     *
     * @param param
     * @param transition
     * @throws DataGraphServiceException thrown in case the given parameter can
     *                                   not be deleted from the dao - this is
     *                                   usually when there is elements
     *                                   referring to the parameter
     */
    private void DeleteParameter(Parameter param, IDataElement element) throws ParameterServiceException {
        parameterService.remove(param, element);
        if (param.getType() == Parameter.Type.LOCAL) {
            parametersLocal.remove(param);
        } else {
            parametersGlobal.remove(param);
        }
    }

    /**
     * Sets a label's text and coloring.
     * 
     * @param label
     * @param msg
     * @param isError 
     */
    public void setStatusLabel(Label label, String msg, boolean isError) {
        if (isError) {
            label.setTextFill(Color.RED);
        } else {
            label.setTextFill(Color.GREEN);
        }
        label.setText(msg);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Callback<TableColumn<Parameter, Button>, TableCell<Parameter, Button>> paramDeleteLocalCellFactory;
        Callback<TableColumn<Parameter, Button>, TableCell<Parameter, Button>> paramDeleteGlobalCellFactory;
        Callback<TableColumn<Parameter, Number>, TableCell<Parameter, Number>> paramReferencesGlobalCellFactory;

        choiceParamScope.getItems().clear();
        choiceParamScope.getItems().add(Parameter.Type.GLOBAL);
        choiceParamScope.getItems().add(Parameter.Type.LOCAL);
        buttonParamCreate.setOnAction(e -> CreateParameter(transitionSelected));

        /**
         * Table local parameters.
         */
        paramTableLocal.setItems(parametersLocal);
        paramTableLocal.setEditable(true);

        paramNameLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        paramValueLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        paramValueLocal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramValueLocal.setOnEditCommit((CellEditEvent<Parameter, String> t) -> {
                    if (!t.getNewValue().matches(regexFunctionNumber)) {
                        setStatusLabel(statusParamLocal, "Invalid value specified!", true);
                    } else {
                        ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                                .setValue(t.getNewValue());
                        setStatusLabel(statusParamLocal, "Value changed!", false);
                    }
                });
        paramNoteLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNote()));
        paramNoteLocal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramNoteLocal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                            .setNote(t.getNewValue());
                });
        paramDeleteLocal.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setAllowIndeterminate(true);
            cb.setIndeterminate(true);
            cb.setOnAction(e -> {
                try {
                    DeleteParameter(cellData.getValue(), transitionSelected);
                    setStatusLabel(statusParamLocal, "Deleted parameter '" + cellData.getValue().getId() + "'! [LOCAL]", false);
                } catch (ParameterServiceException ex) {
                    setStatusLabel(statusParamLocal, ex.getMessage(), true);
                }
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        paramDeleteLocalCellFactory = paramDeleteLocal.getCellFactory();
        paramDeleteLocal.setCellFactory(c -> {
            TableCell cell = paramDeleteLocalCellFactory.call(c);
            Tooltip tooltip = new Tooltip("Removes this parameter");
            cell.setTooltip(tooltip);
            return cell;
        });

        /**
         * Table global parameters.
         */
        paramTableGlobal.setItems(parametersGlobal);
        paramTableGlobal.setEditable(true);

        paramNameGlobal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        paramValueGlobal.setCellValueFactory(new PropertyValueFactory("value"));
        paramValueGlobal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramValueGlobal.setOnEditCommit((CellEditEvent<Parameter, String> t) -> {
                    if (!t.getNewValue().replace(",", ".").matches(regexFunctionNumber)) {
                        setStatusLabel(statusParamGlobal, "Invalid value specified!", true);
                    } else {
                        ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                                .setValue(t.getNewValue().replace(",", "."));
                        setStatusLabel(statusParamGlobal, "Value changed!", true);
                    }
                });
        paramNoteGlobal.setCellValueFactory(new PropertyValueFactory("note"));
        paramNoteGlobal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramNoteGlobal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                            .setNote(t.getNewValue());
                });
        paramReferencesGlobal.setCellValueFactory(cellData -> new ReadOnlyIntegerWrapper(cellData.getValue().getReferingNodes().size()));
        paramReferencesGlobalCellFactory = paramReferencesGlobal.getCellFactory();
        paramReferencesGlobal.setCellFactory(c -> {
            TableCell cell = paramReferencesGlobalCellFactory.call(c);
            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(cell.itemProperty().asString().concat(" elements are using this parameter."));
            cell.setTooltip(tooltip);
            return cell;
        });
        paramDeleteGlobal.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setAllowIndeterminate(true);
            cb.setIndeterminate(true);
            cb.setOnAction(e -> {
                try {
                    DeleteParameter(cellData.getValue(), null);
                    setStatusLabel(statusParamGlobal, "Deleted parameter '" + cellData.getValue().getId() + "'! [GLOBAL]", false);
                } catch (ParameterServiceException ex) {
                    cb.setIndeterminate(true);
                    setStatusLabel(statusParamGlobal, ex.getMessage(), true);
                }
            });
            return new ReadOnlyObjectWrapper(cb);
        });
        paramDeleteGlobalCellFactory = paramDeleteGlobal.getCellFactory();
        paramDeleteGlobal.setCellFactory(c -> {
            TableCell cell = paramDeleteGlobalCellFactory.call(c);
            Tooltip tooltip = new Tooltip("Attempts to remove this parameter");
            cell.setTooltip(tooltip);
            return cell;
        });
    }
}
