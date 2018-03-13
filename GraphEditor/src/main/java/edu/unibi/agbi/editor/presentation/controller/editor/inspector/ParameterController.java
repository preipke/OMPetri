/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.inspector;

import edu.unibi.agbi.editor.business.exception.InputValidationException;
import edu.unibi.agbi.editor.business.exception.ParameterException;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ParameterService;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.util.FunctionFactory;
import edu.unibi.agbi.petrinet.util.ParameterFactory;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Controller
public class ParameterController implements Initializable
{
    @Autowired private FunctionFactory functionBuilder;
    @Autowired private ParameterFactory parameterFactory;
    @Autowired private MessengerService messengerService;
    @Autowired private ParameterService parameterService;

//    @Value("${regex.param.ident.flowIn.actual}") private String regexParamIdentFlowInActual;
//    @Value("${regex.param.ident.flowIn.total}") private String regexParamIdentFlowInTotal;
//    @Value("${regex.param.ident.flowOut.actual}") private String regexParamIdentFlowOutActual;
//    @Value("${regex.param.ident.flowOut.total}") private String regexParamIdentFlowOutTotal;
    @Value("${regex.param.ident.speed}") private String regexParamIdentSpeed;
    @Value("${regex.param.ident.token}") private String regexParamIdentToken;

    @FXML private Button buttonApply;
    @FXML private Button buttonCreate;
    @FXML private Button buttonClear;
    @FXML private Button buttonEdit;
    @FXML private Button buttonRemove;
    @FXML private ChoiceBox<Parameter.Type> choiceScope;
    @FXML private ChoiceBox<IDataElement> choiceRelatedElement;
    @FXML private ListView<Parameter> listParameters;
    @FXML private TextField inputName;
    @FXML private TextField inputUnit;
    @FXML private TextField inputValue;
    @FXML private TextField inputFilterNode;
    @FXML private TextField inputFilterParam;

    private IDataElement data;

    private PauseTransition pauseTransition;

    public void setElement(IDataElement element) {
        data = element;
        fillParameterList(element);
        // TODO avoid redundant reloading of available parameters
    }

    public void ClearInputs() {
        buttonApply.setDisable(true);
        buttonCreate.setDisable(true);
        if (listParameters.getSelectionModel().selectedItemProperty() != null) {
            buttonEdit.setDisable(false);
            buttonRemove.setDisable(false);
        } else {
            buttonEdit.setDisable(true);
            buttonRemove.setDisable(true);
        }
        inputName.setText("");
        inputUnit.setText("");
        inputValue.setText("");
        inputFilterNode.setText("");
        choiceScope.getSelectionModel().selectFirst();
        choiceRelatedElement.getItems().clear();
    }

    private void ShowParameter(Parameter param) {

        if (param == null) {
            return;
        }

        inputName.setText(param.getId());
        if (param.getUnit() != null) {
            inputUnit.setText(param.getUnit());
        } else {
            inputUnit.setText("");
        }
        inputValue.setText(param.getValue());
        choiceScope.getSelectionModel().select(param.getType());
        if (choiceScope.getSelectionModel().getSelectedItem() == Parameter.Type.LOCAL) {
            choiceRelatedElement.getSelectionModel().select((DataTransition) param.getRelatedElement());
        }

        buttonApply.setDisable(true);
        buttonCreate.setDisable(true);
    }

    private void fillParameterList(IDataElement element) {
        listParameters.setStyle("");
        listParameters.getItems().clear();
        listParameters.getItems().addAll(parameterService.getFilteredAndSortedParameterList(element, inputFilterParam.getText().toLowerCase()));
    }

    private void RemoveParameter(Parameter param) {
        try {
            parameterService.remove(param);
            listParameters.getItems().remove(param);
            listParameters.setStyle("-fx-border-color: green");
        } catch (ParameterException ex) {
            listParameters.setStyle("-fx-border-color: red");
            messengerService.setRightStatus("Cannot delete parameter.", ex);
        }
    }
    
    private String validateAndGetParameterId() {
        String id = inputName.getText();
        try {
            if (!isNameInputValid()) {
                throw new InputValidationException("Cannot create parameter using invalid name!");
            }
        } catch (InputValidationException ex) {
            messengerService.addException(ex);
            return null;
        }
        return id;
    }
    
    private String validateAndGetParameterValue() {
        String value = inputValue.getText();
        try {
            value = value.replace(",", ".");
            if (value.isEmpty() | !value.matches(functionBuilder.getNumberRegex())) {
                throw new InputValidationException("");
            }
            inputValue.setStyle("");
        } catch (InputValidationException ex) {
            inputValue.setStyle("-fx-border-color: red");
            messengerService.setRightStatus(
                    "Cannot create parameter with invalid value!", 
                    ex);
            return null;
        }
        return value;
    }

    private void CreateParameter() {

        Parameter param;

        String id = validateAndGetParameterId();
        String value = validateAndGetParameterValue();
        String unit = inputUnit.getText();
        
        Parameter.Type scope = choiceScope.getSelectionModel().getSelectedItem();
        IDataElement element = choiceRelatedElement.getSelectionModel().getSelectedItem();

        // Validate Scope and Related
        if (scope == Parameter.Type.LOCAL) {
            if (element == null) {
                choiceScope.setStyle("-fx-border-color: red");
                choiceRelatedElement.setStyle("-fx-border-color: red");
                messengerService.setRightStatus(
                        "Cannot create local parameter without specifying a related node!", 
                        new ParameterException("Must specify a related node for local parameters."));
                return;
            }
        }
        choiceScope.setStyle("");
        choiceRelatedElement.setStyle("");

        try {
            
            switch (scope) {
                
                case GLOBAL:
                    param = parameterFactory.createGlobalParameter(id, value, unit);
                    break;

                case LOCAL:
                    param = parameterFactory.createLocalParameter(id, value, unit, element);
                    break;
                    
                default:
                    throw new ParameterException("Cannot create parameter other than local or global!");
                
            }

            parameterService.add(param);
            buttonCreate.setDisable(true);
            
            fillParameterList(data);
            listParameters.setStyle("-fx-border-color: green");
            
        } catch (ParameterException ex) {
            
            messengerService.setRightStatus("Parameter creation failed!", ex);
            listParameters.setStyle("-fx-border-color: red");
        }
    }
    
    private void UpdateParameter() {

        Parameter param;

        String id = validateAndGetParameterId();
        String value = validateAndGetParameterValue();
        String unit = inputUnit.getText();

        param = parameterService.findParameter(id, data);
        if (param != null) {
            
            try {
                parameterService.updateParameter(param, value, unit);
                buttonApply.setDisable(true);
                listParameters.setStyle("-fx-border-color: green");
                
                fillParameterList(data); // update parameter list to show changes
                
            } catch (ParameterException ex) {
                
                messengerService.setRightStatus("Applying parameter changes failed!", ex);
                listParameters.setStyle("-fx-border-color: red");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(e -> fillParameterList(data));

        buttonApply.setOnAction(eh -> UpdateParameter());
        buttonCreate.setOnAction(eh -> CreateParameter());
        buttonClear.setOnAction(eh -> ClearInputs());
        buttonEdit.setOnAction(eh -> ShowParameter(listParameters.getSelectionModel().getSelectedItem()));
        buttonRemove.setOnAction(eh -> RemoveParameter(listParameters.getSelectionModel().getSelectedItem()));

        inputName.textProperty().addListener(cl -> enableParameterControlButtons());
        inputValue.textProperty().addListener(cl -> enableParameterControlButtons());
        inputUnit.textProperty().addListener(cl -> enableParameterControlButtons());

        choiceScope.getItems().add(Parameter.Type.GLOBAL);
        choiceScope.getItems().add(Parameter.Type.LOCAL);
        choiceScope.getSelectionModel().selectFirst();
        choiceScope.getSelectionModel().selectedItemProperty().addListener(cl -> {
            setReferenceChoices();
            enableParameterControlButtons();
        });
        choiceRelatedElement.getSelectionModel().selectedItemProperty().addListener(cl -> enableParameterControlButtons());

        inputFilterNode.textProperty().addListener(cl -> setReferenceChoices());
        inputFilterParam.textProperty().addListener(cl -> pauseTransition.playFromStart());

        listParameters.setCellFactory(l -> new ParameterCellFormatter());
        listParameters.getSelectionModel().selectedItemProperty().addListener(cl -> {
            listParameters.setStyle("");
            if (listParameters.getSelectionModel().selectedItemProperty() != null) {
                buttonEdit.setDisable(false);
                buttonRemove.setDisable(false);
            } else {
                buttonEdit.setDisable(true);
                buttonRemove.setDisable(true);
            }
        });
    }

    private void setReferenceChoices() {
        
        if (choiceScope.getSelectionModel().getSelectedItem() != null) {
            choiceRelatedElement.getItems().clear();
            
            if (choiceScope.getSelectionModel().getSelectedItem() == Parameter.Type.LOCAL) {
                
                choiceRelatedElement.setDisable(false);
                inputFilterNode.setDisable(false);
                
                choiceRelatedElement.getItems().addAll(
                        parameterService.getFilteredChoicesForLocalParameters(
                                inputFilterNode.getText().toLowerCase())); 
                
                if (data != null &&
                        data instanceof DataTransition || 
                        data instanceof DataArc) {
                    
                    choiceRelatedElement.getSelectionModel().select((IDataElement) data);
                }
                
            } else {
                choiceRelatedElement.setDisable(true);
                inputFilterNode.setDisable(true);
            }
        }
    }

    private boolean isNameInputValid() {

        String id = inputName.getText();
        Parameter param = parameterService.findParameter(id, data);
        
        if (param != null) { // parameter with given id either exists globally / locally or is a reference
            
            switch (param.getType()) {
                
                case GLOBAL:
                    return true;
                
                case LOCAL:
                    return true;
                    
                default: // reference
                    inputName.setStyle("-fx-border-color: red");
                    messengerService.setRightStatus("Parameter ID is already used by another element!");
                    return false;
            }
            
        } else {

            // Validate ID format
            
            if (id.isEmpty() | !id.matches(functionBuilder.getParameterRegex())) {
                inputName.setStyle("-fx-border-color: red");
                messengerService.setRightStatus("Cannot create parameter using a restricted name format!");
                return false;
            }

            String[] restrictedRegex = new String[]{
//                regexParamIdentFlowInActual, regexParamIdentFlowInTotal,
//                regexParamIdentFlowOutActual, regexParamIdentFlowOutTotal,
                regexParamIdentSpeed, regexParamIdentToken
            };

            for (String regex : restrictedRegex) {
                if (id.matches(regex)) {
                    inputName.setStyle("-fx-border-color: red");
                    messengerService.setRightStatus("Cannot create parameter using a restricted name format!");
                    return false;
                }
            }
        }

        messengerService.setRightStatus("");
        inputName.setStyle("-fx-border-color: green");
        return true;
    }

    private void enableParameterControlButtons() {

        buttonApply.setDisable(true);
        buttonCreate.setDisable(true);

        if (inputName.getText() != null) {
            if (!inputName.getText().isEmpty()) {
                if (isNameInputValid()) {

                    if (choiceScope.getSelectionModel()
                            .getSelectedItem() == Parameter.Type.LOCAL) {

                        if (choiceRelatedElement.getSelectionModel()
                                .getSelectedItem() != null) {
                            
                            if (choiceRelatedElement.getSelectionModel()
                                    .getSelectedItem()
                                    .getLocalParameter(inputName.getText()) != null) {
                                buttonApply.setDisable(false);
                            } else {
                                buttonCreate.setDisable(false);
                            }
                        }

                    } else {

                        if (parameterService.getParameter(inputName.getText()) != null) {
                            buttonApply.setDisable(false);
                        } else {
                            buttonCreate.setDisable(false);
                        }
                    }
                }
            }
        }
    }

    private class ParameterCellFormatter extends ListCell<Parameter>
    {
        @Override
        protected void updateItem(Parameter item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item.toString());
                setOpacity(1.0);
                if (item.getType() == Parameter.Type.LOCAL) {
                    if (data != null && item.getRelatedElement().equals(data)) {
                        setText("> " + item.toString());
                    } else {
                        setOpacity(0.5);
                    }
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        ShowParameter(item);
                    }
                });
            } else {
                setText("");
            }
        }
    }
}
