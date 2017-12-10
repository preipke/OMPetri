/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.graph;

import edu.unibi.agbi.editor.presentation.controller.editor.GraphController;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataCluster;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ParameterService;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.impl.Place;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionFactory;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ElementController implements Initializable
{
    @Autowired private ModelService modelService;
    @Autowired private FunctionFactory functionFactory;
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
    @FXML private Parent parentSubtype;
    @FXML private Parent parentColor;
    @FXML private Parent parentConflictType;
    @FXML private Parent parentConflictValue;
    @FXML private Parent parentFunction;
    @FXML private Parent parentToken;

    // Identifier
    @FXML private TextField inputType;
    @FXML private TextField inputName;
    @FXML private TextField inputLabel;
    @FXML private TextArea inputDescription;
    
    @FXML private Button buttonClone;
    @FXML private SplitMenuButton buttonDisable;
    @FXML private MenuItem buttonDisableAll;
    @FXML private MenuItem buttonEnableAll;
    @FXML private Button buttonDisableClustered;

    // Colour
    @FXML private ChoiceBox choiceSubtype;
    @FXML private ChoiceBox choiceColour;
//    @FXML private Button buttonColourCreate;

    // Other Properties
    @FXML private Button buttonEdit;
    @FXML private Button buttonEditClustered;
    @FXML private CheckBox checkConstant;
    @FXML private ListView<IGraphElement> listClusteredElements;
    
    @FXML private TextArea inputFunction;
    @FXML private TextField inputToken;
    @FXML private TextField inputTokenMin;
    @FXML private TextField inputTokenMax;
    
    @FXML private ChoiceBox<Place.ConflictResolutionType> choiceConflictRes;
    @FXML private TextField inputConflictType;
    
    @FXML private HBox boxConflictValue;
    @FXML private TextField inputConflictValue;
    @FXML private ChoiceBox<String> choiceConflictValue;

    private IGraphElement element;
    private IDataElement data;

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

        this.element = element;
        this.data = element.getData();
        System.out.println("Data: " + data.isDisabled());
        System.out.println("Element: " + element.isElementDisabled());

        LoadGuiElements(element);
        LoadElementInfo(data);
        LoadElementType(data);
        LoadElementProperties(data);
    }

    /**
     * Loads GUI elements specific for the given graph element type.
     *
     * @param element
     */
    private void LoadGuiElements(IGraphElement element) {

        setDisableButton(element);
        setDisableClusteredButton(null);
        elementFrame.getChildren().clear();
        elementFrame.getChildren().add(identifierPane);
        propertiesBox.getChildren().clear();
        propertiesBox.getChildren().add(parentSubtype);
        buttonDisableClustered.setDisable(true);
        buttonEditClustered.setDisable(true);

        switch (element.getData().getType()) {
            
            case ARC:
                buttonClone.setDisable(true);
                buttonEdit.setDisable(false);
                inputLabel.setDisable(true);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(parentColor);
                propertiesBox.getChildren().add(parentFunction);
                propertiesBox.getChildren().add(parentConflictValue);
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
                propertiesBox.getChildren().add(parentColor);
                propertiesBox.getChildren().add(parentToken);
                propertiesBox.getChildren().add(parentConflictType);
                break;

            case TRANSITION:
                buttonClone.setDisable(false);
                buttonEdit.setDisable(false);
                inputLabel.setDisable(false);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(parentFunction);
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

        inputType.setText(element.getType().toString());
        choiceSubtype.getItems().clear();

        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        int typeIndex = -1;

        switch (element.getType()) {

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
        inputName.setText(element.getId());
    }

    /**
     * Populates the GUI with the properties of the given element.
     *
     * @param element
     */
    private void LoadElementProperties(IDataElement element) {

        ObservableList<Colour> choicesColour = FXCollections.observableArrayList();
        Collection<Colour> colors = modelService.getModel().getColours();

        choiceColour.setItems(choicesColour); // assign upfront, input listeners accesses on text change

        switch (element.getType()) {

            case ARC:
                DataArc arc = (DataArc) element;
                DataPlace placeRelated; // related place to arc
                List<IArc> neighboringArcs; // list of incoming or outgoing arcs that contains this arc
                
                if (arc.getSource().getType() == DataType.PLACE) {
                    placeRelated = (DataPlace) arc.getSource();
                    neighboringArcs = placeRelated.getArcsOut();
                } else {
                    placeRelated = (DataPlace) arc.getTarget();
                    neighboringArcs = placeRelated.getArcsIn();
                }
                
                Weight weight;
                for (Colour color : colors) {
                    if (arc.getWeight(color) != null) {
                        choicesColour.add(color);
                    }
                }
                weight = arc.getWeight(choicesColour.get(0));
                choiceColour.getSelectionModel().select(0); // must be done here for listener
                
                inputFunction.setText(weight.getFunction().toString());
                inputConflictType.setText(placeRelated.getConflictResolutionType().toString());
                
                boxConflictValue.getChildren().clear();
                switch (placeRelated.getConflictResolutionType()) {
                    
                    case PRIORITY:
                        boxConflictValue.getChildren().add(choiceConflictValue);
                        choiceConflictValue.getItems().clear();
                        
                        int targetIndex = 0;
                        for (int i = 0; i < neighboringArcs.size(); i++) {
                            if (neighboringArcs.get(i).equals(arc)) {
                                targetIndex = i;
                            }
                            choiceConflictValue.getItems().add(Integer.toString(i + 1));
                        }
                        choiceConflictValue.getSelectionModel().select(targetIndex);
                        break;
                        
                    case PROBABILITY:
                        boxConflictValue.getChildren().add(inputConflictValue);
                        inputConflictValue.setText(Double.toString(arc.getConflictResolutionValue()));
                        break;
                }
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
                listClusteredElements.getItems().addAll(clusterArc.getStoredArcs().values());
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
                checkConstant.setSelected(place.isConstant());
                choiceColour.getSelectionModel().select(0); // must be done here for listener
                inputToken.setText(Double.toString(token.getValueStart()));
                inputTokenMin.setText(Double.toString(token.getValueMin()));
                inputTokenMax.setText(Double.toString(token.getValueMax()));
                choiceConflictRes.getSelectionModel().select(place.getConflictResolutionType());
                break;

            case TRANSITION:
                DataTransition transition = (DataTransition) element;
                inputFunction.setText(transition.getFunction().toString());
                break;
        }

    }

    private void StoreElementType(IDataElement element) throws DataException {
        Object subtype = choiceSubtype.getSelectionModel().getSelectedItem();
        if (subtype != null) {
            modelService.changeSubtype(element, subtype);
        }
    }
    
    private void StoreConflictResolutionType(IDataElement element) throws DataException {
        if (element instanceof DataPlace) {
            DataPlace place = (DataPlace) element;

            Place.ConflictResolutionType conflictResType = choiceConflictRes.getSelectionModel().getSelectedItem();
            if (conflictResType != null) {
                modelService.ChangeConflictResolutionType(modelService.getDao(), place, conflictResType);
            }
        }
    }
    
    private void StoreConflictResolutionValue(IDataElement element) throws DataException {
        if (element instanceof DataArc) {
            DataArc arc = (DataArc) element;

            int priority = choiceConflictValue.getSelectionModel().getSelectedIndex();
            if (priority > -1) {
                modelService.ChangeConflictResolutionPriority(modelService.getDao(), arc, priority);
            }
        }
    }
    
    private void ParseConstantChoice() {
        if (data instanceof IDataNode) {
            IDataNode node = (IDataNode) data;
            if (node.isConstant() != checkConstant.isSelected()) {
                node.setConstant(checkConstant.isSelected());
            }
        }
    }

    private void ParsePlaceToken() {

        if (data instanceof DataPlace) {

            DataPlace place = (DataPlace) data;
            Colour colour = (Colour) choiceColour.getSelectionModel().getSelectedItem();

            try {
                Token token = new Token(colour);
                if (isNumberInputValid(inputToken)) {
                    token.setValueStart(Double.parseDouble(inputToken.getText().replace(",", ".")));
                }
                if (isNumberInputValid(inputTokenMin)) {
                    token.setValueMin(Double.parseDouble(inputTokenMin.getText().replace(",", ".")));
                }
                if (isNumberInputValid(inputTokenMax)) {
                    token.setValueMax(Double.parseDouble(inputTokenMax.getText().replace(",", ".")));
                }
                modelService.setPlaceToken(place, token);
                if (token.getValueStart() != 0) {
                    place.setTokenLabelText(Double.toString(token.getValueStart()));
                } else {
                    place.setTokenLabelText("");
                }
            } catch (NumberFormatException ex) {
                messengerService.addException("Exception parsing token values!", ex);
            }
        }
    }
    
    private void ParseConflictResolutionValue() {
        if (data instanceof DataArc) {
            DataArc arc = (DataArc) data;
            try {
                if (isNumberInputValid(inputConflictValue)) {
                    arc.setConflictResolutionValue(Double.parseDouble(inputConflictValue.getText().replace(",", ".")));
                }
            } catch (NumberFormatException ex) {
                messengerService.addException("Exception parsing conflict resolution value!", ex);
            }
        }
    }

    private void ParseAndSetFunction() {

        Function func;
        String input;
        
        if (inputFunction.getText().isEmpty()) {
            input = "1";
        } else {
            input = inputFunction.getText().replace("\n", "");
        }

        try {
            func = parameterService.validateAndGetFunction(data, input);
            PrettyFormulaParser.parseToImage(input); // just for synthax validation
            
            modelService.setElementFunction(data, func, (Colour) choiceColour.getSelectionModel().getSelectedItem());
            inputFunction.setStyle("-fx-border-color: green");
            
        } catch (Exception ex) {
            inputFunction.setStyle("-fx-border-color: red");
            messengerService.setLeftStatus("Cannot build function! " + ex.getMessage());
        }
    }

    private boolean isNumberInputValid(TextField input) {
        String value = input.getText().replace(",", ".");
        if (value.matches(functionFactory.getNumberRegex())) {
            input.setStyle("-fx-border-color: green");
            return true;
        } else {
            input.setStyle("-fx-border-color: red");
            return false;
        }
    }
    
    private void setDisableButton(IGraphElement element) {
        buttonDisable.getItems().clear();
        if (data.getType() == DataType.CLUSTER
                || data.getType() == DataType.CLUSTERARC) {
            if (data.isDisabled()) {
                buttonDisable.setText("Enable All");
            } else {
                buttonDisable.setText("Disable All");
            }
        } else {
            if (data.getShapes().size() > 1) {
                buttonDisable.getItems().add(buttonDisableAll);
                buttonDisable.getItems().add(buttonEnableAll);
            }
            if (element.isElementDisabled()) {
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
            if (data.getShapes().size() == 1) {
                data.setDisabled(!data.isDisabled());
            } else {
                element.setElementDisabled(!element.isElementDisabled());
            }
            setDisableButton(element);
            LoadElementProperties(data);
        });
        buttonDisableAll.setOnAction(eh -> data.setDisabled(true));
        buttonEnableAll.setOnAction(eh -> data.setDisabled(false));
        buttonEdit.setOnAction(eh -> graphController.ShowInspector(data));
        buttonEditClustered.setOnAction(eh -> {
            if (listClusteredElements.getSelectionModel().getSelectedItem() != null) {
                IGraphElement elem = listClusteredElements.getSelectionModel().getSelectedItem();
                graphController.ShowInspector(listClusteredElements.getSelectionModel().getSelectedItem().getData());
            }
        });
        
        buttonDisableClustered.setOnAction(eh -> {
            if (listClusteredElements.getSelectionModel().getSelectedItem() != null) {
                int index = listClusteredElements.getSelectionModel().getSelectedIndex();
                IGraphElement elem = listClusteredElements.getSelectionModel().getSelectedItem();
                elem.setElementDisabled(!elem.isElementDisabled());
                listClusteredElements.getItems().remove(elem);
                listClusteredElements.getItems().add(index, elem);
            }
            setDisableButton(element);
            setDisableClusteredButton(listClusteredElements.getSelectionModel().getSelectedItem());
            if (data.getType() == DataType.CLUSTER) { // should be mandatory
                ((DataCluster) data).UpdateShape();
            }
        });

        choiceSubtype.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreElementType(data);
                    choiceSubtype.setStyle("-fx-border-color: green");
                } catch (DataException ex) {
                    choiceSubtype.setStyle("-fx-border-color: red");
                    messengerService.addException("Cannot change subtype!", ex);
                }
            }
        });
        
        choiceConflictRes.getItems().clear();
        for (Place.ConflictResolutionType type : Place.ConflictResolutionType.values()) {
            choiceConflictRes.getItems().add(type);
        }
        choiceConflictRes.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreConflictResolutionType(data);
                    choiceConflictRes.setStyle("-fx-border-color: green");
                } catch (DataException ex) {
                    choiceConflictRes.setStyle("-fx-border-color: red");
                    messengerService.addException("Cannot change conflict resolution type!", ex);
                }
            }
        });
        choiceConflictValue.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreConflictResolutionValue(data);
                    choiceConflictValue.setStyle("-fx-border-color: green");
                } catch (DataException ex) {
                    choiceConflictValue.setStyle("-fx-border-color: red");
                    messengerService.addException("Cannot change conflict resolution value!", ex);
                }
            }
        });

        inputName.textProperty().addListener(cl -> {
            try {
                modelService.changeElementId(data, inputName.getText());
                inputName.setStyle("-fx-border-color: green");
            } catch (DataException ex) {
                inputName.setStyle("-fx-border-color: red");
                messengerService.addException("Cannot change element name!", ex);
            }
        });
        inputLabel.textProperty().addListener(cl -> data.setLabelText(getText(inputLabel)));
        inputDescription.textProperty().addListener(cl -> data.setDescription(getText(inputDescription)));
        
//        inputArcWeight.textProperty().addListener(cl -> ParseArcWeight());
        checkConstant.selectedProperty().addListener(cl -> ParseConstantChoice());
        inputToken.textProperty().addListener(cl -> ParsePlaceToken());
        inputTokenMin.textProperty().addListener(cl -> ParsePlaceToken());
        inputTokenMax.textProperty().addListener(cl -> ParsePlaceToken());
        inputFunction.textProperty().addListener(e -> ParseAndSetFunction());
        inputConflictValue.textProperty().addListener(cl -> ParseConflictResolutionValue());
        
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
                if (item.isElementDisabled()) {
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
