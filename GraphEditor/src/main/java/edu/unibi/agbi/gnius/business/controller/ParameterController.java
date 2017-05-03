/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.InputValidationException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
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
    @Autowired private DataGraphService dataGraphService;
    @Autowired private MessengerService messengerService;

    @FXML private TextField paramInputName;
    @FXML private TextField paramInputNote;
    @FXML private ChoiceBox paramReferencePlaceChoice;
    @FXML private ChoiceBox paramReferenceTokenChoice;
    @FXML private TextField paramInputValue;
    @FXML private SwingNode paramFunctionImageNode;
    @FXML private Label paramInputStatusMessage;

    @FXML private Button paramAddLocal;
    @FXML private Button paramAddGlobal;

    @FXML private TableView paramTableLocal;
    @FXML private TableColumn<Parameter, String> paramNameLocal;
    @FXML private TableColumn<Parameter, String> paramValueLocal;
    @FXML private TableColumn<Parameter, String> paramNoteLocal;
    @FXML private TableColumn<Parameter, Button> paramDeleteLocal;
    @FXML private Label paramLocalStatusMessage;

    @FXML private TableView paramTableGlobal;
    @FXML private TableColumn<Parameter, String> paramNameGlobal;
    @FXML private TableColumn<Parameter, String> paramValueGlobal;
    @FXML private TableColumn<Parameter, String> paramNoteGlobal;
    @FXML private TableColumn<Parameter, Number> paramReferencesGlobal;
    @FXML private TableColumn<Parameter, Button> paramDeleteGlobal;
    @FXML private Label paramGlobalStatusMessage;

    @Value("${css.button.tablerow.delete}") private String buttonTableRowDeleteStyle;
    @Value("${param.validation.regex.split}") private String paramValidationRegexSplit;
    @Value("${param.validation.regex.match}") private String paramValidationRegexMatch;

    private final ObservableList<Parameter> localParameters;
    private final ObservableList<Parameter> globalParameters;
    
    private IDataElement selectedElement;

    public ParameterController() {
        localParameters = FXCollections.observableArrayList();
        globalParameters = FXCollections.observableArrayList();
    }

    public void ShowParameterDetails(IDataElement elem) {
        RefreshInputAndReferenceChoices();
        RefreshLocalParameters(elem);
        RefreshGlobalParameterReferences();
    }

    /**
     * Validates the function input for a transition.
     * 
     * @param element
     * @param functionInput
     * @throws InputValidationException 
     */
    public void ValidateFunctionInput(DataTransition element, String functionInput) throws InputValidationException {

        String[] candidates = functionInput.split(paramValidationRegexSplit);
        List<Parameter> currentParameter = new ArrayList();
        Parameter param;

        for (String candidate : candidates) {
            if (!candidate.matches(paramValidationRegexMatch)) {
                if (dataGraphService.getParameterIds().contains(candidate)) {
                    param = dataGraphService.getParameter(candidate);
                    if (param.getType() == Parameter.Type.LOCAL) {
                        if (!element.getParameters().keySet().contains(candidate)) {
                            throw new InputValidationException("'" + candidate + "' is a LOCAL parameter for a different element");
                        }
                    }
                    param.addReferingNode(element);
                    currentParameter.add(param);
                } else {
                    throw new InputValidationException("Parameter '" + candidate + "' does not exist");
                }
            } 
        }

        element.getFunction().getParameter().clear();
        currentParameter.forEach(parameter -> {
            element.getFunction().getParameter().add(parameter);
        });
    }

    /**
     * Gets all parameters usable for an element.
     * 
     * @param elem
     * @return 
     */
    public List<Parameter> getParameter(IDataElement elem) {

        List<Parameter> parameters = new ArrayList();
        List<Parameter> tmp;

        tmp = new ArrayList();
        tmp.addAll(elem.getParameters().values());
        tmp.sort(Comparator.comparing(Parameter::toString));
        parameters.addAll(tmp);

        tmp.clear();
        tmp.addAll(globalParameters);
        tmp.sort(Comparator.comparing(Parameter::toString));
        parameters.addAll(tmp);

        return parameters;
    }

    /**
     * Clears inputs and sets the choices for places to choose from as value
     * references.
     */
    private void RefreshInputAndReferenceChoices() {
        
        paramInputName.clear();
        paramInputNote.clear();
        paramInputValue.clear();
        if (paramFunctionImageNode.getContent() != null) {
            paramFunctionImageNode.getContent().removeAll();
        }
        paramInputStatusMessage.setText("");

        Collection<INode> places = dataGraphService.getDataDao().getPlaces();
        ObservableList<PlaceReferenceChoice> placeReferenceChoices = FXCollections.observableArrayList();

        for (INode place : places) {
            placeReferenceChoices.add(new PlaceReferenceChoice((DataPlace) place));
        }
        placeReferenceChoices.sort(Comparator.comparing(PlaceReferenceChoice::toString));

//        paramReferencePlaceChoice.getItems().clear();
        paramReferencePlaceChoice.setItems(placeReferenceChoices);
        paramReferencePlaceChoice.getItems().add(0, "- no reference -");
        paramReferencePlaceChoice.getSelectionModel().select(0);
    }

    /**
     * Refreshs the available local parameters for an element.
     *
     * @param elem
     */
    private void RefreshLocalParameters(IDataElement elem) {
        List<Parameter> parameters = new ArrayList();
        selectedElement = elem;
        localParameters.clear();
        if (elem != null) {
            parameters.addAll(elem.getParameters().values());
            parameters.sort(Comparator.comparing(Parameter::toString));
            localParameters.addAll(parameters);
            localParameters.stream().forEach(param -> {
                if (!dataGraphService.isElementReferencingParameter(elem, param)) {
                    param.removeReferingNode(elem);
                }
            });
        }
        paramLocalStatusMessage.setText("");
    }

    /**
     * Refreshs the number of refering nodes for each global parameter.
     */
    private void RefreshGlobalParameterReferences() {
        globalParameters.parallelStream().forEach(param -> {
            IDataElement[] elements = new IDataElement[param.getReferingNodes().size()];
            param.getReferingNodes().toArray(elements);
            for (IDataElement element : elements) {
                if (!dataGraphService.isElementReferencingParameter(element, param)) {
                    param.removeReferingNode(element);
                }
            }
        });
        paramGlobalStatusMessage.setText("");
    }

    /**
     * Refresh the token to choose from as value reference for the selected
     * place.
     */
    private void RefreshTokenChoices() {

        if (isReferenceChosen()) {
            setManualValueInputEnabled(false);
        } else {
            setManualValueInputEnabled(true);
            return;
        }

        PlaceReferenceChoice choice = (PlaceReferenceChoice) paramReferencePlaceChoice.getSelectionModel().getSelectedItem();

        if (choice == null) {
            return;
        }

        DataPlace place = choice.getPlace();
        ObservableList<TokenReferenceChoice> tokenReferenceChoices = FXCollections.observableArrayList();

        Collection<Token> token = place.getToken();
        for (Token tkn : token) {
            tokenReferenceChoices.add(new TokenReferenceChoice(tkn));
        }

        paramReferenceTokenChoice.getItems().clear();
        paramReferenceTokenChoice.setItems(tokenReferenceChoices);
        paramReferenceTokenChoice.getSelectionModel().select(0);
    }

    /**
     * Indicates wether or not a place for referencing the value has been
     * chosen.
     *
     * @return
     */
    private boolean isReferenceChosen() {
        return paramReferencePlaceChoice.getSelectionModel().getSelectedIndex() != 0;
    }

    /**
     * Parses the input from the textfield.
     *
     * @return
     */
    private String parseValue(String input) {
        String value;
        try {
            value = Double.toString(Double.parseDouble(input));
        } catch (NumberFormatException ex) {
            messengerService.addToLog("The specified value is not a number! [" + ex.getMessage() + "]");
            return null;
        }
        return value;
    }

    private void setManualValueInputEnabled(boolean value) {
        paramInputValue.setDisable(!value);
        paramReferenceTokenChoice.setDisable(value);
        paramInputValue.setVisible(value);
        paramReferenceTokenChoice.setVisible(!value);
    }

    /**
     * Creates a local parameter for the given element.
     *
     * @param elem
     */
    private void createLocalParameter(IDataElement elem) {
        try {
            Parameter param = createParameter(Parameter.Type.LOCAL);
            elem.getParameters().put(param.getId(), param);
            localParameters.add(param);
            paramInputStatusMessage.setText("Created LOCAL parameter '" + param.getId() + "'!");
            paramInputStatusMessage.setTextFill(Color.GREEN);
        } catch (InputValidationException ex) {
            paramInputStatusMessage.setText("Cannot create parameter! [" + ex.getMessage() + "]");
            paramInputStatusMessage.setTextFill(Color.RED);
        }
    }

    /**
     * Creates a global parameter.
     */
    private void createGlobalParameter() {
        try {
            Parameter param = createParameter(Parameter.Type.GLOBAL);
            globalParameters.add(param);
            paramInputStatusMessage.setText("Created GLOBAL parameter '" + param.getId() + "'!");
            paramInputStatusMessage.setTextFill(Color.GREEN);
        } catch (InputValidationException ex) {
            paramInputStatusMessage.setText("Cannot create parameter! [" + ex.getMessage() + "]");
            paramInputStatusMessage.setTextFill(Color.RED);
        }
    }

    /**
     * Creates a new parameter. Uses the given values to create either a local
     * or global parameter, getting name and value from the input textfields.
     *
     * @param id
     * @param type
     * @return
     * @throws DataGraphServiceException thrown in case of empty name and
     *                                   misformatted value inputs or id
     *                                   conflicts while storing the new
     *                                   parameter
     */
    private Parameter createParameter(Parameter.Type type) throws InputValidationException {

        String id, note, value;
        Parameter param;

        if (paramInputName.getText().isEmpty()) {
            throw new InputValidationException("Specify a parameter name");
        }
        id = paramInputName.getText();
        note = paramInputNote.getText();

        if (isReferenceChosen()) {
            TokenReferenceChoice choice = (TokenReferenceChoice) paramReferenceTokenChoice.getSelectionModel().getSelectedItem();
            value = Double.toString(choice.getToken().getValueStart());
        } else if (!paramInputValue.getText().isEmpty()) {
            value = parseValue(paramInputValue.getText());
            if (value == null) {
                paramInputValue.selectAll();
                throw new InputValidationException("The given value cannot be parsed");
            }
        } else {
            throw new InputValidationException("Specify a value for the parameter");
        }

        param = new Parameter(id, note, value, type);
        try {
            dataGraphService.add(param);
        } catch (DataGraphServiceException ex) {
            paramInputName.selectAll();
            throw new InputValidationException(ex.getMessage());
        }

        paramInputName.clear();
        paramInputNote.clear();
        paramInputValue.clear();

        return param;
    }

    /**
     * Attempts to remove the given parameter.
     *
     * @param param
     * @param element
     * @throws DataGraphServiceException thrown in case the given parameter can
     *                                   not be deleted from the dao - this is
     *                                   usually when there is elements refering
     *                                   to this parameter
     */
    private void deleteParameter(Parameter param, IDataElement element) throws DataGraphServiceException {
        dataGraphService.remove(param);
        if (element != null) {
            element.getParameters().remove(param.getId());
        }
        if (param.getType() == Parameter.Type.LOCAL) {
            localParameters.remove(param);
        } else {
            globalParameters.remove(param);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        paramAddLocal.setOnAction(e -> createLocalParameter(selectedElement));
        paramAddGlobal.setOnAction(e -> createGlobalParameter());

        paramReferencePlaceChoice.valueProperty().addListener((ObservableValue obs, Object o, Object n) -> {
            System.out.println("Fire!");
            RefreshTokenChoices();
        });

        paramTableLocal.setItems(localParameters);
        paramTableLocal.setEditable(true);
        
        paramNameLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        paramValueLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        paramValueLocal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramValueLocal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    String value = parseValue(t.getNewValue());
                    if (value == null) {
                        messengerService.setRightStatus("Given value cannot be parsed!");
                    } else {
                        ((Parameter) t.getTableView()
                                .getItems()
                                .get(t.getTablePosition().getRow()))
                                .setValue(t.getNewValue());
                    }
                });
        paramNoteLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNote()));
        paramNoteLocal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramNoteLocal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setNote(t.getNewValue());
                });
        paramDeleteLocal.setCellValueFactory(cellData -> {
            Button btn = new Button();
            btn.getStyleClass().add(buttonTableRowDeleteStyle);
            btn.setOnAction(e -> {
                try {
                    deleteParameter(cellData.getValue(), selectedElement);
                    paramLocalStatusMessage.setText("Deleted LOCAL parameter '" + cellData.getValue().getId() + "'!");
                    paramLocalStatusMessage.setTextFill(Color.GREEN);
                } catch (DataGraphServiceException ex) {
                    paramLocalStatusMessage.setText(ex.getMessage());
                    paramLocalStatusMessage.setTextFill(Color.RED);
                }
            });
            return new ReadOnlyObjectWrapper(btn);
        });

        paramTableGlobal.setItems(globalParameters);
        paramTableGlobal.setEditable(true);
        
        paramNameGlobal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getId()));
        paramValueGlobal.setCellValueFactory(new PropertyValueFactory("value"));
        paramValueGlobal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramValueGlobal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setValue(t.getNewValue());
                });
        paramNoteGlobal.setCellValueFactory(new PropertyValueFactory("note"));
        paramNoteGlobal.setCellFactory(TextFieldTableCell.<Parameter>forTableColumn());
        paramNoteGlobal.setOnEditCommit(
                (CellEditEvent<Parameter, String> t) -> {
                    ((Parameter) t.getTableView().getItems().get(
                            t.getTablePosition().getRow())).setNote(t.getNewValue());
                });
        paramReferencesGlobal.setCellValueFactory(cellData -> cellData.getValue().getReferingNodesCountProperty());
        paramDeleteGlobal.setCellValueFactory(cellData -> {
            Button btn = new Button();
            btn.getStyleClass().add(buttonTableRowDeleteStyle);
            btn.setOnAction(e -> {
                try {
                    deleteParameter(cellData.getValue(), null);
                    paramGlobalStatusMessage.setText("Deleted GLOBAL parameter '" + cellData.getValue().getId() + "'!");
                    paramGlobalStatusMessage.setTextFill(Color.GREEN);
                } catch (DataGraphServiceException ex) {
                    paramGlobalStatusMessage.setText(ex.getMessage());
                    paramGlobalStatusMessage.setTextFill(Color.RED);
                }
            });
            return new ReadOnlyObjectWrapper(btn);
        });
    }

    private class PlaceReferenceChoice
    {
        private final DataPlace place;

        private PlaceReferenceChoice(DataPlace place) {
            this.place = place;
        }

        private DataPlace getPlace() {
            return place;
        }

        @Override
        public String toString() {
            return place.getName() + " (" + place.getId() + ")";
        }
    }

    private class TokenReferenceChoice
    {
        private final Token token;

        private TokenReferenceChoice(Token token) {
            this.token = token;
        }

        private Token getToken() {
            return token;
        }

        @Override
        public String toString() {
            return token.getValueStart() + " (" + token.getColour().getId() + ")";
        }
    }
}
