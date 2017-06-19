/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.element;

import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataClusterArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.exception.InputValidationException;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataCluster;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.ParameterService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.model.Weight;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import edu.unibi.agbi.prettyformulafx.main.DetailedParseCancellationException;
import edu.unibi.agbi.prettyformulafx.main.ImageComponent;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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
    @Autowired private MessengerService messengerService;
    @Autowired private DataService dataService;
    @Autowired private ParameterService parameterService;
    @Autowired private FunctionBuilder functionBuilder;

    // Container
    @FXML private VBox elementFrame;
    @FXML private TitledPane identifierPane;
    @FXML private TitledPane propertiesPane;
    @FXML private TitledPane clusterPane;
    @FXML private VBox propertiesBox;

    // Identifier
    @FXML private TextField inputSubType;
    @FXML private TextField inputId;
    @FXML private TextField inputName;
    @FXML private TextField inputLabel;
    @FXML private TextArea inputDescription;

    // Properties
    @FXML private GridPane propertiesSubtype;
    @FXML private GridPane propertiesColor;
    @FXML private GridPane propertiesArc;
    @FXML private GridPane propertiesPlace;
    @FXML private VBox propertiesTransition;

    @FXML private ChoiceBox choiceSubtype;
    @FXML private ChoiceBox choiceColour;
    @FXML private Button buttonColourCreate;

    @FXML private TextField inputArcWeight;
    @FXML private TextField inputPlaceToken;
    @FXML private TextField inputPlaceTokenMin;
    @FXML private TextField inputPlaceTokenMax;
    @FXML private TextField inputTransitionFunction;
    @FXML private SwingNode swingNodeTransitionFunctionImage;

    @FXML private Menu menuRefPlaces;
    @FXML private Menu menuRefTransitions;
    @FXML private Menu menuParamLocal;
    @FXML private Menu menuParamGlobal;
    @FXML private MenuItem menuItemParamEdit;

    @FXML private Label statusMessage;
    
    @FXML private ListView<ClusterElement> listClusteredElements;

    private IDataElement elementSelected;
    private String inputLatestValid;
    private int inputLatestCaretPosition;

    /**
     * Shows the details for the given graph element. The values are loaded from
     * the related data element.
     *
     * @param element
     */
    public void ShowElementDetails(IGraphElement element) {

        elementSelected = element.getDataElement();

        LoadGuiElements(elementSelected);
        LoadElementInfo(elementSelected);
        LoadElementType(elementSelected);
        LoadElementProperties(elementSelected);
    }

    /**
     * Loads GUI elements specific for the given graph element type.
     *
     * @param element
     */
    private void LoadGuiElements(IDataElement element) {

        elementFrame.getChildren().clear();
        elementFrame.getChildren().add(identifierPane);
        propertiesBox.getChildren().clear();
        propertiesBox.getChildren().add(propertiesSubtype);
        propertiesBox.getChildren().add(propertiesColor);

        switch (element.getElementType()) {
            
            case ARC:
                inputLabel.setDisable(true);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(propertiesArc);
                break;

            case CLUSTER:
                inputLabel.setDisable(false);
                elementFrame.getChildren().add(clusterPane);
                break;

            case CLUSTERARC:
                inputLabel.setDisable(true);
                elementFrame.getChildren().add(clusterPane);
                break;

            case PLACE:
                inputLabel.setDisable(false);
                elementFrame.getChildren().add(propertiesPane);
                propertiesBox.getChildren().add(propertiesPlace);
                break;

            case TRANSITION:
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

        inputSubType.setText(element.getElementType().toString());
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
        } else {
            inputLabel.setText("");
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
        Collection<Colour> colors = dataService.getModel().getColours();

        choiceColour.setItems(choicesColour); // assign upfront, input listeners accesses on text change

        switch (element.getElementType()) {

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
                    ClusterElement ce = new ClusterElement((IGraphNode) n);
                    listClusteredElements.getItems().add(ce);
                });
                break;

            case CLUSTERARC:
                DataClusterArc clusterArc = (DataClusterArc) element;
                listClusteredElements.getItems().clear();
                clusterArc.getStoredArcs().forEach(a -> {
                    ClusterElement ce = new ClusterElement((IGraphArc) a);
                    listClusteredElements.getItems().add(ce);
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
                inputLatestCaretPosition = inputTransitionFunction.getText().length();
                LoadParameterChoices(transition);
                break;
        }

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

        dataService.getModel().getPlaces().stream()
                .filter(place -> place.getId().toLowerCase().contains(filter) || place.getName().toLowerCase().contains(filter))
                .forEach(place -> {

                    final Menu menuPlaceArcsIn = new Menu("Incoming Arcs");
                    if (place.getArcsIn().size() > 0) {

                        place.getArcsIn().forEach(arc -> {

                            String ident = arc.getSource().getId() + arc.getTarget().getId();

                            MenuItem itemArcFlowDer = new MenuItem("Actual | f(t)");
                            itemArcFlowDer.setOnAction(e -> {
                                InsertToFunctionInput(ident + "_now");
                            });

                            MenuItem itemArcFlow = new MenuItem("Total | F(t)");
                            itemArcFlow.setOnAction(e -> {
                                InsertToFunctionInput(ident + "_total");
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

                        place.getArcsOut().forEach(arc -> {

                            String ident = arc.getSource().getId() + arc.getTarget().getId();

                            MenuItem itemArcFlowDer = new MenuItem("Actual | f(t)");
                            itemArcFlowDer.setOnAction(e -> {
                                InsertToFunctionInput(ident + "_now");
                            });

                            MenuItem itemArcFlow = new MenuItem("Total | F(t)");
                            itemArcFlow.setOnAction(e -> {
                                InsertToFunctionInput(ident + "_total");
                            });

                            Menu menuArc = new Menu("Token Flow to " + arc.getTarget().getId() + " (" + arc.getSource().getId() + "->" + arc.getTarget().getId() + ")");
                            menuArc.getItems().add(itemArcFlowDer);
                            menuArc.getItems().add(itemArcFlow);

                            menuPlaceArcsOut.getItems().add(menuArc);
                        });
                    } else {
                        menuPlaceArcsOut.setDisable(true);
                    }

                    MenuItem itemPlaceToken = new MenuItem("Token");
                    itemPlaceToken.setOnAction(e -> {
                        InsertToFunctionInput(place.getId());
                    });

                    Menu menuPlace = new Menu("(" + place.getId() + ") " + place.getName());
                    menuPlace.getItems().add(itemPlaceToken);
                    menuPlace.getItems().add(menuPlaceArcsIn);
                    menuPlace.getItems().add(menuPlaceArcsOut);

                    menuRefPlaces.getItems().add(menuPlace);
                });

        dataService.getModel().getTransitions().stream()
                .filter(transition -> transition.getId().toLowerCase().contains(filter) || transition.getName().toLowerCase().contains(filter))
                .forEach(transition -> {

                    MenuItem itemTransitionSpeed = new MenuItem("Speed | v(t)");
                    itemTransitionSpeed.setOnAction(e -> {
                        InsertToFunctionInput(transition.getId());
                    });

                    MenuItem itemTransitionFire = new MenuItem("Fire | 0 or 1");
                    itemTransitionFire.setDisable(true);

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
                        });
                        menuParamGlobal.getItems().add(item);
                    });
            menuParamGlobal.setDisable(false);
        } else {
            menuParamGlobal.setDisable(true);
        }
    }

    private void StoreElementType(IDataElement element) throws DataServiceException {

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

    private void ParseArcWeight() {

        if (ValidateNumberInput(inputArcWeight)) {

            if (elementSelected instanceof DataArc) {

                DataArc arc = (DataArc) elementSelected;
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

        if (elementSelected instanceof DataPlace) {

            DataPlace place = (DataPlace) elementSelected;
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

        if (elementSelected instanceof DataTransition) {

            DataTransition transition = (DataTransition) elementSelected;

            try {
                ParseInputToImage(inputTransitionFunction.getText().replace(",", "."));
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
                messengerService.addException(ex);
            }
        }
    }

    private void ParseInputToImage(String input) throws Exception {

        final DataTransition transition;
        final BufferedImage image;

        if (elementSelected != null && elementSelected.getElementType() == Element.Type.TRANSITION) {
            transition = (DataTransition) elementSelected;
        } else {
            return;
        }

        parameterService.ValidateFunction(input, transition);
        image = PrettyFormulaParser.parseToImage(input);

        SwingUtilities.invokeLater(() -> {
            if (swingNodeTransitionFunctionImage.getContent() != null) {
                swingNodeTransitionFunctionImage.getContent().removeAll();
            }
            ImageComponent img = new ImageComponent();
            img.setImage(image);
            swingNodeTransitionFunctionImage.setContent(img);
        });

        inputLatestValid = input;
    }

    private void setInputStatus(Control input, boolean isError) {
        if (isError) {
            input.setStyle("-fx-border-color: red");
            statusMessage.setTextFill(Color.RED);
            statusMessage.setText("Invalid!");
        } else {
            input.setStyle("");
            statusMessage.setTextFill(Color.GREEN);
            statusMessage.setText("Valid!");
        }
    }

    private boolean ValidateNumberInput(TextField input) {
        try {
            String value = input.getText().replace(",", ".");
            if (!value.matches(functionBuilder.getNumberRegex())) {
                throw new InputValidationException("'" + value + "' is not a number");
            }
            setInputStatus(input, false);
        } catch (InputValidationException ex) {
            setInputStatus(input, true);
            return false;
        }
        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        choiceSubtype.valueProperty().addListener(cl -> {
            if (elementSelected != null) {
                try {
                    StoreElementType(elementSelected);
                    setInputStatus(choiceSubtype, false);
                } catch (DataServiceException ex) {
                    setInputStatus(choiceSubtype, true);
                    messengerService.addException("Cannot change subtype!", ex);
                }
            }
        });

        inputName.textProperty().addListener(cl -> {
            if (!inputName.isDisabled()) {
                if (inputName.getText() != null) {
                    elementSelected.setName(inputName.getText());
                } else {
                    elementSelected.setName("");
                }
            }
        });
        inputLabel.textProperty().addListener(cl -> {
            if (!inputLabel.isDisabled()) {
                if (inputLabel.getText() != null) {
                    elementSelected.setLabelText(inputLabel.getText());
                } else {
                    elementSelected.setLabelText("");
                }
            }
        });
        inputDescription.textProperty().addListener(cl -> {
            if (!inputDescription.isDisabled()) {
                if (inputDescription.getText() != null) {
                    elementSelected.setDescription(inputDescription.getText());
                } else {
                    elementSelected.setDescription("");
                }
            }
        });
        inputArcWeight.textProperty().addListener(cl -> {
            ParseArcWeight();
        });
        inputPlaceToken.textProperty().addListener(cl -> {
            ParsePlaceToken();
        });
        inputPlaceTokenMin.textProperty().addListener(cl -> {
            ParsePlaceToken();
        });
        inputPlaceTokenMax.textProperty().addListener(cl -> {
            ParsePlaceToken();
        });
        inputTransitionFunction.textProperty().addListener(e -> {
            ParseTransitionFunction();
        });
        inputTransitionFunction.setOnKeyTyped(eh -> {
            try {
                PrettyFormulaParser.parseToImage(inputTransitionFunction.getText().replace(",", "."));
            } catch (DetailedParseCancellationException ex) {
                if (eh != null && eh.getCode() != KeyCode.RIGHT && eh.getCode() != KeyCode.LEFT && eh.getCode() != KeyCode.UNDERSCORE) {
//                    inputTransitionFunction.selectRange(ex.getCharPositionInLine(), ex.getEndCharPositionInLine());
                }
            }
        });
        inputTransitionFunction.setOnMouseClicked(eh -> {
            inputLatestCaretPosition = inputTransitionFunction.getCaretPosition();
        });
        inputTransitionFunction.setOnKeyReleased(eh -> {
            inputLatestCaretPosition = inputTransitionFunction.getCaretPosition();
        });

        menuItemParamEdit.setOnAction(e -> {
            mainController.ShowElementParameters(elementSelected);
        });
        
        listClusteredElements.setOnMouseClicked(eh -> {
            if (!listClusteredElements.getItems().isEmpty()) {
                if (eh.getButton() == MouseButton.PRIMARY && eh.getClickCount() == 2) {
                    ShowElementDetails(listClusteredElements.getSelectionModel().getSelectedItem().getElement());
                }
            }
        });
        
        listClusteredElements.selectionModelProperty().addListener(cl -> {
        });
    }
    
    private class ClusterElement {
        
        private final IGraphElement element;
        
        private ClusterElement(IGraphElement element) {
            this.element = element;
        }
        
        private IGraphElement getElement() {
            return element;
        }
        
        @Override
        public String toString() {
            return element.getDataElement().getName() + " (" + element.getDataElement().getId() + ")";
        }
    }
}
