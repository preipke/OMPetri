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

import edu.unibi.agbi.petrinet.entity.PN_Element;
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
    @FXML private TextField propertySource;
    @FXML private TextField propertyTarget;
    
    private IDataElement activeDataNode;
    
    /**
     * Shows the details for the given entity.
     * @param element 
     */
    public void getDetails(IGraphElement element) {
        
        clear();
        
        activeDataNode = element.getRelatedDataElement();

        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        ObservableList<ColourChoice> choicesColour = FXCollections.observableArrayList();
        
        Collection<Colour> colors = dataService.getColours();
        
        int typeIndex = 0;
        
        switch (activeDataNode.getElementType()) {
                
            case ARC:
                
                DataArc arc = (DataArc) activeDataNode;
                
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
                propertySource.setText(((IDataNode)arc.getSource()).getName());
                propertyTarget.setText(((IDataNode)arc.getTarget()).getName());
                
                nodePropertiesBox.getChildren().add(arcPropertyGrid);
                break;
            
            case PLACE:
                
                DataPlace place = (DataPlace) activeDataNode;
                
                DataPlace.Type placeType;
                for (int i = 0; i < DataPlace.Type.values().length; i++) {
                    placeType = DataPlace.Type.values()[i];
                    if (placeType == place.getPlaceType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(new PlaceTypeChoice(placeType));
                }
                
                propertyName.setText(place.getName());
                propertyLabel.setText(place.getId());
                propertyDescription.setText(place.getDescription());
                
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
                
                DataTransition transition = (DataTransition) activeDataNode;
                
                DataTransition.Type transitionType;
                for (int i = 0; i < DataTransition.Type.values().length; i++) {
                    transitionType = DataTransition.Type.values()[i];
                    if (transitionType == transition.getTransitionType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(new TransitionTypeChoice(transitionType));
                }
                
                propertyName.setText(transition.getName());
                propertyLabel.setText(transition.getId());
                propertyDescription.setText(transition.getDescription());
                
                Function function = transition.getFunction();
                propertyFunction.setText(function.toString());
                
                nodePropertiesBox.getChildren().add(idPropertyBox);
                nodePropertiesBox.getChildren().add(transitionPropertyGrid);
                break;
        }
        
        propertyType.setText(activeDataNode.getElementType().toString());
        propertySubtypeChoices.setItems(choicesSubtype);
        propertySubtypeChoices.getSelectionModel().select(typeIndex);

        propertyColorChoices.setItems(choicesColour);
        propertyColorChoices.getSelectionModel().select(0);
        
        detailsBox.setVisible(true);
    }
    
    /**
     * Updates properties. Stores the values from the textfields within the
     * according entity.
     */
    public void update() {
        
        if (activeDataNode == null) {
            return;
        }
        
        // TODO
        // store values according to the selected colour
        
        PN_Element.Type elementType = activeDataNode.getElementType();
        
        ColourChoice colourChoice;
        Colour colour;
        
        switch (elementType) {
                
            case ARC:
                
                colourChoice = (ColourChoice)propertyColorChoices.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();
                
                DataArc arc = (DataArc) activeDataNode;
                
                ArcTypeChoice arcTypeChoice = (ArcTypeChoice) propertySubtypeChoices.getSelectionModel().getSelectedItem();
                DataArc.Type arcType = arcTypeChoice.getType();
                
                arc.setArcType(arcType);
                dataService.setArcTypeDefault(arcType);
                
                Weight weight = new Weight(colour);
                if (!propertyWeight.getText().isEmpty()) {
                    weight.setValue(propertyWeight.getText());
                }
                arc.setWeight(weight);
                
                break;
            
            case PLACE:
                
                colourChoice = (ColourChoice)propertyColorChoices.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();
                
                DataPlace place = (DataPlace) activeDataNode;
                
                PlaceTypeChoice placeTypeChoice = (PlaceTypeChoice) propertySubtypeChoices.getSelectionModel().getSelectedItem();
                DataPlace.Type placeType = placeTypeChoice.getType();
                
                place.setPlaceType(placeType);
                dataService.setPlaceTypeDefault(placeType);
                
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
                
                if (!propertyName.getText().isEmpty()) {
                    place.setName(propertyName.getText());
                }
                if (!propertyDescription.getText().isEmpty()) {
                    place.setDescription(propertyDescription.getText());
                }
                
                break;
                
            case TRANSITION:
                
                DataTransition transition = (DataTransition) activeDataNode;
                
                TransitionTypeChoice transitionTypeChoice = (TransitionTypeChoice) propertySubtypeChoices.getSelectionModel().getSelectedItem();
                DataTransition.Type transitionType = transitionTypeChoice.getType();
                
                transition.setTransitionType(transitionType);
                dataService.setTransitionTypeDefault(transitionType);
                
                Function function = new Function();
                if (!propertyFunction.getText().isEmpty()) {
                    function.setFunction(propertyFunction.getText());
                }
                transition.setFunction(function);
                
                if (!propertyName.getText().isEmpty()) {
                    transition.setName(propertyName.getText());
                }
                if (!propertyDescription.getText().isEmpty()) {
                    transition.setDescription(propertyDescription.getText());
                }
                
                break;
        }
    }
    
    /**
     * Reset texfields. Discards changes and shows the current values.
     */
    public void reset() {
        
    }
    
    /**
     * Clear details. Hides the node specific grids.
     */
    public void clear() {
        nodePropertiesBox.getChildren().clear();
    }
    
    /**
     * Hide details. Completely hides the details box.
     */
    public void hide() {
        clear();
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

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        detailsBox.setVisible(false);
    }
}
