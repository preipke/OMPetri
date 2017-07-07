/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.graph;

import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.exception.InputValidationException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
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
import javafx.scene.control.TitledPane;
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
    @Autowired private MessengerService messengerService;
    @Autowired private FunctionBuilder functionBuilder;

    @FXML private TextField inputParamName;
    @FXML private TextField inputParamNote;
    @FXML private TextField inputParamValue;
    @FXML private ChoiceBox<Parameter.Type> choiceParamScope;
    @FXML private Button buttonParamCreate;

    @FXML private TitledPane paneParamLocal;
    @FXML private TableView tableParamLocal;
    @FXML private TableColumn<Parameter, String> paramNameLocal;
    @FXML private TableColumn<Parameter, String> paramValueLocal;
    @FXML private TableColumn<Parameter, String> paramNoteLocal;
    @FXML private TableColumn<Parameter, Button> paramDeleteLocal;

    @FXML private TableView tableParamGlobal;
    @FXML private TableColumn<Parameter, String> paramNameGlobal;
    @FXML private TableColumn<Parameter, String> paramValueGlobal;
    @FXML private TableColumn<Parameter, String> paramNoteGlobal;
    @FXML private TableColumn<Parameter, Number> paramReferencesGlobal;
    @FXML private TableColumn<Parameter, Button> paramDeleteGlobal;

    @FXML private Label statusParamCreate;
    @FXML private Label statusParamGlobal;
    @FXML private Label statusParamLocal;

    @Value("${regex.param.ident.flowIn.actual}") private String regexParamIdentFlowInActual;
    @Value("${regex.param.ident.flowIn.total}") private String regexParamIdentFlowInTotal;
    @Value("${regex.param.ident.flowOut.actual}") private String regexParamIdentFlowOutActual;
    @Value("${regex.param.ident.flowOut.total}") private String regexParamIdentFlowOutTotal;
    @Value("${regex.param.ident.speed}") private String regexParamIdentSpeed;
    @Value("${regex.param.ident.token}") private String regexParamIdentToken;

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
    public void ShowParameters(IDataElement element) {
        choiceParamScope.getItems().clear();
        choiceParamScope.getItems().add(Parameter.Type.GLOBAL);
        if (element != null) {
            paneParamLocal.setText(element.toString() + " - Local");
            if (element instanceof DataTransition) {
                transitionSelected = (DataTransition) element;
            } else {
                transitionSelected = null;
            }
            choiceParamScope.getItems().add(Parameter.Type.LOCAL);
        } else {
            paneParamLocal.setText("<ALL> - Local");
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
     * @throws DataServiceException
     */
    private void CreateParameter(DataTransition transition) {

        String id = inputParamName.getText();
        String unit = inputParamNote.getText();
        String value = inputParamValue.getText();
        Parameter.Type type = choiceParamScope.getSelectionModel().getSelectedItem();

        // Validate id
        try {
            if (id.isEmpty() | !id.matches(functionBuilder.getParameterRegex())) {
                throw new InputValidationException("Trying to create parameter using restricted characters or identifier format");
            }
            String[] restrictedRegex = new String[]{
                regexParamIdentFlowInActual, regexParamIdentFlowInTotal,
                regexParamIdentFlowOutActual, regexParamIdentFlowOutTotal,
                regexParamIdentSpeed, regexParamIdentToken
            };
            for (String regex : restrictedRegex) {
                if (id.matches(regex)) {
                    throw new InputValidationException("Trying to create parameter using restricted identifier");
                }
            }
            setStatus(inputParamName, statusParamCreate, "");
        } catch (InputValidationException ex) {
            setStatus(inputParamName, statusParamCreate, "Invalid parameter name!", ex);
            return;
        }

        // Validate value
        try {
            value = value.replace(",", ".");
            if (value.isEmpty() | !value.matches(functionBuilder.getNumberRegex())) {
                throw new InputValidationException("");
            }
            setStatus(inputParamValue, statusParamCreate, "");
        } catch (InputValidationException ex) {
            setStatus(inputParamValue, statusParamCreate, "Invalid parameter value!", ex);
            return;
        }

        Parameter param = new Parameter(id, value, unit, type, null);
        try {
            parameterService.add(param, transition);
            if (param.getType() == Parameter.Type.LOCAL) {
                parametersLocal.add(param);
            } else if (param.getType() == Parameter.Type.GLOBAL) {
                parametersGlobal.add(param);
            }
            inputParamName.clear();
            inputParamNote.clear();
            inputParamValue.clear();
            setStatus(null, statusParamCreate, "Created parameter '" + param.getId() + "'! [" + param.getType() + "]");
        } catch (ParameterServiceException ex) {
            setStatus(null, statusParamCreate, "Parameter creation failed!", ex);
        }
    }

    /**
     * Deletes a parameter.
     *
     * @param param
     * @param transition
     * @throws DataServiceException thrown in case the given parameter can
     *                                   not be deleted from the dao - this is
     *                                   usually when there is elements
     *                                   referring to the parameter
     */
    private void DeleteParameter(Parameter param) throws ParameterServiceException {
        parameterService.remove(param);
        if (param.getType() == Parameter.Type.LOCAL) {
            parametersLocal.remove(param);
        } else {
            parametersGlobal.remove(param);
        }
    }
    
    private void setStatus(TextField input, Label label, String msg) {
        if (input != null) {
            input.setStyle("");
        }
        label.setTextFill(Color.GREEN);
        label.setText(msg);
    }

    /**
     * Sets a label's text and coloring.
     *
     * @param input
     * @param label
     * @param msg
     * @param thr
     */
    private void setStatus(TextField input, Label label, String msg, Throwable thr) {
        if (input != null) {
            input.setStyle("-fx-border-color: red");
        }
        label.setTextFill(Color.RED);
        label.setText(msg);
        if (thr != null) {
            messengerService.addException(msg, thr);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Callback<TableColumn<Parameter, Button>, TableCell<Parameter, Button>> paramDeleteLocalCellFactory;
        Callback<TableColumn<Parameter, Button>, TableCell<Parameter, Button>> paramDeleteGlobalCellFactory;
        Callback<TableColumn<Parameter, Number>, TableCell<Parameter, Number>> paramReferencesGlobalCellFactory;

        buttonParamCreate.setOnAction(e -> CreateParameter(transitionSelected));

        /**
         * Table local parameters.
         */
        tableParamLocal.setItems(parametersLocal);
        tableParamLocal.setEditable(true);

        paramNameLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        paramValueLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        paramValueLocal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramValueLocal.setOnEditCommit((CellEditEvent<Parameter, String> t) -> {
            if (!t.getNewValue().replace(",", ".").matches(functionBuilder.getNumberRegex())) {
                setStatus(null, statusParamLocal, "Invalid value!", null);
            } else {
                ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                        .setValue(t.getNewValue().replace(",", "."));
                setStatus(null, statusParamLocal, "Value changed!");
            }
        });
        paramNoteLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUnit()));
        paramNoteLocal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramNoteLocal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                            .setUnit(t.getNewValue());
                });
        paramDeleteLocal.setCellValueFactory(cellData -> {
            CheckBox cb = new CheckBox();
            cb.setAllowIndeterminate(true);
            cb.setIndeterminate(true);
            cb.setOnAction(e -> {
                try {
                    DeleteParameter(cellData.getValue());
                    setStatus(null, statusParamLocal, "Deleted parameter '" + cellData.getValue().getId() + "'! [LOCAL]");
                } catch (ParameterServiceException ex) {
                    cb.setIndeterminate(true);
                    setStatus(null, statusParamLocal, "Cannot delete parameter!", ex);
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
        tableParamGlobal.setItems(parametersGlobal);
        tableParamGlobal.setEditable(true);

        paramNameGlobal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        paramValueGlobal.setCellValueFactory(new PropertyValueFactory("value"));
        paramValueGlobal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramValueGlobal.setOnEditCommit((CellEditEvent<Parameter, String> t) -> {
            if (!t.getNewValue().replace(",", ".").matches(functionBuilder.getNumberRegex())) {
                setStatus(null, statusParamGlobal, "Invalid value!", null);
            } else {
                ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                        .setValue(t.getNewValue().replace(",", "."));
                setStatus(null, statusParamGlobal, "Value changed!");
            }
        });
        paramNoteGlobal.setCellValueFactory(new PropertyValueFactory("unit"));
        paramNoteGlobal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramNoteGlobal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(t.getTablePosition().getRow()))
                            .setUnit(t.getNewValue());
                });
        paramReferencesGlobal.setCellValueFactory(cellData -> new ReadOnlyIntegerWrapper(cellData.getValue().getUsingElements().size()));
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
                    DeleteParameter(cellData.getValue());
                    setStatus(null, statusParamGlobal, "Deleted parameter '" + cellData.getValue().getId() + "'! [GLOBAL]");
                } catch (ParameterServiceException ex) {
                    cb.setIndeterminate(true);
                    setStatus(null, statusParamGlobal, "Cannot delete parameter!", ex);
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
