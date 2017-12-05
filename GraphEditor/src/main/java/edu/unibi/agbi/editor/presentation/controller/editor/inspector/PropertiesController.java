/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.inspector;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ParameterService;
import edu.unibi.agbi.petrinet.model.Colour;
import edu.unibi.agbi.petrinet.model.Token;
import edu.unibi.agbi.petrinet.util.FunctionBuilder;
import edu.unibi.agbi.prettyformulafx.main.ImageComponent;
import edu.unibi.agbi.prettyformulafx.main.PrettyFormulaParser;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javax.swing.SwingUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class PropertiesController implements Initializable
{
    @Autowired private ModelService dataService;
    @Autowired private FunctionBuilder functionBuilder;
    @Autowired private ParameterService parameterService;
    @Autowired private MessengerService messengerService;

    @FXML private VBox parentContainer;
    @FXML private Parent parentColor;
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

    @FXML private Menu menuLocalParams;
    @FXML private Menu menuGlobalParams;
    @FXML private Menu menuPlaces;
    @FXML private Menu menuTransitions;

    private PauseTransition pauseTransition;

    private IDataElement data;

    private String inputLatestValid;
    private int inputCaretPosition;

    public void setElement(IDataElement element) {

        data = element;

        choiceColour.getItems().clear();
        choiceColour.getItems().addAll(dataService.getModel().getColours());

        parentContainer.getChildren().clear();
        if (element != null) {
            switch (element.getType()) {
                case ARC:
                    parentContainer.getChildren().add(parentColor);
                    parentContainer.getChildren().add(parentFunction);
                    setArc((DataArc) element);
                    break;

                case PLACE:
                    parentContainer.getChildren().add(parentColor);
                    parentContainer.getChildren().add(parentToken);
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
                dataService.setPlaceToken(place, token);
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
            
            String input = inputFunction.getText().replace("\n", "");

            try {
                parameterService.ValidateFunction(element, input);
                ParseFunctionToImage(input);

                inputLatestValid = input;
                inputFunction.setStyle("-fx-border-color: green");
            } catch (Exception ex) {
                inputFunction.setStyle("-fx-border-color: red");
            }

            try {
                if (inputFunction.getText().isEmpty()) {
                    dataService.setElementFunction(element, "1", (Colour) choiceColour.getSelectionModel().getSelectedItem());
                } else {
                    dataService.setElementFunction(element, inputLatestValid, (Colour) choiceColour.getSelectionModel().getSelectedItem());
                }
            } catch (DataException ex) {
                messengerService.addException("Cannot build function from input '" + inputLatestValid + "'!", ex);
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

        dataService.getModel().getPlaces().stream()
                .filter(place
                        -> place.getId().toLowerCase().contains(filter)
//                        || place.getName().toLowerCase().contains(filter)
                        || ((DataPlace) place).getLabelText().contains(filter))
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

        dataService.getModel().getTransitions().stream()
                .filter(transition
                        -> transition.getId().toLowerCase().contains(filter)
//                        || transition.getName().toLowerCase().contains(filter)
                        || ((DataTransition) transition).getLabelText().contains(filter))
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

        parameterService.getLocalParameters(element).stream()
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

        parameterService.getGlobalParameters().stream()
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
    }
}
