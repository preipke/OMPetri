/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.inspector;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.ParameterService;
import edu.unibi.agbi.editor.core.data.entity.data.DataType;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.ConflictResolutionStrategy;
import edu.unibi.agbi.petrinet.model.Function;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.util.FunctionFactory;
import edu.unibi.agbi.prettyformulafx.main.ImageComponent;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import javafx.animation.PauseTransition;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author PR
 */
@Controller
public class PropertiesController implements Initializable
{
    @Autowired private ModelService modelService;
    @Autowired private FunctionFactory functionBuilder;
    @Autowired private ParameterService parameterService;
    @Autowired private MessengerService messengerService;

    @FXML private VBox parentContainer;
    @FXML private Parent parentColor;
    @FXML private Parent parentConflictType;
    @FXML private Parent parentConflictValue;
    @FXML private Parent parentFunction;
    @FXML private Parent parentToken;

    @FXML private CheckBox checkConstant;
    @FXML private ChoiceBox<Colour> choiceColour;
//    @FXML private Button buttonColourCreate;
    @FXML private TextArea inputFunction;
    @FXML private TextField inputFilter;
    @FXML private TextField inputToken;
    @FXML private TextField inputTokenMin;
    @FXML private TextField inputTokenMax;
    @FXML private SwingNode imageFunction;
    
    @FXML private ChoiceBox<ConflictResolutionStrategy> choiceConflictRes;
    @FXML private TextField inputConflictType;
    
    @FXML private HBox boxConflictValue;
    @FXML private TextField inputConflictValue;
    @FXML private ChoiceBox<String> choiceConflictValue;

    @FXML private Menu menuLocalParams;
    @FXML private Menu menuGlobalParams;
    @FXML private Menu menuPlaces;
    @FXML private Menu menuTransitions;

    private PauseTransition pauseTransition;

    private IDataElement data;

    private int inputCaretPosition;

    public void setElement(IDataElement element) {

        data = element;

        choiceColour.getItems().clear();
        choiceColour.getItems().addAll(modelService.getModel().getColours());

        parentContainer.getChildren().clear();
        if (element != null) {
            switch (element.getType()) {
                case ARC:
                    parentContainer.getChildren().add(parentColor);
                    parentContainer.getChildren().add(parentFunction);
                    parentContainer.getChildren().add(new Separator(Orientation.HORIZONTAL));
                    parentContainer.getChildren().add(parentConflictValue);
                    setArc((DataArc) element);
                    break;

                case PLACE:
                    parentContainer.getChildren().add(parentColor);
                    parentContainer.getChildren().add(parentToken);
                    parentContainer.getChildren().add(parentConflictType);
                    setPlace((DataPlace) element);
                    break;

                case TRANSITION:
                    parentContainer.getChildren().add(parentFunction);
                    setTransition((DataTransition) element);
                    break;
            }
        }
    }

    private void setArc(DataArc arc) {
        for (Colour color : choiceColour.getItems()) {
            if (arc.getWeight(color) != null) {
                choiceColour.getSelectionModel().select(color);
                inputFunction.setText(arc.getWeight(color).getFunction().toString());
                break;
            }
        }
        
        DataPlace placeRelated; // related place to arc
        List<IArc> neighboringArcs; // list of incoming or outgoing arcs that contains this arc

        if (arc.getSource().getType() == DataType.PLACE) {
            placeRelated = (DataPlace) arc.getSource();
            neighboringArcs = placeRelated.getArcsOut();
        } else {
            placeRelated = (DataPlace) arc.getTarget();
            neighboringArcs = placeRelated.getArcsIn();
        }
        
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
        
        inputCaretPosition = inputFunction.getText().length();

        setFunctionReferenceChoices(inputFilter.getText().toLowerCase());
        setFunctionParameterChoices(arc, inputFilter.getText().toLowerCase());
    }

    private void setPlace(DataPlace place) {
        Token token;
        for (Colour color : choiceColour.getItems()) {
            if ((token = place.getToken(color)) != null) {
                checkConstant.setSelected(place.isConstant());
                choiceColour.getSelectionModel().select(color); // must be done here for listener
                inputToken.setText(Double.toString(token.getValueStart()));
                inputTokenMin.setText(Double.toString(token.getValueMin()));
                inputTokenMax.setText(Double.toString(token.getValueMax()));
                choiceConflictRes.getSelectionModel().select(place.getConflictResolutionType());
                break;
            }
        }
    }

    private void setTransition(DataTransition transition) {

        inputFunction.setText(transition.getFunction().toString());
        inputCaretPosition = inputFunction.getText().length();

        setFunctionReferenceChoices(inputFilter.getText().toLowerCase());
        setFunctionParameterChoices(transition, inputFilter.getText().toLowerCase());
    }
    
    private void ParseConstantChoice() {
        if (data instanceof IDataNode) {
            IDataNode node = (IDataNode) data;
            if (node.isConstant() != checkConstant.isSelected()) {
                node.setConstant(checkConstant.isSelected());
            }
        }
    }
    
    private void ParseConflictResolutionValue() {
        if (data instanceof DataArc) {
            DataArc arc = (DataArc) data;
            try {
                if (ValidateNumberInput(inputConflictValue)) {
                    arc.setConflictResolutionValue(Double.parseDouble(inputConflictValue.getText().replace(",", ".")));
                }
            } catch (NumberFormatException ex) {
                messengerService.addException("Exception parsing conflict resolution value!", ex);
            }
        }
    }

    private void ParsePlaceToken(IDataElement data) {

        if (data instanceof DataPlace) {

            DataPlace place = (DataPlace) data;
            Colour colour = (Colour) choiceColour.getSelectionModel().getSelectedItem();

            try {
                Token token = new Token(colour);
                if (ValidateNumberInput(inputToken)) {
                    token.setValueStart(Double.parseDouble(inputToken.getText().replace(",", ".")));
                }
                if (ValidateNumberInput(inputTokenMin)) {
                    token.setValueMin(Double.parseDouble(inputTokenMin.getText().replace(",", ".")));
                }
                if (ValidateNumberInput(inputTokenMax)) {
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

    private void ParseFunction(IDataElement element) {

        if (element != null) {

            Function func;
            String input;

            if (inputFunction.getText().isEmpty()) {
                input = "1";
            } else {
                input = inputFunction.getText().replace("\n", "");
            }

            try {
                func = parameterService.validateAndGetFunction(data, input);
                ParseFunctionToImage(input);

                modelService.setElementFunction(data, func, (Colour) choiceColour.getSelectionModel().getSelectedItem());
                inputFunction.setStyle("-fx-border-color: green");
                
            } catch (Exception ex) {
                inputFunction.setStyle("-fx-border-color: red");
                messengerService.setLeftStatus("Cannot build function! " + ex.getMessage());
            }
        }
    }

    private void ParseFunctionToImage(String input) throws Exception {

        final BufferedImage image;

        image = PrettyFormulaParser.parseToImage(input);
        if (imageFunction.getContent() != null) {
            imageFunction.getContent().removeAll();
        }

        SwingUtilities.invokeLater(() -> {
            ImageComponent img = new ImageComponent();
            img.setImage(image);
            imageFunction.setContent(img);
        });
    }

    /**
     * Inserts a string to the function input. Inserts the string at the latest
     * caret position, moves caret by string length.
     *
     * @param param
     */
    private void InsertToFunctionInput(String value) {
        String function;
        function = inputFunction.getText().substring(0, inputCaretPosition);
        function += value;
        function += inputFunction.getText().substring(inputCaretPosition);
        inputFunction.setText(function);
        inputCaretPosition = inputCaretPosition + value.length();
    }

    private void setFunctionReferenceChoices(String filter) {

        menuPlaces.getItems().clear();
        menuTransitions.getItems().clear();

        modelService.getModel().getPlaces().stream()
                .filter(place -> 
                        place.getId().toLowerCase().contains(filter) || 
                                ((DataPlace) place).getLabelText().contains(filter))
                .forEach(place -> {

//                    final Menu menuArcsIn = new Menu("Incoming Arcs");
//                    final Menu menuArcsOut = new Menu("Outgoing Arcs");
//                    
//                    if (place.getArcsIn().size() > 0) {
//
//                        place.getArcsIn().forEach(arc -> {
//
//                            String ident = arc.getSource().getId() + arc.getTarget().getId();
//
//                            MenuItem itemArcFlowDer = new MenuItem("Actual | f(t)");
//                            itemArcFlowDer.setOnAction(e -> {
//                                InsertToFunctionInput(ident + "_now");
//                            });
//
//                            MenuItem itemArcFlow = new MenuItem("Total | F(t)");
//                            itemArcFlow.setOnAction(e -> {
//                                InsertToFunctionInput(ident + "_total");
//                            });
//
//                            Menu menuArc = new Menu("Token Flow from " + arc.getSource().getId() + " (" + arc.getSource().getId() + "->" + arc.getTarget().getId() + ")");
//                            menuArc.getItems().add(itemArcFlowDer);
//                            menuArc.getItems().add(itemArcFlow);
//
//                            menuArcsIn.getItems().add(menuArc);
//                        });
//                    } else {
//                        menuArcsIn.setDisable(true);
//                    }
//                    
//                    if (place.getArcsOut().size() > 0) {
//
//                        place.getArcsOut().forEach(arc -> {
//
//                            String ident = arc.getSource().getId() + arc.getTarget().getId();
//
//                            MenuItem itemArcFlowDer = new MenuItem("Actual | f(t)");
//                            itemArcFlowDer.setOnAction(e -> {
//                                InsertToFunctionInput(ident + "_now");
//                            });
//
//                            MenuItem itemArcFlow = new MenuItem("Total | F(t)");
//                            itemArcFlow.setOnAction(e -> {
//                                InsertToFunctionInput(ident + "_total");
//                            });
//
//                            Menu menuArc = new Menu("Token Flow to " + arc.getTarget().getId() + " (" + arc.getSource().getId() + "->" + arc.getTarget().getId() + ")");
//                            menuArc.getItems().add(itemArcFlowDer);
//                            menuArc.getItems().add(itemArcFlow);
//
//                            menuArcsOut.getItems().add(menuArc);
//                        });
//                    } else {
//                        menuArcsOut.setDisable(true);
//                    }
//
//                    MenuItem itemToken = new MenuItem("Token");
//                    itemToken.setOnAction(e -> {
//                        InsertToFunctionInput(place.getId());
//                    });
//                    
//                    Menu menu = new Menu(place.toString());
//                    menu.setMnemonicParsing(false);
//                    menu.getItems().add(itemToken);
//                    menu.getItems().add(menuArcsIn);
//                    menu.getItems().add(menuArcsOut);
//                    menuPlaces.getItems().add(menu);
                    
                    MenuItem item = new MenuItem(place.toString());
                    item.setMnemonicParsing(false);
                    item.setOnAction(e -> {
                        InsertToFunctionInput(place.getId());
                    });
                    menuPlaces.getItems().add(item);
                });

        if (menuPlaces.getItems().isEmpty()) {
            menuPlaces.setDisable(true);
        } else {
            menuPlaces.setDisable(false);
        }

        modelService.getModel().getTransitions().stream()
                .filter(transition -> 
                        transition.getId().toLowerCase().contains(filter) || 
                                ((DataTransition) transition).getLabelText().contains(filter))
                .forEach(transition -> {

//                    MenuItem itemSpeed = new MenuItem("Speed | v(t)");
//                    itemSpeed.setOnAction(e -> {
//                        InsertToFunctionInput(transition.getId());
//                    });
//                    MenuItem itemFire = new MenuItem("Fire | 0 or 1");
//                    itemFire.setDisable(true);
//
//                    Menu menu = new Menu(transition.toString());
//                    menu.setMnemonicParsing(false);
//                    menu.getItems().add(itemFire);
//                    menu.getItems().add(itemSpeed);
//                    menuTransitions.getItems().add(menu);

                    MenuItem item = new MenuItem(transition.toString());
                    item.setMnemonicParsing(false);
                    item.setOnAction(e -> {
                        InsertToFunctionInput(transition.getId());
                    });
                    menuTransitions.getItems().add(item);
                });

        if (menuTransitions.getItems().isEmpty()) {
            menuTransitions.setDisable(true);
        } else {
            menuTransitions.setDisable(false);
        }
    }

    private void setFunctionParameterChoices(IDataElement element, String filter) {

        menuLocalParams.getItems().clear();
        menuGlobalParams.getItems().clear();

        parameterService.getSortedLocalParameters(element)
                .stream()
                .filter(param -> param.getId().toLowerCase().contains(filter))
                .forEach(param -> {
                    MenuItem item = new MenuItem(param.getId() + " = " + param.getValue());
                    item.setMnemonicParsing(false);
                    item.setOnAction(e -> {
                        InsertToFunctionInput(param.getId());
                    });
                    menuLocalParams.getItems().add(item);
                });

        if (menuLocalParams.getItems().isEmpty()) {
            menuLocalParams.setDisable(true);
        } else {
            menuLocalParams.setDisable(false);
        }

        parameterService.getSortedGlobalParameters(modelService.getModel())
                .stream()
                .filter(param -> param.getId().toLowerCase().contains(filter))
                .forEach(param -> {
                    MenuItem item = new MenuItem(param.toString());
                    item.setMnemonicParsing(false);
                    item.setOnAction(e -> {
                        InsertToFunctionInput(param.getId());
                    });
                    menuGlobalParams.getItems().add(item);
                });

        if (menuGlobalParams.getItems().isEmpty()) {
            menuGlobalParams.setDisable(true);
        } else {
            menuGlobalParams.setDisable(false);
        }
    }
    
    private void StoreConflictResolutionType(IDataElement element) throws DataException {
        if (element instanceof DataPlace) {
            DataPlace place = (DataPlace) element;

            ConflictResolutionStrategy conflictResType = choiceConflictRes.getSelectionModel().getSelectedItem();
            if (conflictResType != null) {
                modelService.ChangeConflictResolutionStrategy(modelService.getDao(), place, conflictResType);
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        pauseTransition = new PauseTransition(Duration.seconds(0.25));
        pauseTransition.setOnFinished(e -> {
            if (data instanceof DataTransition) {
                setFunctionParameterChoices((DataTransition) data, inputFilter.getText().toLowerCase());
                setFunctionReferenceChoices(inputFilter.getText().toLowerCase());
            }
        });
        checkConstant.selectedProperty().addListener(cl -> ParseConstantChoice());
        inputFilter.textProperty().addListener(cl -> {
            pauseTransition.playFromStart();
        });
        inputFunction.setOnMouseClicked(eh -> {
            inputCaretPosition = inputFunction.getCaretPosition();
        });
        inputFunction.setOnKeyReleased(eh -> {
            inputCaretPosition = inputFunction.getCaretPosition();
        });
        inputFunction.textProperty().addListener(e -> ParseFunction(data));
//        inputFunction.setOnKeyTyped(eh -> {
//            try {
//                PrettyFormulaParser.parseToImage(inputFunction.getText().replace("\n",""));
//            } catch (DetailedParseCancellationException ex) {
//                if (eh != null && eh.getCode() != KeyCode.RIGHT && eh.getCode() != KeyCode.LEFT && eh.getCode() != KeyCode.UNDERSCORE) {
////                    inputFunction.selectRange(ex.getCharPositionInLine(), ex.getEndCharPositionInLine());
//                }
//            }
//        });
        inputToken.textProperty().addListener(cl -> ParsePlaceToken(data));
        inputTokenMin.textProperty().addListener(cl -> ParsePlaceToken(data));
        inputTokenMax.textProperty().addListener(cl -> ParsePlaceToken(data));
//        inputWeight.textProperty().addListener(cl -> ParseArcWeight(data));

        choiceConflictRes.getItems().clear();
        for (ConflictResolutionStrategy type : ConflictResolutionStrategy.values()) {
            choiceConflictRes.getItems().add(type);
        }
        choiceConflictRes.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreConflictResolutionType(data);
                } catch (DataException ex) {
                    messengerService.addException("Cannot change conflict resolution type!", ex);
                }
            }
        });
        choiceConflictValue.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreConflictResolutionValue(data);
                } catch (DataException ex) {
                    messengerService.addException("Cannot change conflict resolution value!", ex);
                }
            }
        });
    }
}
