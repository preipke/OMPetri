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
import edu.unibi.agbi.gnius.core.exception.ParameterServiceException;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Colour;
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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class ElementController implements Initializable
{
    @Autowired private MainController mainController;
    @Autowired private ParameterService parameterService;
    @Autowired private DataGraphService dataService;
    @Autowired private MessengerService messengerService;

    // Top Container
    @FXML private TitledPane identifierPane;
    @FXML private TitledPane propertiesPane;
    @FXML private VBox propertiesBox;

    // Identifier
    @FXML private TextField inputType;
    @FXML private TextField inputId;
    @FXML private TextField inputName;
    @FXML private TextField inputLabel;
    @FXML private TextArea inputDescription;

    // Property classes
    @FXML private GridPane propertiesSubtype;
    @FXML private GridPane propertiesColor;
    @FXML private GridPane propertiesArc;
    @FXML private GridPane propertiesPlace;
    @FXML private VBox propertiesTransition;

    // Subtype & Color
    @FXML private ChoiceBox choiceSubtype;
    @FXML private ChoiceBox choiceColour;
    @FXML private Button buttonColourCreate;

    // Arc
    @FXML private TextField inputArcWeight;

    // Place
    @FXML private TextField inputPlaceToken;
    @FXML private TextField inputPlaceTokenMin;
    @FXML private TextField inputPlaceTokenMax;

    // Transition
    @FXML private TextField inputTransitionFunction;
    @FXML private SwingNode swingNodeTransitionFunctionImage;

    @FXML private Menu menuRefPlaces;
    @FXML private Menu menuRefTransitions;
    @FXML private Menu menuParamLocal;
    @FXML private Menu menuParamGlobal;
    @FXML private MenuItem menuItemParamEdit;

    @FXML private Label statusMessage;

    @Value("${param.name.reference.fire}") private String referenceFireName;
    @Value("${param.name.reference.speed}") private String referenceSpeedName;
    @Value("${param.name.reference.token}") private String referenceTokenName;
    @Value("${param.name.reference.tokenflow}") private String referenceTokenflowName;

    @Value("${regex.function.number}") private String regexNumber;

    private IDataElement elementSelected;
    private String inputLatestValid;
    private int inputLatestCaretPosition;

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

        elementSelected = element.getDataElement();

        if (elementSelected.getElementType() == Element.Type.CLUSTERARC) {
            elementSelected = ((DataClusterArc) elementSelected).getDataCluster();
        }

        LoadGuiElements(elementSelected);
        LoadElementInfo(elementSelected);
        LoadElementType(elementSelected);
        LoadElementProperties(elementSelected);
    }

    public void StoreElementDetails() throws DataGraphServiceException {

        if (elementSelected == null) {
            return;
        }

        StoreElementInfo(elementSelected);
        StoreElementProperties(elementSelected);
        
        elementSelected = null;
    }

    /**
     * Loads GUI elements specific for the given graph element type.
     *
     * @param element
     */
    private void LoadGuiElements(IDataElement element) {

        identifierPane.setVisible(true);
        inputLabel.setDisable(false);
        
        switch (element.getElementType()) {

            case CLUSTER:
                System.out.println("TODO LoadGuiElements CLUSTER");
                propertiesPane.setVisible(false);
                break;

            default:
                propertiesPane.setVisible(true);
                choiceSubtype.setStyle("");
                propertiesBox.getChildren().clear();
                propertiesBox.getChildren().add(propertiesSubtype);
                propertiesBox.getChildren().add(propertiesColor);
        }

        switch (element.getElementType()) {

            case ARC:
                inputLabel.setDisable(true);
                inputLabel.setText("");
                inputArcWeight.setStyle("");
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
     * Loads the element type information from the given data element. Fills the
     * GUI with type information and according subtype choices.
     *
     * @param element
     */
    private void LoadElementType(IDataElement element) {

        inputType.setText(element.getElementType().toString());
        choiceSubtype.getItems().clear();

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
        inputId.setText(element.getId());
        inputName.setText(element.getName());
        if (!inputLabel.isDisabled()) {
            inputLabel.setText(element.getLabelText());
        }
        inputDescription.setText(element.getDescription());
    }

    /**
     * Populates the GUI with the properties of the given element.
     *
     * @param element
     */
    private void LoadElementProperties(IDataElement element) {

        ObservableList<Colour> choicesColour = FXCollections.observableArrayList();
        Collection<Colour> colors = dataService.getActiveModel().getColours();

        switch (element.getElementType()) {

            case ARC:

                DataArc arc = (DataArc) element;

                Map<Colour, Weight> weights = arc.getWeightMap();
                Weight weight;

                for (Colour color : colors) {
                    if (weights.get(color) != null) {
                        choicesColour.add(color);
                    }
                }
                weight = weights.get(choicesColour.get(0));
                inputArcWeight.setText(weight.getValue());
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
                        choicesColour.add(color);
                    }
                }
                token = tokens.get(choicesColour.get(0));
                inputPlaceToken.setText(Double.toString(token.getValueStart()));
                inputPlaceTokenMin.setText(Double.toString(token.getValueMin()));
                inputPlaceTokenMax.setText(Double.toString(token.getValueMax()));
                break;

            case TRANSITION:

                DataTransition transition = (DataTransition) element;

                inputTransitionFunction.setText(transition.getFunction().toString());
                inputLatestCaretPosition = inputTransitionFunction.getText().length();
                LoadParameterChoices(transition);
                ParseFunctionInputToImage(null, true);
                break;
        }

        choiceColour.setItems(choicesColour);
        choiceColour.getSelectionModel().select(0);
    }

    /**
     * Creates MenuItem choices for inserting parameter from the parameters
     * available for the given element.
     */
    private void LoadParameterChoices(IDataElement element) {

        final String filter = "".toLowerCase();

        menuRefPlaces.getItems().clear();
        menuRefTransitions.getItems().clear();
        menuParamLocal.getItems().clear();
        menuParamGlobal.getItems().clear();

        dataService.getActiveModel().getPlaces().stream()
                .filter(place -> place.getId().toLowerCase().contains(filter) || place.getName().toLowerCase().contains(filter))
                .forEach(place -> {

                    final Menu menuPlaceArcsIn = new Menu("Incoming Arcs");
                    if (place.getArcsIn().size() > 0) {

                        IntegerProperty arcIndex = new SimpleIntegerProperty(0);
                        place.getArcsIn().forEach(arc -> {

                            arcIndex.set(arcIndex.get() + 1);
                            String ident = referenceTokenflowName + arc.getTarget().getId() + arc.getSource().getId();
                            String value = "'" + arc.getTarget().getId() + "'.tokenFlow.inflow[" + arcIndex.get() + "]";

                            MenuItem itemArcFlow = new MenuItem("Actual | f(t)");
                            itemArcFlow.setOnAction(e -> {
                                CreateReferencingParameter(ident + "_now", "der(" + value + ")", arc);
                            });

                            MenuItem itemArcFlowDer = new MenuItem("Total | F(t)");
                            itemArcFlowDer.setOnAction(e -> {
                                CreateReferencingParameter(ident + "_total", value, arc);
                            });

                            Menu menuArc = new Menu("Token Flow from " + arc.getSource().getId() + " (" + arc.getSource().getId() + "->" + arc.getTarget().getId() + ")");
                            menuArc.getItems().add(itemArcFlowDer);
                            menuArc.getItems().add(itemArcFlow);

                            menuPlaceArcsIn.getItems().add(menuArc);
                        });
                    } else {
                        menuPlaceArcsIn.setDisable(true);
                    }

                    final Menu menuPlaceArcsOut = new Menu("Outgoing Arcs");
                    if (place.getArcsOut().size() > 0) {

                        IntegerProperty arcIndex = new SimpleIntegerProperty(0);
                        place.getArcsOut().forEach(arc -> {

                            arcIndex.set(arcIndex.get() + 1);
                            String ident = referenceTokenflowName + arc.getSource().getId() + arc.getTarget().getId();
                            String value = "'" + arc.getSource().getId() + "'.tokenFlow.outflow[" + arcIndex.get() + "]";

                            MenuItem itemArcFlow = new MenuItem("Actual | f(t)");
                            itemArcFlow.setOnAction(e -> {
                                CreateReferencingParameter(ident + "_now", "der(" + value + ")", arc);
                            });

                            MenuItem itemArcFlowDer = new MenuItem("Total | F(t)");
                            itemArcFlowDer.setOnAction(e -> {
                                CreateReferencingParameter(ident + "_total", value, arc);
                            });

                            Menu menuArc = new Menu("Token Flow to " + arc.getTarget().getId() + " (" + arc.getTarget().getId() + "->" + arc.getSource().getId() + ")");
                            menuArc.getItems().add(itemArcFlowDer);
                            menuArc.getItems().add(itemArcFlow);

                            menuPlaceArcsOut.getItems().add(menuArc);
                        });
                    } else {
                        menuPlaceArcsOut.setDisable(true);
                    }

                    MenuItem itemPlaceToken = new MenuItem("Token");
                    itemPlaceToken.setOnAction(e -> {
                        CreateReferencingParameter(referenceTokenName + place.getId(), "'" + place.getId() + "'.t", place);
                    });

                    Menu menuPlace = new Menu("(" + place.getId() + ") " + place.getName());
                    menuPlace.getItems().add(itemPlaceToken);
                    menuPlace.getItems().add(menuPlaceArcsIn);
                    menuPlace.getItems().add(menuPlaceArcsOut);

                    menuRefPlaces.getItems().add(menuPlace);
                });

        dataService.getActiveModel().getTransitions().stream()
                .filter(transition -> transition.getId().toLowerCase().contains(filter) || transition.getName().toLowerCase().contains(filter))
                .forEach(transition -> {

                    MenuItem itemTransitionSpeed = new MenuItem("Speed | v(t)");
                    itemTransitionSpeed.setOnAction(e -> {
                        CreateReferencingParameter(referenceSpeedName + transition.getId(), "'" + transition.getId() + "'.actualSpeed", transition);
                    });

                    MenuItem itemTransitionFire = new MenuItem("Fire | 0 or 1");
                    itemTransitionFire.setOnAction(e -> {
                        CreateReferencingParameter(referenceFireName + transition.getId(), "'" + transition.getId() + "'.fire", transition);
                    });
                    itemTransitionFire.setDisable(true); // not supported by PNlib yet

                    Menu menuTransition = new Menu("(" + transition.getId() + ") " + transition.getName());
                    menuTransition.getItems().add(itemTransitionFire);
                    menuTransition.getItems().add(itemTransitionSpeed);

                    menuRefTransitions.getItems().add(menuTransition);
                });

        List<Parameter> paramsLocal = parameterService.getLocalParameters(element);
        if (!paramsLocal.isEmpty()) {
            paramsLocal.stream()
                    .filter(param -> param.getId().toLowerCase().contains(filter))
                    .forEach(param -> {
                        MenuItem item = new MenuItem(param.getId() + " = " + param.getValue());
                        item.setOnAction(e -> {
                            InsertToFunctionInput(param.getId());
                            ParseFunctionInputToImage(null, true);
                        });
                        menuParamLocal.getItems().add(item);
                    });
            menuParamLocal.setDisable(false);
        } else {
            menuParamLocal.setDisable(true);
        }

        List<Parameter> paramsGlobal = parameterService.getGlobalParameters();
        if (!paramsGlobal.isEmpty()) {
            paramsGlobal.stream()
                    .filter(param -> param.getId().toLowerCase().contains(filter))
                    .forEach(param -> {
                        MenuItem item = new MenuItem(param.getId() + " = " + param.getValue());
                        item.setOnAction(e -> {
                            InsertToFunctionInput(param.getId());
                            ParseFunctionInputToImage(null, true);
                        });
                        menuParamGlobal.getItems().add(item);
                    });
            menuParamGlobal.setDisable(false);
        } else {
            menuParamGlobal.setDisable(true);
        }
    }
    
    private void StoreElementType(IDataElement element) throws DataGraphServiceException {
        
        Object itemSelected = choiceSubtype.getSelectionModel().getSelectedItem();
        if (itemSelected == null) {
            return;
        }

        switch (element.getElementType()) {

            case ARC:

                DataArc arc = (DataArc) element;
                DataArc.Type arcType = (DataArc.Type) itemSelected;
                if (arc.getArcType() != arcType) {
                    dataService.changeArcType(arc, arcType);
                }
                break;

            case PLACE:

                DataPlace place = (DataPlace) element;
                DataPlace.Type placeType = (DataPlace.Type) itemSelected;
                if (place.getPlaceType() != placeType) {
                    dataService.setPlaceTypeDefault(placeType);
                    dataService.changePlaceType(place, placeType);
                }
                break;

            case TRANSITION:

                DataTransition transition = (DataTransition) element;
                DataTransition.Type transitionType = (DataTransition.Type) itemSelected;
                if (transition.getTransitionType() != transitionType) {
                    dataService.setTransitionTypeDefault(transitionType);
                    dataService.changeTransitionType(transition, transitionType);
                }
                break;
        }
    }

    /**
     * Stores node properties. Stores the values from the textfields within the
     * according entity, overwrites old values.
     *
     * @throws DataGraphServiceException
     */
    private void StoreElementProperties(IDataElement element) throws DataGraphServiceException {

        // TODO store values according to the selected colour
        Colour colour = (Colour) choiceColour.getSelectionModel().getSelectedItem();

        switch (element.getElementType()) {

            case ARC:

                DataArc arc = (DataArc) element;

                try {
                    Weight weight = new Weight(colour);
                    if (!inputArcWeight.getText().isEmpty()) {
                        weight.setValue(String.valueOf(Double.parseDouble(inputArcWeight.getText().replace(",", "."))));
                    }
                    arc.setWeight(weight);
                } catch (NumberFormatException ex) {
                    messengerService.addToLog("Given weight is not a number!");
                }

                break;

            case CLUSTER:
                System.out.println("TODO StoreElementProperties CLUSTER");
                break;

            case PLACE:

                DataPlace place = (DataPlace) element;

                Token token = new Token(colour);
                if (!inputPlaceToken.getText().isEmpty()) {
                    try {
                        token.setValueStart(Double.parseDouble(inputPlaceToken.getText().replace(",", ".")));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token' is not a number!");
                    }
                }
                if (!inputPlaceTokenMin.getText().isEmpty()) {
                    try {
                        token.setValueMin(Double.parseDouble(inputPlaceTokenMin.getText().replace(",", ".")));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token (min.)' is not a number!");
                    }
                }
                if (!inputPlaceTokenMax.getText().isEmpty()) {
                    try {
                        token.setValueMax(Double.parseDouble(inputPlaceTokenMax.getText().replace(",", ".")));
                    } catch (NumberFormatException ex) {
                        messengerService.addToLog("Value for 'Token (max.)' is not a number!");
                    }
                }
                place.setToken(token);

                break;

            case TRANSITION:

                DataTransition transition = (DataTransition) element;

                try {
                    if (inputTransitionFunction.getText().isEmpty()) {
                        parameterService.setTransitionFunction(transition, "1");
                    } else {
                        ParseFunctionInputToImage(null, false);
                        parameterService.setTransitionFunction(transition, inputLatestValid);
                    }
                } catch (Exception ex) {
                    messengerService.addToLog(ex.getMessage());
                }

                break;
        }
    }

    /**
     * Stores info from the property textfields to the given element.
     *
     * @param element
     */
    private void StoreElementInfo(IDataElement element) {
        if (!inputName.isDisabled()) {
            if (inputName.getText() != null) {
                element.setName(inputName.getText());
            } else {
                element.setName("");
            }
        }
        if (!inputLabel.isDisabled()) {
            if (inputLabel.getText() != null) {
                element.setLabelText(inputLabel.getText());
            } else {
                element.setLabelText("");
            }
        }
        if (!inputDescription.isDisabled()) {
            if (inputDescription.getText() != null) {
                element.setDescription(inputDescription.getText());
            } else {
                element.setDescription("");
            }
        }
    }

    /**
     * Creates a parameter that references an element.
     *
     * @param id
     * @param value
     * @param element
     */
    private void CreateReferencingParameter(String id, String value, IElement element) {
        Parameter param = new Parameter(id, "", value, Parameter.Type.REFERENCE);
        try {
            parameterService.addParameter(param, element);
            InsertToFunctionInput(id);
            ParseFunctionInputToImage(null, true);
        } catch (ParameterServiceException ex) {
            setStatus(null, "Cannot insert reference!", ex);
        }
    }

    /**
     * Inserts the given parameter to the function input. Inserts the function
     * at the latest caret position, moves caret by values length.
     *
     * @param param
     */
    private void InsertToFunctionInput(String value) {
        String function;
        function = inputTransitionFunction.getText().substring(0, inputLatestCaretPosition);
        function += value;
        function += inputTransitionFunction.getText().substring(inputLatestCaretPosition);
        inputTransitionFunction.setText(function);
        inputLatestCaretPosition = inputLatestCaretPosition + value.length();
    }

    private void ParseFunctionInputToImage(KeyEvent event, boolean generateImg) {

        final DataTransition transition;
        final BufferedImage image;
        final String input = inputTransitionFunction.getText().replace(",", ".");

        if (elementSelected == null || elementSelected.getElementType() != Element.Type.TRANSITION) {
            return;
        } else {
            transition = (DataTransition) elementSelected;
        }

        try {
            parameterService.ValidateFunction(input, transition);
            image = PrettyFormulaParser.parseToImage(input);
            if (generateImg) {
                SwingUtilities.invokeLater(() -> {
                    if (swingNodeTransitionFunctionImage.getContent() != null) {
                        swingNodeTransitionFunctionImage.getContent().removeAll();
                    }
                    ImageComponent img = new ImageComponent();
                    img.setImage(image);
                    swingNodeTransitionFunctionImage.setContent(img);
                });
            }
            inputLatestValid = input;
            setStatus(inputTransitionFunction, "Valid!", null);
        } catch (DetailedParseCancellationException ex) {
            if (event != null && event.getCode() != KeyCode.RIGHT && event.getCode() != KeyCode.LEFT) {
                inputTransitionFunction.selectRange(ex.getCharPositionInLine(), ex.getEndCharPositionInLine());
            }
            setStatus(inputTransitionFunction, "Invalid input!", ex);
        } catch (Exception ex) {
            setStatus(inputTransitionFunction, "Invalid input!", ex);
        }
    }

    private void ValidateNumberInput(TextField input) {
        try {
            String value = input.getText().replace(",", ".");
            if (!value.matches(regexNumber)) {
                throw new InputValidationException("'" + value + "' is not a number");
            }
            setStatus(input, "", null);
        } catch (InputValidationException ex) {
            setStatus(input, "Invalid input!", ex);
        }
    }

    private void setStatus(Control input, String msg, Throwable thr) {
        if (thr != null) {
            if (input != null) {
                input.setStyle("-fx-border-color: red");
            }
            statusMessage.setTextFill(Color.RED);
            messengerService.addToLog(msg + " [" + thr.getMessage() + "]");
        } else {
            if (input != null) {
                input.setStyle("");
            }
            statusMessage.setTextFill(Color.GREEN);
        }
        statusMessage.setText(msg);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        identifierPane.setVisible(false);
        propertiesPane.setVisible(false);
        
        choiceSubtype.valueProperty().addListener(cl -> {
            if (elementSelected != null) {
                try {
                    StoreElementType(elementSelected);
                    setStatus(choiceSubtype, "", null);
                } catch (DataGraphServiceException ex) {
                    setStatus(choiceSubtype, ex.getMessage(), ex);
                }
            }
        });

        inputArcWeight.textProperty().addListener(cl -> ValidateNumberInput(inputArcWeight));
        inputPlaceToken.textProperty().addListener(cl -> ValidateNumberInput(inputPlaceToken));
        inputPlaceTokenMin.textProperty().addListener(cl -> ValidateNumberInput(inputPlaceTokenMin));
        inputPlaceTokenMax.textProperty().addListener(cl -> ValidateNumberInput(inputPlaceTokenMax));

        inputTransitionFunction.textProperty().addListener(e -> ParseFunctionInputToImage(null, true));
        inputTransitionFunction.setOnMouseClicked(e -> {
            inputLatestCaretPosition = inputTransitionFunction.getCaretPosition();
        });
        inputTransitionFunction.setOnKeyReleased(e -> {
            inputLatestCaretPosition = inputTransitionFunction.getCaretPosition();
        });

        menuItemParamEdit.setOnAction(e -> {
            IDataElement elem = elementSelected;
            try {
                StoreElementDetails();
            } catch (DataGraphServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            }
            mainController.ShowParameters(elem);
        });
    }
}
