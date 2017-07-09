/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.details;

import edu.unibi.agbi.gnius.core.exception.InputValidationException;
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
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
    @Autowired private FunctionBuilder functionBuilder;
    @Autowired private MessengerService messengerService;
    @Autowired private ParameterService parameterService;

    @Value("${regex.param.ident.flowIn.actual}") private String regexParamIdentFlowInActual;
    @Value("${regex.param.ident.flowIn.total}") private String regexParamIdentFlowInTotal;
    @Value("${regex.param.ident.flowOut.actual}") private String regexParamIdentFlowOutActual;
    @Value("${regex.param.ident.flowOut.total}") private String regexParamIdentFlowOutTotal;
    @Value("${regex.param.ident.speed}") private String regexParamIdentSpeed;
    @Value("${regex.param.ident.token}") private String regexParamIdentToken;
    
    @FXML private Button buttonApply;
    @FXML private Button buttonCreate;
    @FXML private Button buttonClear;
    @FXML private Button buttonEdit;
    @FXML private Button buttonRemove;
    @FXML private ChoiceBox<Parameter.Type> choiceScope;
    @FXML private ChoiceBox<DataTransition> choiceNode;
    @FXML private ListView<Parameter> listParameters;
    @FXML private TextField inputName;
    @FXML private TextField inputUnit;
    @FXML private TextField inputValue;
    @FXML private TextField inputFilterNode;
    @FXML private TextField inputFilterParam;
    
    private IDataElement data;
    
    private PauseTransition pauseTransition;
    
    public void setElement(IDataElement element) {
        IDataElement dataOld = data;
        data = element;
        if (dataOld == null) {
            setParameters(element);
        } else {
            if (element.getElementType() == Element.Type.TRANSITION) {
                if (((DataTransition) element).getParameters().size() > 0) {
                    setParameters(element);
                } else {
                    if (dataOld.getElementType() == Element.Type.TRANSITION) {
                        if (((DataTransition) dataOld).getParameters().size() > 0) {
                            setParameters(element);
                        }
                    }
                }
            } else {
                if (dataOld.getElementType() == Element.Type.TRANSITION) {
                    if (((DataTransition) dataOld).getParameters().size() > 0) {
                        setParameters(element);
                    }
                }
            }
        }
    }
    
    public void Clear() {
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
        choiceNode.getItems().clear();
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
        
        buttonApply.setDisable(true);
        buttonCreate.setDisable(true);
    }
    
    private void setParameters(IDataElement element) {
        listParameters.getItems().clear();
        listParameters.getItems().addAll(parameterService.getFilteredAndSortedParameterList(element, inputFilterParam.getText().toLowerCase()));
    }
    
    private void RemoveParameter(Parameter param) {
        try {
            parameterService.remove(param);
            listParameters.getItems().remove(param);
            listParameters.setStyle("-fx-border-color: green");
        } catch (ParameterServiceException ex) {
            listParameters.setStyle("-fx-border-color: red");
            messengerService.setRightStatus("Cannot delete parameter.", ex);
        }
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
    private void ValidateAndCreateParameter() {

        Parameter param;
        
        String id = inputName.getText();
        String value = inputValue.getText();
        String unit = inputUnit.getText();
        Parameter.Type scope = choiceScope.getSelectionModel().getSelectedItem();
        DataTransition transition = choiceNode.getSelectionModel().getSelectedItem();

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
            inputName.setStyle("");
        } catch (InputValidationException ex) {
            inputName.setStyle("-fx-border-color: red");
            messengerService.addException("Trying to create parameter with invalid name!", ex);
            return;
        }

        // Validate value
        try {
            value = value.replace(",", ".");
            if (value.isEmpty() | !value.matches(functionBuilder.getNumberRegex())) {
                throw new InputValidationException("");
            }
            inputValue.setStyle("");
        } catch (InputValidationException ex) {
            inputValue.setStyle("-fx-border-color: red");
            messengerService.addException("Trying to create parameter with invalid value!", ex);
            return;
        }

        // Scope and Related
        if (scope == Parameter.Type.LOCAL) {
            if (transition == null) {
                choiceScope.setStyle("-fx-border-color: red");
                choiceNode.setStyle("-fx-border-color: red");
                messengerService.addException("Trying to create local parameter without specifying a node!", new ParameterServiceException("Must specify a related node for local parameters."));
                return;
            }
        }
        choiceScope.setStyle("");
        choiceNode.setStyle("");
        
        try {
            if (scope == Parameter.Type.LOCAL) {
                param = transition.getParameter(id);
            } else {
                param = parameterService.getParameter(id);
            }
            if (param != null) {
                param.setValue(value);
                param.setUnit(value);
            } else {
                parameterService.add(new Parameter(id, value, unit, scope, transition), transition);
                buttonCreate.setDisable(true);
            }
            setParameters(data);
            listParameters.setStyle("-fx-border-color: green");
        } catch (ParameterServiceException ex) {
            messengerService.addException("Parameter creation failed!", ex);
            listParameters.setStyle("-fx-border-color: red");
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(e -> setParameters(data));

        buttonApply.setOnAction(eh -> ValidateAndCreateParameter());
        buttonCreate.setOnAction(eh -> ValidateAndCreateParameter());
        buttonClear.setOnAction(eh -> Clear());
        buttonEdit.setOnAction(eh -> ShowParameter(listParameters.getSelectionModel().getSelectedItem()));
        buttonRemove.setOnAction(eh -> RemoveParameter(listParameters.getSelectionModel().getSelectedItem()));
                
        inputName.textProperty().addListener(cl -> setButtons());
        inputValue.textProperty().addListener(cl -> setButtons());
        inputUnit.textProperty().addListener(cl -> setButtons());
        
        choiceScope.getItems().add(Parameter.Type.GLOBAL);
        choiceScope.getItems().add(Parameter.Type.LOCAL);
        choiceScope.getSelectionModel().selectedItemProperty().addListener(cl -> {
            setReferenceChoices();
            setButtons();
        });
        
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
            choiceNode.getItems().clear();
            if (choiceScope.getSelectionModel().getSelectedItem() == Parameter.Type.LOCAL) {
                choiceNode.setDisable(false);
                inputFilterNode.setDisable(false);
                choiceNode.getItems().addAll(parameterService.getReferenceChoices(inputFilterNode.getText().toLowerCase())); // populate node choice method
                if (data != null && data instanceof DataTransition) {
                    choiceNode.getSelectionModel().select((DataTransition) data);
                }
            } else {
                choiceNode.setDisable(true);
                inputFilterNode.setDisable(true);
            }
        }
    }
    
    private void setButtons() {
        buttonApply.setDisable(true);
        buttonCreate.setDisable(true);
        if (inputName.getText() != null) {
            if (!inputName.getText().isEmpty()) {
                if (choiceScope.getSelectionModel().getSelectedItem() == Parameter.Type.LOCAL) {
                    if (choiceNode.getSelectionModel().getSelectedItem() != null) {
                        if (choiceNode.getSelectionModel().getSelectedItem().getParameter(inputName.getText()) != null) {
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
                    if (event.getClickCount() > 1) {
                        ShowParameter(item);
                    }
                });
            } else {
                setText("");
            }
        }
    }
}
