/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.graph;

import edu.unibi.agbi.gnius.business.controller.editor.GraphController;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.DataType;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ElementController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private FunctionBuilder functionBuilder;
    @Autowired private MessengerService messengerService;
    @Autowired private ParameterService parameterService;
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private GraphController graphController;

    // Parent Container
    @FXML private VBox elementFrame;
    @FXML private Parent identifierPane;
    @FXML private Parent propertiesPane;
    @FXML private Parent clusterPane;
    @FXML private VBox propertiesBox;

    // Properties Container
    @FXML private Parent propertiesSubtype;
    @FXML private Parent propertiesColor;
    @FXML private Parent propertiesArc;
    @FXML private Parent propertiesPlace;
    @FXML private Parent propertiesTransition;

    // Identifier
    @FXML private TextField inputType;
    @FXML private TextField inputId;
    @FXML private TextField inputName;
    @FXML private TextField inputLabel;
    @FXML private TextArea inputDescription;
    
    @FXML private Button buttonClone;
    @FXML private Button buttonDisable;
    @FXML private Button buttonDisableClustered;

    // Colour
    @FXML private ChoiceBox choiceSubtype;
    @FXML private ChoiceBox choiceColour;
//    @FXML private Button buttonColourCreate;

    // Properties
    @FXML private Button buttonEdit;
    @FXML private Button buttonEditClustered;
    @FXML private Label statusMessage;
    @FXML private ListView<IGraphElement> listClusteredElements;
    @FXML private TextArea inputTransitionFunction;
    @FXML private TextField inputArcWeight;
    @FXML private TextField inputPlaceToken;
    @FXML private TextField inputPlaceTokenMin;
    @FXML private TextField inputPlaceTokenMax;

    private IDataElement data;
    private String inputLatestValid;

    /**
     * Shows the details for the given graph element. The values are loaded from
     * the related data element.
     *
     * @param element
     */
    public void ShowElementInfo(IGraphElement element) {
        
        if (element == null) {
            return;
        }

        data = element.getData();

        LoadGuiElements(data);
        LoadElementInfo(data);
        LoadElementType(data);
        LoadElementProperties(data);
    }

    /**
     * Loads GUI elements specific for the given graph element type.
     *
     * @param element
     */
    private void LoadGuiElements(IDataElement element) {

        setDisableButton(data);
        setDisableClusteredButton(null);
        elementFrame.getChildren().clear();
        elementFrame.getChildren().add(identifierPane);
        propertiesBox.getChildren().clear();
        propertiesBox.getChildren().add(propertiesSubtype);
        propertiesBox.getChildren().add(propertiesColor);
        buttonDisableClustered.setDisable(true);
        buttonEditClustered.setDisable(true);

        switch (element.getDataType()) {
            
            case ARC:
                buttonClone.setDisable(true);
                buttonEdit.setDisable(false);
                inputLabel.setDisable(true);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(propertiesArc);
                break;

            case CLUSTER:
                buttonClone.setDisable(true);
                buttonEdit.setDisable(true);
                inputLabel.setDisable(false);
                elementFrame.getChildren().add(clusterPane);
                break;

            case CLUSTERARC:
                buttonClone.setDisable(true);
                buttonEdit.setDisable(true);
                inputLabel.setDisable(true);
                elementFrame.getChildren().add(clusterPane);
                break;

            case PLACE:
                buttonClone.setDisable(false);
                buttonEdit.setDisable(false);
                inputLabel.setDisable(false);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(propertiesPlace);
                break;

            case TRANSITION:
                buttonClone.setDisable(false);
                buttonEdit.setDisable(false);
                inputLabel.setDisable(false);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(propertiesTransition);
                break;
        }
    }

    /**
     * Loads the element type information from the given data element. Fills the
     * GUI with type information and according subtype choices.
     *
     * @param element
     */
    private void LoadElementType(IDataElement element) {

        inputType.setText(element.getDataType().toString());
        choiceSubtype.getItems().clear();

        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        int typeIndex = -1;

        switch (element.getDataType()) {

            case ARC:
                DataArc arc = (DataArc) element;
                DataArc.Type arcType;
                for (int i = 0; i < DataArc.Type.values().length; i++) {
                    arcType = DataArc.Type.values()[i];
                    if (arcType == arc.getArcType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(arcType);
                }
                break;

            case PLACE:
                DataPlace place = (DataPlace) element;
                DataPlace.Type placeType;
                for (int i = 0; i < DataPlace.Type.values().length; i++) {
                    placeType = DataPlace.Type.values()[i];
                    if (placeType == place.getPlaceType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(placeType);
                }
                break;

            case TRANSITION:
                DataTransition transition = (DataTransition) element;
                DataTransition.Type transitionType;
                for (int i = 0; i < DataTransition.Type.values().length; i++) {
                    transitionType = DataTransition.Type.values()[i];
                    if (transitionType == transition.getTransitionType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(transitionType);
                }
                break;
        }
        if (typeIndex > -1) {
            choiceSubtype.setItems(choicesSubtype);
            choiceSubtype.getSelectionModel().select(typeIndex);
        }
    }

    /**
     * Prints info for the given element to the property textfields.
     *
     * @param element
     */
    private void LoadElementInfo(IDataElement element) {
        if (element.getDescription() != null) {
            inputDescription.setText(element.getDescription());
        } else {
            inputDescription.setText("");
        }
        if (element.getLabelText() != null) {
            inputLabel.setText(element.getLabelText());
        } else {
            inputLabel.setText("");
        }
        inputId.setText(element.getId());
        inputName.setText(element.getName());
    }

    /**
     * Populates the GUI with the properties of the given element.
     *
     * @param element
     */
    private void LoadElementProperties(IDataElement element) {

        ObservableList<Colour> choicesColour = FXCollections.observableArrayList();
        Collection<Colour> colors = dataService.getModel().getColours();

        choiceColour.setItems(choicesColour); // assign upfront, input listeners accesses on text change

        switch (element.getDataType()) {

            case ARC:
                DataArc arc = (DataArc) element;
                Weight weight;
                for (Colour color : colors) {
                    if (arc.getWeight(color) != null) {
                        choicesColour.add(color);
                    }
                }
                weight = arc.getWeight(choicesColour.get(0));
                choiceColour.getSelectionModel().select(0); // must be done here for listener
                inputArcWeight.setText(weight.getValue());
                break;

            case CLUSTER:
                DataCluster cluster = (DataCluster) element;
                listClusteredElements.getItems().clear();
                cluster.getGraph().getNodes().forEach(n -> {
                    listClusteredElements.getItems().add((IGraphElement) n);
                });
                break;

            case CLUSTERARC:
                DataClusterArc clusterArc = (DataClusterArc) element;
                listClusteredElements.getItems().clear();
                clusterArc.getStoredArcs().values().forEach(a -> {
                    listClusteredElements.getItems().add(a);
                });
                break;

            case PLACE:
                DataPlace place = (DataPlace) element;
                Token token;
                for (Colour color : colors) {
                    if (place.getToken(color) != null) {
                        choicesColour.add(color);
                    }
                }
                token = place.getToken(choicesColour.get(0));
                choiceColour.getSelectionModel().select(0); // must be done here for listener
                inputPlaceToken.setText(Double.toString(token.getValueStart()));
                inputPlaceTokenMin.setText(Double.toString(token.getValueMin()));
                inputPlaceTokenMax.setText(Double.toString(token.getValueMax()));
                break;

            case TRANSITION:
                DataTransition transition = (DataTransition) element;
                inputTransitionFunction.setText(transition.getFunction().toString());
                break;
        }

    }

    private void StoreElementType(IDataElement element) throws DataServiceException {
        Object subtype = choiceSubtype.getSelectionModel().getSelectedItem();
        if (subtype != null) {
            dataService.ChangeElementSubtype(element, subtype);
        }
    }

    private void ParseArcWeight() {

        if (ValidateNumberInput(inputArcWeight)) {

            if (data instanceof DataArc) {

                DataArc arc = (DataArc) data;
                Colour colour = (Colour) choiceColour.getSelectionModel().getSelectedItem();

                try {
                    Weight weight = new Weight(colour);
                    if (!inputArcWeight.getText().isEmpty()) {
                        weight.setValue(String.valueOf(Double.parseDouble(inputArcWeight.getText().replace(",", "."))));
                    }
                    dataService.setArcWeight(arc, weight);
                } catch (NumberFormatException ex) {
                    messengerService.addException("Exception parsing weight value!", ex);
                }
            }
        }
    }

    private void ParsePlaceToken() {

        if (data instanceof DataPlace) {

            DataPlace place = (DataPlace) data;
            Colour colour = (Colour) choiceColour.getSelectionModel().getSelectedItem();

            try {
                Token token = new Token(colour);
                if (ValidateNumberInput(inputPlaceToken)) {
                    token.setValueStart(Double.parseDouble(inputPlaceToken.getText().replace(",", ".")));
                }
                if (ValidateNumberInput(inputPlaceTokenMin)) {
                    token.setValueMin(Double.parseDouble(inputPlaceTokenMin.getText().replace(",", ".")));
                }
                if (ValidateNumberInput(inputPlaceTokenMax)) {
                    token.setValueMax(Double.parseDouble(inputPlaceTokenMax.getText().replace(",", ".")));
                }
                dataService.setPlaceToken(place, token);
            } catch (NumberFormatException ex) {
                messengerService.addException("Exception parsing token values!", ex);
            }
        }
    }

    private void ParseTransitionFunction() {

        if (data instanceof DataTransition) {

            DataTransition transition = (DataTransition) data;
            String input = inputTransitionFunction.getText().replace("\n", "");

            try {
                parameterService.ValidateFunction(transition, input);
                PrettyFormulaParser.parseToImage(input);
                
                inputLatestValid = input;
                setInputStatus(inputTransitionFunction, false);
            } catch (Exception ex) {
                setInputStatus(inputTransitionFunction, true);
            }

            try {
                if (inputTransitionFunction.getText().isEmpty()) {
                    dataService.setTransitionFunction(transition, "1");
                } else {
                    dataService.setTransitionFunction(transition, inputLatestValid);
                }
            } catch (DataServiceException ex) {
                messengerService.addException("Cannot build function from input '" + inputLatestValid + "'!", ex);
            }
        }
    }

    private void setInputStatus(Control input, boolean isError) {
        if (isError) {
            input.setStyle("-fx-border-color: red");
            statusMessage.setTextFill(Color.RED);
            statusMessage.setText("Invalid!");
        } else {
            input.setStyle("-fx-border-color: green");
            statusMessage.setTextFill(Color.GREEN);
            statusMessage.setText("Valid!");
        }
    }

    private boolean ValidateNumberInput(TextField input) {
        String value = input.getText().replace(",", ".");
        if (value.matches(functionBuilder.getNumberRegex())) {
            input.setStyle("-fx-border-color: green");
            return true;
        } else {
            input.setStyle("-fx-border-color: red");
            return false;
        }
    }
    
    private void setDisableButton(IDataElement element) {
        if (element.getDataType() == DataType.CLUSTER
                || element.getDataType() == DataType.CLUSTERARC) {
            if (element.isDisabled()) {
                buttonDisable.setText("Enable All");
            } else {
                buttonDisable.setText("Disable All");
            }
        } else {
            if (element.isDisabled()) {
                buttonDisable.setText("Enable");
            } else {
                buttonDisable.setText("Disable");
            }
        }
    }
    
    private void setDisableClusteredButton(IGraphElement element) {
        if (element != null) {
            buttonDisableClustered.setDisable(false);
            buttonEditClustered.setDisable(false);
            if (element.getData().isDisabled()) {
                buttonDisableClustered.setText("Enable");
            } else {
                buttonDisableClustered.setText("Disable");
            }
        } else {
            buttonDisableClustered.setDisable(true);
            buttonEditClustered.setDisable(true);
        }
    }
    
    private String getText(TextInputControl control) {
        if (!control.isDisabled()) {
            if (control.getText() != null) {
                return control.getText();
            }
        }
        return "";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        buttonClone.setOnAction(eh -> {
            if (data instanceof IDataNode) {
                try {
                    mouseEventHandler.setCloningMode((IDataNode) data);
                } catch (Exception ex) {
                    messengerService.addException("Cannot clone node!", ex);
                }
            }
        });
        buttonDisable.setOnAction(eh -> {
            data.setDisabled(!data.isDisabled());
            setDisableButton(data);
            LoadElementProperties(data);
        });
        buttonDisableClustered.setOnAction(eh -> {
            if (listClusteredElements.getSelectionModel().getSelectedItem() != null) {
                int index = listClusteredElements.getSelectionModel().getSelectedIndex();
                IGraphElement elem = listClusteredElements.getSelectionModel().getSelectedItem();
                elem.getData().setDisabled(!elem.getData().isDisabled());
                listClusteredElements.getItems().remove(elem);
                listClusteredElements.getItems().add(index, elem);
            }
            setDisableButton(data);
            setDisableClusteredButton(listClusteredElements.getSelectionModel().getSelectedItem());
            if (data.getDataType() == DataType.CLUSTER) { // should be mandatory
                ((DataCluster) data).UpdateShape();
            }
        });
        buttonEdit.setOnAction(eh -> graphController.ShowInspector(data));
        buttonEditClustered.setOnAction(eh -> {
            if (listClusteredElements.getSelectionModel().getSelectedItem() != null) {
                IGraphElement elem = listClusteredElements.getSelectionModel().getSelectedItem();
                graphController.ShowInspector(listClusteredElements.getSelectionModel().getSelectedItem().getData());
            }
        });

        choiceSubtype.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreElementType(data);
                    setInputStatus(choiceSubtype, false);
                } catch (DataServiceException ex) {
                    setInputStatus(choiceSubtype, true);
                    messengerService.addException("Cannot change subtype!", ex);
                }
            }
        });

        inputName.textProperty().addListener(cl -> data.setName(getText(inputName)));
        inputLabel.textProperty().addListener(cl -> data.setLabelText(getText(inputLabel)));
        inputDescription.textProperty().addListener(cl -> data.setDescription(getText(inputDescription)));
        
        inputArcWeight.textProperty().addListener(cl -> ParseArcWeight());
        inputPlaceToken.textProperty().addListener(cl -> ParsePlaceToken());
        inputPlaceTokenMin.textProperty().addListener(cl -> ParsePlaceToken());
        inputPlaceTokenMax.textProperty().addListener(cl -> ParsePlaceToken());
        inputTransitionFunction.textProperty().addListener(e -> ParseTransitionFunction());
        
        listClusteredElements.setCellFactory(l -> new ClusterCellFormatter());
        listClusteredElements.getSelectionModel().selectedItemProperty().addListener(cl -> {
            setDisableClusteredButton(listClusteredElements.getSelectionModel().getSelectedItem());
        });
    }
    
    private class ClusterCellFormatter extends ListCell<IGraphElement>
    {
        @Override
        protected void updateItem(IGraphElement item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item.toString());
                setOpacity(1.0);
                if (item.getData().isDisabled()) {
                    setOpacity(0.5);
                }
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        ShowElementInfo(item);
                    }
                });
            } else {
                setText("");
            }
        }
    }
}
