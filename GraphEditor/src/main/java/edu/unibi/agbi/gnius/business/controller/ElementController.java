/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.exception.InputValidationException;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.prettyformulafx.main.DetailedParseCancellationException;
import edu.unibi.agbi.prettyformulafx.main.ImageComponent;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collection;
import java.util.List;
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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ElementController implements Initializable
{
    @Autowired private MainController mainController;
    @Autowired private ParameterController parameterController;
    @Autowired private DataGraphService dataService;
    @Autowired private MessengerService messengerService;

    // Top Container
    @FXML private TitledPane identifierPane;
    @FXML private TitledPane propertiesPane;
    @FXML private VBox propertiesBox;

    // Identifier
    @FXML private TextField elementType;
    @FXML private TextField elementId;
    @FXML private TextField elementName;
    @FXML private TextField elementLabel;
    @FXML private TextArea elementDescription;

    // Property classes
    @FXML private GridPane propertiesSubtype;
    @FXML private GridPane propertiesColor;
    @FXML private GridPane propertiesArc;
    @FXML private GridPane propertiesPlace;
    @FXML private VBox propertiesTransition;

    // Subtype & Color
    @FXML private ChoiceBox elementSubtype;
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
    @FXML private Button buttonParameterShow;
    @FXML private MenuButton buttonParameterInsert;

    private IDataElement selectedElement;
    private String latestValidInput;
    private int latestCaretPosition;

    /**
     * Hides the element details container.
     */
    public void HideElementDetails() {
        identifierPane.setVisible(false);
        propertiesPane.setVisible(false);
    }

    /**
     * Shows the details for the given graph element. The values are loaded from
     * the related data element.
     *
     * @param element
     */
    public void ShowElementDetails(IGraphElement element) {

        selectedElement = element.getDataElement();

        if (selectedElement.getElementType() == Element.Type.CLUSTERARC) {
            selectedElement = ((DataClusterArc) selectedElement).getDataCluster();
        }

        LoadGuiElements(selectedElement);
        LoadElementType(selectedElement);
        LoadElementIdentifier(selectedElement);
        LoadElementProperties(selectedElement);
    }

    /**
     * Stores node properties. Stores the values from the textfields within the
     * according entity, overwrites old values.
     *
     * @throws DataGraphServiceException
     */
    public void StoreElementProperties() throws DataGraphServiceException {

        if (selectedElement == null) {
            return;
        }

        // TODO store values according to the selected colour
        ColourChoice colourChoice;
        Colour colour;

        switch (selectedElement.getElementType()) {

            case ARC:

                colourChoice = (ColourChoice) colorChoice.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();

                DataArc arc = (DataArc) selectedElement;

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
                System.out.println("TODO StoreElementProperties CLUSTER");
                break;

            case PLACE:

                colourChoice = (ColourChoice) colorChoice.getSelectionModel().getSelectedItem();
                colour = colourChoice.getColour();

                DataPlace place = (DataPlace) selectedElement;

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
                        messengerService.addToLog("Value for 'Token' is not a number!");
                    }
                }
                if (!propertyTokenMin.getText().isEmpty()) {
                    try {
                        token.setValueMin(Double.parseDouble(propertyTokenMin.getText()));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token (min.)' is not a number!");
                    }
                }
                if (!propertyTokenMax.getText().isEmpty()) {
                    try {
                        token.setValueMax(Double.parseDouble(propertyTokenMax.getText()));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token (max.)' is not a number!");
                    }
                }
                place.setToken(token);

                break;

            case TRANSITION:

                DataTransition transition = (DataTransition) selectedElement;

                TransitionTypeChoice transitionTypeChoice = (TransitionTypeChoice) elementSubtype.getSelectionModel().getSelectedItem();
                DataTransition.Type transitionType = transitionTypeChoice.getType();

                if (transition.getTransitionType() != transitionType) {
                    dataService.setTransitionTypeDefault(transitionType);
                    dataService.changeTransitionType(transition, transitionType);
                }

                ParseFunctionInput(null);
                if (!transitionFunctionInput.getText().isEmpty()) {
                    transition.getFunction().set(latestValidInput);
                } else {
                    transition.getFunction().set("1");
                }

                break;
        }

        storeNodeInfo(selectedElement);
    }

    /**
     * Loads GUI elements specific for the given graph element type.
     *
     * @param element
     */
    private void LoadGuiElements(IDataElement element) {

        identifierPane.setVisible(true);
        elementLabel.setDisable(false);

        switch (element.getElementType()) {

            case CLUSTER:
                System.out.println("TODO LoadGuiElements CLUSTER");
                break;

            default:
                propertiesPane.setVisible(true);
                propertiesBox.getChildren().clear();
                propertiesBox.getChildren().add(propertiesSubtype);
                propertiesBox.getChildren().add(propertiesColor);
        }

        switch (element.getElementType()) {

            case ARC:
                elementLabel.setDisable(true);
                elementLabel.setText("");
                propertiesBox.getChildren().add(propertiesArc);
                break;

            case PLACE:
                propertiesBox.getChildren().add(propertiesPlace);
                break;

            case TRANSITION:
                propertiesBox.getChildren().add(propertiesTransition);
                break;
        }
    }

    /**
     * Populates the GUI with the properties of the given element.
     *
     * @param element
     */
    private void LoadElementProperties(IDataElement element) {

        ObservableList<ColourChoice> choicesColour = FXCollections.observableArrayList();
        Collection<Colour> colors = dataService.getDataDao().getColours();

        switch (element.getElementType()) {

            case ARC:

                DataArc arc = (DataArc) element;

                Map<Colour, Weight> weights = arc.getWeightMap();
                Weight weight;

                for (Colour color : colors) {
                    if (weights.get(color) != null) {
                        choicesColour.add(new ColourChoice(color));
                    }
                }
                weight = weights.get(choicesColour.get(0).getColour());
                propertyWeight.setText(weight.getValue());
                break;

            case CLUSTER:
                System.out.println("TODO LoadElementDetails CLUSTER");
                break;

            case PLACE:

                DataPlace place = (DataPlace) element;

                Map<Colour, Token> tokens = place.getTokenMap();
                Token token = null;
                for (Colour color : colors) {
                    if (tokens.get(color) != null) {
                        choicesColour.add(new ColourChoice(color));
                    }
                }
                token = tokens.get(choicesColour.get(0).getColour());
                propertyToken.setText(Double.toString(token.getValueStart()));
                propertyTokenMin.setText(Double.toString(token.getValueMin()));
                propertyTokenMax.setText(Double.toString(token.getValueMax()));
                break;

            case TRANSITION:

                DataTransition transition = (DataTransition) element;

                transitionFunctionInput.setText(transition.getFunction().toString());
                latestCaretPosition = transitionFunctionInput.getText().length();
                LoadParameterChoices(transition);
                ParseFunctionInput(null);
                break;
        }

        colorChoice.setItems(choicesColour);
        colorChoice.getSelectionModel().select(0);
    }

    /**
     * Creates MenuItem choices for inserting parameter from the parameters
     * available for the given element.
     */
    private void LoadParameterChoices(IDataElement element) {
        List<Parameter> params = parameterController.getParameter(element);
        MenuItem item;
        buttonParameterInsert.getItems().clear();
        for (final Parameter param : params) {
            item = new MenuItem(param.getId() + " = " + param.getValue());
            item.setOnAction(e -> {
                InsertParamToFunctionInput(param);
                ParseFunctionInput(null);
            });
            buttonParameterInsert.getItems().add(item);
        }
    }

    /**
     * Loads the element type information from the given data element. Fills the
     * GUI with type information and according subtype choices.
     *
     * @param element
     */
    private void LoadElementType(IDataElement element) {

        elementType.setText(element.getElementType().toString());
        elementSubtype.getItems().clear();

        ObservableList<Object> choicesSubtype = FXCollections.observableArrayList();
        int typeIndex = -1;

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
        if (typeIndex > -1) {
            elementSubtype.setItems(choicesSubtype);
            elementSubtype.getSelectionModel().select(typeIndex);
        }
    }

    /**
     * Prints info for the given element to the property textfields.
     *
     * @param element
     */
    private void LoadElementIdentifier(IDataElement element) {
        elementId.setText(element.getId());
        elementName.setText(element.getName());
        if (!elementLabel.isDisabled()) {
            elementLabel.setText(element.getLabelText());
        }
        elementDescription.setText(element.getDescription());
    }

    /**
     * Stores info from the property textfields to the given element.
     *
     * @param element
     */
    private void storeNodeInfo(IDataElement element) {
        if (!elementName.isDisabled()) {
            if (elementName.getText() != null) {
                element.setName(elementName.getText());
            } else {
                element.setName("");
            }
        }
        if (!elementLabel.isDisabled()) {
            if (elementLabel.getText() != null) {
                element.setLabelText(elementLabel.getText());
            } else {
                element.setLabelText("");
            }
        }
        if (!elementDescription.isDisabled()) {
            if (elementDescription.getText() != null) {
                element.setDescription(elementDescription.getText());
            } else {
                element.setDescription("");
            }
        }
    }

    /**
     * Inserts the given parameter to the function input. Inserts the function
     * at the latest caret position.
     *
     * @param param
     */
    private void InsertParamToFunctionInput(Parameter param) {
        System.out.println("Caret position: " + latestCaretPosition);
        String function;
        function = transitionFunctionInput.getText().substring(0, latestCaretPosition);
        function += param.getId();
        function += transitionFunctionInput.getText().substring(latestCaretPosition);
        transitionFunctionInput.setText(function);
    }

    private void ParseFunctionInput(KeyEvent event) {

        final DataTransition transition;
        final BufferedImage image;

        if (selectedElement.getElementType() != Element.Type.TRANSITION) {
            return;
        } else {
            transition = (DataTransition) selectedElement;
        }

        try {
            parameterController.ValidateFunctionInput(transition, transitionFunctionInput.getText());
            image = PrettyFormulaParser.parseToImage(transitionFunctionInput.getText());
            SwingUtilities.invokeLater(() -> {
                if (transitionFunctionImage.getContent() != null) {
                    transitionFunctionImage.getContent().removeAll();
                }
                ImageComponent img = new ImageComponent();
                img.setImage(image);
                transitionFunctionImage.setContent(img);
            });
            latestValidInput = transitionFunctionInput.getText();
            transitionFunctionStatus.setTextFill(Color.GREEN);
            transitionFunctionStatus.setText("Valid!");
        } catch (DetailedParseCancellationException ex) {
            if (event != null && event.getCode() != KeyCode.RIGHT && event.getCode() != KeyCode.LEFT) {
                transitionFunctionInput.selectRange(ex.getCharPositionInLine(), ex.getEndCharPositionInLine());
            }
            transitionFunctionStatus.setTextFill(Color.RED);
            transitionFunctionStatus.setText("Invalid input! [" + ex.getMessage() + "]");
        } catch (Exception ex) {
            transitionFunctionStatus.setTextFill(Color.RED);
            transitionFunctionStatus.setText("Invalid input! [" + ex.getMessage() + "]");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        identifierPane.setVisible(false);
        propertiesPane.setVisible(false);
        transitionFunctionInput.setOnMouseClicked(e -> {
            latestCaretPosition = transitionFunctionInput.getCaretPosition();
        });
        transitionFunctionInput.setOnKeyReleased(e -> {
            latestCaretPosition = transitionFunctionInput.getCaretPosition();
            ParseFunctionInput(e);
        });
        buttonParameterShow.setOnAction(e -> {
            try {
                StoreElementProperties();
            } catch (DataGraphServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            }
            mainController.ShowParameter(selectedElement);
        });
    }

    private class ColourChoice
    {
        private final Colour colour;

        private ColourChoice(Colour colour) {
            this.colour = colour;
        }

        private Colour getColour() {
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

        private ArcTypeChoice(DataArc.Type type) {
            this.type = type;
        }

        private DataArc.Type getType() {
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

        private PlaceTypeChoice(DataPlace.Type type) {
            this.type = type;
        }

        private DataPlace.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    private class TransitionTypeChoice
    {
        private final DataTransition.Type type;

        private TransitionTypeChoice(DataTransition.Type type) {
            this.type = type;
        }

        private DataTransition.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
}
