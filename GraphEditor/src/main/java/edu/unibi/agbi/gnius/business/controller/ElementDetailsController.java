/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.prettyformulafx.main.DetailedParseCancellationException;
import edu.unibi.agbi.prettyformulafx.main.ImageComponent;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.swing.SwingUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class ElementDetailsController implements Initializable
{
    @Autowired private DataGraphService dataService;
    @Autowired @Lazy private MessengerService messengerService;
    
    private IDataElement selectedDataElement;
    
    // Container
    @FXML private VBox elementDetails;
    @FXML private TitledPane clusterPane;
    @FXML private TitledPane typePane;
    @FXML private TitledPane identPane;
    @FXML private TitledPane propertiesPane;
    
    // Cluster
    @FXML private TextField clusterLabel;
    @FXML private TextArea clusterDescription;
    @FXML private ListView clusterElementsList;
    
    // Type
    @FXML private TextField elementType;
    @FXML private ChoiceBox elementSubtype;
    
    // Identifier
    @FXML private TextField elementId;
    @FXML private TextField elementName;
    @FXML private TextField elementLabel;
    @FXML private TextArea elementDescription;
    
    // Properties
    @FXML private VBox propertiesContainer;
    @FXML private GridPane colorSelectionGrid;
    @FXML private GridPane arcPropertiesGrid;
    @FXML private GridPane placePropertiesGrid;
    @FXML private VBox transitionPropertiesBox;
    
    // Color
    @FXML private ChoiceBox colorChoice;
    @FXML private Button colorCreate;
    
    // Arc
    @FXML private TextField propertyWeight;
    
    // Place
    @FXML private TextField propertyToken;
    @FXML private TextField propertyTokenMin;
    @FXML private TextField propertyTokenMax;
    
    // Transition
    @FXML private TextField transitionFunctionInput;
    @FXML private SwingNode transitionFunctionImage;
    @FXML private Label transitionFunctionStatus;
    
    /**
     * Hides the element properties container.
     */
    public void HideElementProperties() {
        elementDetails.setVisible(false);
    }
    
    /**
     * Shows the properties for the given graph element. The values are
     * loaded from the related data element.
     * 
     * @param element 
     */
    public void ShowElementDetails(IGraphElement element) {
        
        selectedDataElement = element.getDataElement();
        
        LoadGuiElements(selectedDataElement);
        LoadElementDetails(selectedDataElement);
    }
    
    /**
     * Stores node properties. Stores the values from the textfields within the
     * according entity, overwrites old values.
     * @throws edu.unibi.agbi.gnius.core.service.exception.DataGraphServiceException
     */
    public void StoreElementProperties() throws DataGraphServiceException {
        
        if (selectedDataElement == null) {
            return;
        }
        
        // TODO
        // store values according to the selected colour
        ColourChoice colourChoice;
        Colour colour;
        
        switch (selectedDataElement.getElementType()) {
                
            case ARC:
                
                colourChoice = (ColourChoice) colorChoice.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();
                
                DataArc arc = (DataArc) selectedDataElement;
                
                ArcTypeChoice arcTypeChoice = (ArcTypeChoice) elementSubtype.getSelectionModel().getSelectedItem();
                DataArc.Type arcType = arcTypeChoice.getType();
                
                if (arc.getArcType() != arcType) {
                    dataService.setArcTypeDefault(arcType);
                    dataService.changeArcType(arc, arcType);
                }
                
                Weight weight = new Weight(colour);
                if (!propertyWeight.getText().isEmpty()) {
                    weight.setValue(propertyWeight.getText());
                }
                arc.setWeight(weight);
                
                break;
                
            case CLUSTER:
                System.out.println("TODO!");
                break;
            
            case PLACE:
                
                colourChoice = (ColourChoice)colorChoice.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();
                
                DataPlace place = (DataPlace) selectedDataElement;
                
                PlaceTypeChoice placeTypeChoice = (PlaceTypeChoice) elementSubtype.getSelectionModel().getSelectedItem();
                DataPlace.Type placeType = placeTypeChoice.getType();
                
                if (place.getPlaceType() != placeType) {
                    dataService.setPlaceTypeDefault(placeType);
                    dataService.changePlaceType(place, placeType);
                }
                
                Token token = new Token(colour);
                if (!propertyToken.getText().isEmpty()) {
                    try {
                        token.setValueStart(Double.parseDouble(propertyToken.getText()));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token' ist not a number!");
                    }
                }
                if (!propertyTokenMin.getText().isEmpty()) {
                    try {
                        token.setValueMin(Double.parseDouble(propertyTokenMin.getText()));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token (min.)' ist not a number!");
                    }
                }
                if (!propertyTokenMax.getText().isEmpty()) {
                    try {
                        token.setValueMax(Double.parseDouble(propertyTokenMax.getText()));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token (max.)' ist not a number!");
                    }
                }
                place.setToken(token);
                
                storeNodeInfo(place);
                
                break;
                
            case TRANSITION:
                
                DataTransition transition = (DataTransition) selectedDataElement;
                
                TransitionTypeChoice transitionTypeChoice = (TransitionTypeChoice) elementSubtype.getSelectionModel().getSelectedItem();
                DataTransition.Type transitionType = transitionTypeChoice.getType();
                
                if (transition.getTransitionType() != transitionType) {
                    dataService.setTransitionTypeDefault(transitionType);
                    dataService.changeTransitionType(transition, transitionType);
                }
                
                Function function = new Function();
                if (!transitionFunctionInput.getText().isEmpty()) {
                    function.setFunction(transitionFunctionInput.getText());
                }
                transition.setFunction(function);
                
                storeNodeInfo(transition);
                
                break;
        }
    }
    
    /**
     * Loads GUI elements specific for the given graph element type.
     * @param element 
     */
    private void LoadGuiElements(IDataElement element) {
        
        elementDetails.getChildren().clear();
        
        if (element instanceof GraphCluster) {
            elementDetails.getChildren().add(clusterPane);
        } else {
            elementDetails.getChildren().add(typePane);
            elementDetails.getChildren().add(identPane);
            elementDetails.getChildren().add(propertiesPane);
        }
        
        propertiesContainer.getChildren().clear();
        
        switch (element.getElementType()) {
                
            case ARC:
                propertiesContainer.getChildren().add(arcPropertiesGrid);
                break;
                
            case CLUSTER:
                System.out.println("TODO!");
                break;
            
            case PLACE:
                propertiesContainer.getChildren().add(placePropertiesGrid);
                break;
                
            case TRANSITION:
                propertiesContainer.getChildren().add(transitionPropertiesBox);
                break;
        }
        
        elementDetails.setVisible(true);
    }
    
    /**
     * Loads element specific properties to the GUI elements.
     * @param element 
     */
    private void LoadElementDetails(IDataElement element) {

        LoadElementType(element);
        loadElementIdentifier(element);
        
        ObservableList<ColourChoice> choicesColour = FXCollections.observableArrayList();
        Collection<Colour> colors = dataService.getColours();
        
        switch (element.getElementType()) {
                
            case ARC:
                
                DataArc arc = (DataArc) element;
                
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
                break;
                
            case CLUSTER:
                System.out.println("TODO");
                break;
            
            case PLACE:
                
                DataPlace place = (DataPlace) element;
                
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
                break;
                
            case TRANSITION:
                
                DataTransition transition = (DataTransition) element;
                
                transitionFunctionInput.setText(transition.getFunction().toString());
                parseFunctionToImage(null);
                break;
        }

        colorChoice.setItems(choicesColour);
        colorChoice.getSelectionModel().select(0);
    }
    
    /**
     * Loads the element type information from the given data element. Fills 
     * the GUI with type information and according subtype choices.
     * @param element 
     */
    private void LoadElementType(IDataElement element) {
        
        elementType.setText(element.getElementType().toString());
        
        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        int typeIndex = 0;
        
        switch (element.getElementType()) {
            
            case ARC:
                DataArc arc = (DataArc) element;
                DataArc.Type arcType;
                for (int i = 0; i < DataArc.Type.values().length; i++) {
                    arcType = DataArc.Type.values()[i];
                    if (arcType == arc.getArcType()) {
                        typeIndex = i;
                    }
                    choicesSubtype.add(new ArcTypeChoice(arcType));
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
                    choicesSubtype.add(new PlaceTypeChoice(placeType));
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
                    choicesSubtype.add(new TransitionTypeChoice(transitionType));
                }
                break;
        }
        elementSubtype.setItems(choicesSubtype);
        elementSubtype.getSelectionModel().select(typeIndex);
    }
    
    /**
     * Prints info for the given node within the property info textfields.
     * @param element 
     */
    private void loadElementIdentifier(IDataElement element) {
        elementId.setText(element.getId());
        elementName.setText(element.getName());
        elementLabel.setText(element.getLabelText());
        elementDescription.setText(element.getDescription());
    }
    
    /**
     * Stores info from the property textfields within the given node.
     * @param node 
     */
    private void storeNodeInfo(IDataNode node) {
        if (!elementName.getText().isEmpty()) {
            node.setName(elementName.getText());
        } else {
            node.setName("");
        }
        if (!elementLabel.getText().isEmpty()) {
            node.setLabelText(elementLabel.getText());
        } else {
            node.setLabelText("");
        }
        if (!elementDescription.getText().isEmpty()) {
            node.setDescription(elementDescription.getText());
        } else {
            node.setDescription("");
        }
    }
    
    /**
     * Parses the input from the function related textfield to an image by using 
     * PrettyFormula.
     * 
     * @param event 
     */
    private void parseFunctionToImage(KeyEvent event) {
        
        transitionFunctionStatus.setText("");
        transitionFunctionInput.deselect();
        try {
            BufferedImage image = PrettyFormulaParser.parseToImage(transitionFunctionInput.getText());
            SwingUtilities.invokeLater(() -> {
                if (transitionFunctionImage.getContent() != null) {
                    transitionFunctionImage.getContent().removeAll();
                }
                ImageComponent img = new ImageComponent();
                img.setImage(image);
                transitionFunctionImage.setContent(img);
            });
            transitionFunctionStatus.setTextFill(Color.GREEN);
            transitionFunctionStatus.setText("Valid!");
        } catch (DetailedParseCancellationException ex) {
            if (event != null && event.getCode() != KeyCode.RIGHT && event.getCode() != KeyCode.LEFT) {
                transitionFunctionInput.selectRange(ex.getCharPositionInLine(), ex.getEndCharPositionInLine()); // check behavior
            }
            transitionFunctionStatus.setTextFill(Color.RED);
            transitionFunctionStatus.setText(ex.getMessage());
        } catch (Exception ex) {
            transitionFunctionStatus.setTextFill(Color.RED);
            transitionFunctionStatus.setText(ex.getMessage());
        }
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        elementDetails.setVisible(false);
        transitionFunctionInput.setOnKeyReleased(event -> parseFunctionToImage(event));
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
