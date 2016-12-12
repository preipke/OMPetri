/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataService;

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
    @Autowired private DataService dataService;
    
    @FXML private VBox detailsBox;
    @FXML private VBox propertiesBox;
    
    @FXML private GridPane arcPropertyGrid;
    @FXML private GridPane colorSelectionGrid;
    @FXML private GridPane placePropertyGrid;
    @FXML private GridPane transitionPropertyGrid;
    @FXML private GridPane typePropertyGrid;
    
    @FXML private ChoiceBox subtypeChoices;
    @FXML private ChoiceBox colorChoices;
    
    @FXML private TextField propertyFunction;
    @FXML private TextField propertyType;
    @FXML private TextField propertyToken;
    @FXML private TextField propertyTokenMin;
    @FXML private TextField propertyTokenMax;
    @FXML private TextField propertyWeight;
    
    @FXML private Button colorButton;
    
    /**
     * Shows the details for the given entity.
     * @param element 
     */
    public void getDetails(IGraphElement element) {
        
        clear();
        
        IDataElement node = element.getRelatedDataElement();
        
        PN_Element.Type elementType = node.getElementType();

        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        ObservableList<ColourChoice> choicesColour = FXCollections.observableArrayList();
        
        Collection<Colour> colors = dataService.getColours();
        
        int selectionIndex = 0;
        
        switch (elementType) {
            
            case PLACE:
                
                DataPlace place = (DataPlace) node;
                
                DataPlace.Type placeType;
                for (int i = 0; i < DataPlace.Type.values().length; i++) {
                    placeType = DataPlace.Type.values()[i];
                    if (placeType == place.getPlaceType()) {
                        selectionIndex = i;
                    }
                    choicesSubtype.add(new PlaceTypeChoice(placeType));
                }
                
                if (colors.size() > 0) {
                    Map<Colour , Token> tokens = place.getTokenMap();
                    for (Colour color : colors) {
                        if (tokens.get(color) != null) {
                            choicesColour.add(new ColourChoice(color));
                        }
                    }
                    Token token = tokens.get(choicesColour.get(0).getColour());
                    propertyToken.setText(Double.toString(token.getValueStart()));
                    propertyTokenMin.setText(Double.toString(token.getValueMin()));
                    propertyTokenMax.setText(Double.toString(token.getValueMax()));
                }
                
                propertiesBox.getChildren().add(placePropertyGrid);
                break;
                
            case TRANSITION:
                
                DataTransition transition = (DataTransition) node;
                
                DataTransition.Type transitionType;
                for (int i = 0; i < DataTransition.Type.values().length; i++) {
                    transitionType = DataTransition.Type.values()[i];
                    if (transitionType == transition.getTransitionType()) {
                        selectionIndex = i;
                    }
                    choicesSubtype.add(new TransitionTypeChoice(transitionType));
                }
                
                Function function = transition.getFunction();
                propertyFunction.setText(function.toString());
                
                propertiesBox.getChildren().add(transitionPropertyGrid);
                break;
                
            case ARC:
                
                DataArc arc = (DataArc) node;
                
                DataArc.Type arcType;
                for (int i = 0; i < DataArc.Type.values().length; i++) {
                    arcType = DataArc.Type.values()[i];
                    if (arcType == arc.getArcType()) {
                        selectionIndex = i;
                    }
                    choicesSubtype.add(new EdgeTypeChoice(arcType));
                }
                
                if (colors.size() > 0) {
                    Map<Colour , Weight> weights = arc.getWeightMap();
                    for (Colour color : colors) {
                        if (weights.get(color) != null) {
                            choicesColour.add(new ColourChoice(color));
                        }
                    }
                    Weight weight = weights.get(choicesColour.get(0).getColour());
                    propertyWeight.setText(Double.toString(weight.getValue()));
                }
                
                propertiesBox.getChildren().add(arcPropertyGrid);
                break;
        }
        
        propertyType.setText(elementType.toString());

        subtypeChoices.setItems(choicesSubtype);
        subtypeChoices.getSelectionModel().select(selectionIndex);
        
        colorChoices.setItems(choicesColour);
        colorChoices.getSelectionModel().select(0);
        
        detailsBox.setVisible(true);
    }
    
    /**
     * Update properties. Stores the values from the textfields within the
     * according entity.
     */
    public void update() {
        
    }
    
    /**
     * Reset texfields. Discards changes and displays the current values.
     */
    public void reset() {
        
    }
    
    /**
     * Clear details. Hides the node specific grids.
     */
    public void clear() {
        propertiesBox.getChildren().remove(arcPropertyGrid);
        propertiesBox.getChildren().remove(placePropertyGrid);
        propertiesBox.getChildren().remove(transitionPropertyGrid);
    }
    
    /**
     * Hide details. Completely hides the details box.
     */
    public void hide() {
        detailsBox.setVisible(false);
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

    private class EdgeTypeChoice
    {
        private final DataArc.Type type;

        public EdgeTypeChoice(DataArc.Type type) {
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
