/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorDetailsController implements Initializable
{
    @Autowired private DataGraphService dataService;
    @Autowired private SelectionService selectionService;
    
    @Autowired private EditorToolsController editorToolsController;
    
    @FXML private VBox detailsBox;
    
    @FXML private GridPane typePropertyGrid;
    @FXML private TextField propertyType;
    @FXML private ChoiceBox propertySubtypeChoices;
    
    @FXML private VBox nodePropertiesBox;
    
    @FXML private GridPane colorSelectionGrid;
    @FXML private ChoiceBox propertyColorChoices;
    @FXML private Button colorButton;
    
    @FXML private VBox idPropertyBox;
    @FXML private TextField propertyName;
    @FXML private TextField propertyLabel;
    @FXML private TextArea propertyDescription;
    
    @FXML private GridPane transitionPropertyGrid;
    @FXML private TextField propertyFunction;
    
    @FXML private GridPane placePropertyGrid;
    @FXML private TextField propertyToken;
    @FXML private TextField propertyTokenMin;
    @FXML private TextField propertyTokenMax;
    
    @FXML private GridPane arcPropertyGrid;
    @FXML private TextField propertyWeight;
    
    private IDataElement selectedDataNode;
    
    /**
     * Shows the properties for the given graph element. The values are
     * extracted from the related data element.
     * 
     * @param element 
     */
    public void ShowElementProperties(IGraphElement element) {
        
        nodePropertiesBox.getChildren().clear();
        
        selectedDataNode = element.getDataElement();

        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        ObservableList<ColourChoice> choicesColour = FXCollections.observableArrayList();
        
        Collection<Colour> colors = dataService.getColours();
        
        int typeIndex = 0;
        
        switch (selectedDataNode.getElementType()) {
                
            case ARC:
                
                DataArc arc = (DataArc) selectedDataNode;
                
                DataArc.Type arcType;
                for (int i = 0; i < DataArc.Type.values().length; i++) {
                    arcType = DataArc.Type.values()[i];
                    if (arcType == arc.getArcType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(new ArcTypeChoice(arcType));
                }
                
                Map<Colour , Weight> weights = arc.getWeightMap();
                Weight weight = null;
                for (Colour color : colors) {
                    if (weights.get(color) != null) {
                        choicesColour.add(new ColourChoice(color));
                    }
                }
//                if (weight != null) {
                    weight = weights.get(choicesColour.get(0).getColour());
//                } else {
//                    weight = weights.get(null);
//                }
                propertyWeight.setText(weight.getValue());
                
                nodePropertiesBox.getChildren().add(arcPropertyGrid);
                break;
            
            case PLACE:
                
                DataPlace place = (DataPlace) selectedDataNode;
                
                DataPlace.Type placeType;
                for (int i = 0; i < DataPlace.Type.values().length; i++) {
                    placeType = DataPlace.Type.values()[i];
                    if (placeType == place.getPlaceType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(new PlaceTypeChoice(placeType));
                }
                
                printNodeInfo(place);
                
                Map<Colour , Token> tokens = place.getTokenMap();
                Token token = null;
                for (Colour color : colors) {
                    if (tokens.get(color) != null) {
                        choicesColour.add(new ColourChoice(color));
                    }
                }
//                if (token != null) {
                    token = tokens.get(choicesColour.get(0).getColour());
//                } else {
//                    token = tokens.get(null);
//                }
                propertyToken.setText(Double.toString(token.getValueStart()));
                propertyTokenMin.setText(Double.toString(token.getValueMin()));
                propertyTokenMax.setText(Double.toString(token.getValueMax()));
                
                nodePropertiesBox.getChildren().add(idPropertyBox);
                nodePropertiesBox.getChildren().add(placePropertyGrid);
                break;
                
            case TRANSITION:
                
                DataTransition transition = (DataTransition) selectedDataNode;
                
                DataTransition.Type transitionType;
                for (int i = 0; i < DataTransition.Type.values().length; i++) {
                    transitionType = DataTransition.Type.values()[i];
                    if (transitionType == transition.getTransitionType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(new TransitionTypeChoice(transitionType));
                }
                
                printNodeInfo(transition);
                
                propertyFunction.setText(transition.getFunction().toString());
                
                nodePropertiesBox.getChildren().add(idPropertyBox);
                nodePropertiesBox.getChildren().add(transitionPropertyGrid);
                break;
        }
        
        propertyType.setText(selectedDataNode.getElementType().toString());
        propertySubtypeChoices.setItems(choicesSubtype);
        propertySubtypeChoices.getSelectionModel().select(typeIndex);

        propertyColorChoices.setItems(choicesColour);
        propertyColorChoices.getSelectionModel().select(0);
        
        detailsBox.setVisible(true);
    }
    
    /**
     * Stores node properties. Stores the values from the textfields within the
     * according entity, overwrites old values.
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
     */
    public void StoreElementProperties() throws DataGraphServiceException {
        
        if (selectedDataNode == null) {
            return;
        }
        
        // TODO
        // store values according to the selected colour
        
        Element.Type elementType = selectedDataNode.getElementType();
        
        ColourChoice colourChoice;
        Colour colour;
        
        switch (elementType) {
                
            case ARC:
                
                colourChoice = (ColourChoice)propertyColorChoices.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();
                
                DataArc arc = (DataArc) selectedDataNode;
                
                ArcTypeChoice arcTypeChoice = (ArcTypeChoice) propertySubtypeChoices.getSelectionModel().getSelectedItem();
                DataArc.Type arcType = arcTypeChoice.getType();
                
                if (arc.getArcType() != arcType) {
                    dataService.setArcTypeDefault(arcType);
                    dataService.setTypeFor(arc, arcType);
                }
                
                Weight weight = new Weight(colour);
                if (!propertyWeight.getText().isEmpty()) {
                    weight.setValue(propertyWeight.getText());
                }
                arc.setWeight(weight);
                
                break;
            
            case PLACE:
                
                colourChoice = (ColourChoice)propertyColorChoices.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();
                
                DataPlace place = (DataPlace) selectedDataNode;
                
                PlaceTypeChoice placeTypeChoice = (PlaceTypeChoice) propertySubtypeChoices.getSelectionModel().getSelectedItem();
                DataPlace.Type placeType = placeTypeChoice.getType();
                
                if (place.getPlaceType() != placeType) {
                    dataService.setPlaceTypeDefault(placeType);
                    dataService.setTypeFor(place, placeType);
                }
                
                Token token = new Token(colour);
                if (!propertyToken.getText().isEmpty()) {
                    try {
                        token.setValueStart(Double.parseDouble(propertyToken.getText()));
                    } catch (NumberFormatException ex) {
                        editorToolsController.addToLog("Value for 'Token' ist not a number!");
                    }
                }
                if (!propertyTokenMin.getText().isEmpty()) {
                    try {
                        token.setValueMin(Double.parseDouble(propertyTokenMin.getText()));
                    } catch (NumberFormatException ex) {
                        editorToolsController.addToLog("Value for 'Token (min.)' ist not a number!");
                    }
                }
                if (!propertyTokenMax.getText().isEmpty()) {
                    try {
                        token.setValueMax(Double.parseDouble(propertyTokenMax.getText()));
                    } catch (NumberFormatException ex) {
                        editorToolsController.addToLog("Value for 'Token (max.)' ist not a number!");
                    }
                }
                place.setToken(token);
                
                storeNodeInfo(place);
                
                break;
                
            case TRANSITION:
                
                DataTransition transition = (DataTransition) selectedDataNode;
                
                TransitionTypeChoice transitionTypeChoice = (TransitionTypeChoice) propertySubtypeChoices.getSelectionModel().getSelectedItem();
                DataTransition.Type transitionType = transitionTypeChoice.getType();
                
                if (transition.getTransitionType() != transitionType) {
                    dataService.setTransitionTypeDefault(transitionType);
                    dataService.setTypeFor(transition, transitionType);
                }
                
                Function function = new Function();
                if (!propertyFunction.getText().isEmpty()) {
                    function.setFunction(propertyFunction.getText());
                }
                transition.setFunction(function);
                
                storeNodeInfo(transition);
                
                break;
        }
    }
    
    /**
     * Hides node properties. Clears and hides the details frame.
     */
    public void HideElementProperties() {
        nodePropertiesBox.getChildren().clear();
        detailsBox.setVisible(false);
    }
    
    /**
     * Prints info for the given node within the property info textfields.
     * @param node 
     */
    private void printNodeInfo(IDataNode node) {

        propertyName.setText(node.getName());
        propertyLabel.setText(node.getLabelText());
        propertyDescription.setText(node.getDescription());
    }
    
    /**
     * Stores info from the property textfields within the given node.
     * @param node 
     */
    private void storeNodeInfo(IDataNode node) {
        
        if (!propertyName.getText().isEmpty()) {
            node.setName(propertyName.getText());
        } else {
            node.setName("");
        }
        if (!propertyLabel.getText().isEmpty()) {
            node.setLabelText(propertyLabel.getText());
        } else {
            node.setLabelText("");
        }
        if (!propertyDescription.getText().isEmpty()) {
            node.setDescription(propertyDescription.getText());
        } else {
            node.setDescription("");
        }
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        detailsBox.setVisible(false);
    }

    private class ColourChoice
    {
        private final Colour colour;

        public ColourChoice(Colour colour) {
            this.colour = colour;
        }

        public Colour getColour() {
            return colour;
        }

        @Override
        public String toString() {
            return colour.getId();
        }
    }

    private class ArcTypeChoice
    {
        private final DataArc.Type type;

        public ArcTypeChoice(DataArc.Type type) {
            this.type = type;
        }

        public DataArc.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    private class PlaceTypeChoice
    {
        private final DataPlace.Type type;

        public PlaceTypeChoice(DataPlace.Type type) {
            this.type = type;
        }

        public DataPlace.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    public class TransitionTypeChoice
    {
        private final DataTransition.Type type;

        public TransitionTypeChoice(DataTransition.Type type) {
            this.type = type;
        }

        public DataTransition.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
}
