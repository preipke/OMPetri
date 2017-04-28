/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.petrinet.entity.INode;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired private ElementController elementDetailsController;

    @FXML private TextField paramInputName;
    @FXML private TextField paramInputNote;
    @FXML private ChoiceBox paramReferencePlaceChoice;
    @FXML private ChoiceBox paramReferenceTokenChoice;
    @FXML private TextField paramInputValue;
    @FXML private SwingNode paramFunctionNode;

    @FXML private Button paramAddLocal;
    @FXML private Button paramAddGlobal;

    @FXML private TableView paramTableLocal;
    @FXML private TableColumn<Parameter, String> paramNameLocal;
    @FXML private TableColumn<Parameter, String> paramValueLocal;
    @FXML private TableColumn<Parameter, String> paramNoteLocal;
    @FXML private TableColumn<Parameter, Button> paramDeleteLocal;

    @FXML private TableView paramTableGlobal;
    @FXML private TableColumn<Parameter, String> paramNameGlobal;
    @FXML private TableColumn<Parameter, String> paramValueGlobal;
    @FXML private TableColumn<Parameter, String> paramNoteGlobal;
    @FXML private TableColumn<Parameter, Button> paramDeleteGlobal;

    private final ObservableList<Parameter> localParameter;
    private final ObservableList<Parameter> globalParameter;

    private IDataElement element;

    public ParameterController() {
        localParameter = FXCollections.observableArrayList();
        globalParameter = FXCollections.observableArrayList();
    }

    public void ShowParameterDetails(IDataElement elem) {
        RefreshLocalParameters(elem);
        RefreshReferenceChoices();
    }

    public List<Parameter> getParameter(IDataElement elem) {
        
        List<Parameter> parameters = new ArrayList();
        List<Parameter> tmp;
        
        tmp = new ArrayList();
        tmp.addAll(elem.getParameters());
        tmp.sort(Comparator.comparing(Parameter::toString));
        parameters.addAll(tmp);
        
        tmp.clear();
        tmp.addAll(globalParameter);
        tmp.sort(Comparator.comparing(Parameter::toString));
        parameters.addAll(tmp);

        return parameters;
    }

    /**
     * Clears and sets the available local parameters for the given element.
     * @param elem 
     */
    private void RefreshLocalParameters(IDataElement elem) {
        
        List<Parameter> parameters = new ArrayList();
        element = elem;
        
        localParameter.clear();
        if (elem != null) {
            parameters.addAll(elem.getParameters());
            parameters.sort(Comparator.comparing(Parameter::toString));
            localParameter.addAll(parameters);
        }
    }

    /**
     * Clears and sets the choices for places to choose from as value references.
     */
    private void RefreshReferenceChoices() {

        Collection<INode> places = dataGraphService.getDataDao().getPlaces();
        ObservableList<PlaceReferenceChoice> placeReferenceChoices = FXCollections.observableArrayList();

        for (INode place : places) {
            placeReferenceChoices.add(new PlaceReferenceChoice((DataPlace) place));
        }
        placeReferenceChoices.sort(Comparator.comparing(PlaceReferenceChoice::toString));

        paramReferencePlaceChoice.getItems().clear();
        paramReferencePlaceChoice.setItems(placeReferenceChoices);
        paramReferencePlaceChoice.getItems().add(0, "- no reference -");
        paramReferencePlaceChoice.getSelectionModel().select(0);
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

        PlaceReferenceChoice choice = (PlaceReferenceChoice) paramReferenceTokenChoice.getSelectionModel().getSelectedItem();

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
    private String parseValueInput() {
        String value;
        try {
            value = Double.toString(Double.parseDouble(paramInputValue.getText()));
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
            Parameter param = createParameter("LOCAL_" + elem.getId(), Parameter.Type.LOCAL);
            localParameter.add(param);
            elem.getParameters().add(param);
        } catch (DataGraphServiceException ex) {
            messengerService.addToLog("Exception creating local parameter! [" + ex.getMessage() + "]");
        }
    }

    /**
     * Creates a global parameter.
     */
    private void createGlobalParameter() {
        try {
            Parameter param = createParameter("GLOBAL_", Parameter.Type.GLOBAL);
            globalParameter.add(param);
        } catch (DataGraphServiceException ex) {
            messengerService.addToLog("Exception creating global parameter! [" + ex.getMessage() + "]");
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
    private Parameter createParameter(String id, Parameter.Type type) throws DataGraphServiceException {

        String name, note, value;
        Parameter param;

        if (!paramInputName.getText().isEmpty()) {
            name = paramInputName.getText();
            note = paramInputNote.getText();
            id = id + "_" + name;
        } else {
            throw new DataGraphServiceException("You have to specify a name for the new parameter!");
        }

        if (isReferenceChosen()) {
            TokenReferenceChoice choice = (TokenReferenceChoice) paramReferenceTokenChoice.getSelectionModel().getSelectedItem();
            value = Double.toString(choice.getToken().getValueStart());
        } else if (!paramInputValue.getText().isEmpty()) {
            value = parseValueInput();
            if (value == null) {
                throw new DataGraphServiceException("The specified value can not be parsed!");
            }
        } else {
            throw new DataGraphServiceException("You have to specify a value for the new parameter!");
        }

        param = new Parameter(id, name, note, value, type);
        dataGraphService.add(param);
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
        if (param.getType() == Parameter.Type.LOCAL) {
            element.getParameters().remove(param);
            localParameter.remove(param);
        } else {
            globalParameter.remove(param);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        paramAddLocal.setOnAction(e -> createLocalParameter(element));
        paramAddGlobal.setOnAction(e -> createGlobalParameter());

        paramReferencePlaceChoice.valueProperty().addListener((ObservableValue obs, Object o, Object n) -> RefreshTokenChoices());

        paramTableLocal.setItems(localParameter);
        paramNameLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
        paramNoteLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNote()));
        paramValueLocal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        paramDeleteLocal.setCellValueFactory(cellData -> {
            Button btn = new Button();
            btn.setOnAction(e -> {
                try {
                    deleteParameter(cellData.getValue(), element);
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
                }
            });
            return new ReadOnlyObjectWrapper(btn);
        });

        paramTableGlobal.setItems(globalParameter);
        paramNameGlobal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
        paramNoteGlobal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNote()));
        paramValueGlobal.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getValue()));
        paramDeleteGlobal.setCellValueFactory(cellData -> {
            Button btn = new Button();
            btn.setOnAction(e -> {
                try {
                    deleteParameter(cellData.getValue(), element);
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
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
