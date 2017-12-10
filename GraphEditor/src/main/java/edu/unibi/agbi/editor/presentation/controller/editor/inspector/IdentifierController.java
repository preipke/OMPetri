/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.inspector;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.service.FactoryService;
import edu.unibi.agbi.editor.core.data.entity.data.IDataElement;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataArc;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataPlace;
import edu.unibi.agbi.editor.core.data.entity.data.impl.DataTransition;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.MessengerService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class IdentifierController implements Initializable
{
    @Autowired private FactoryService factoryService;
    @Autowired private ModelService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private NodeListController nodeListController;

    @FXML private Pane paneSample;
    @FXML private ChoiceBox choiceSubtype;
    @FXML private TextArea inputDescription;
    @FXML private TextField inputLabel;
    @FXML private TextField inputName;
    @FXML private TextField inputType;

    private IDataElement data;
    private PauseTransition pauseTransition;

    public void setElement(IDataElement element) {

        data = element;
        if (element != null) {
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
            inputType.setText(element.getType().toString());
        } else {
            inputDescription.setText("");
            inputLabel.setText("");
            inputName.setText("");
            inputType.setText("");
        }
        LoadElementSubtype(element);
        DisplaySampleShape(element);
    }

    private void DisplaySampleShape(IDataElement element) {

        IGraphElement sample;
        double width = 115;
        double height = 115;

        paneSample.getChildren().clear();

        if (element == null) {
            return;
        }

        switch (element.getType()) {

            case ARC:
                IGraphNode source = new GraphPlace(null, new DataPlace(null, null));
                IGraphNode target = new GraphTransition(null, new DataTransition(null, null));
                source.translateXProperty().set(width * 1 / 3 - source.getCenterOffsetX());
                source.translateYProperty().set(height * 1 / 3 - source.getCenterOffsetY());
                target.translateXProperty().set(width * 3 / 4 - target.getCenterOffsetX());
                target.translateYProperty().set(height * 5 / 6 - target.getCenterOffsetY());
                sample = new GraphArc(null, source, target, (DataArc) element);
                break;

            case PLACE:
                sample = new GraphPlace(null, (DataPlace) element);
                sample.translateXProperty().set(width / 2 - sample.getCenterOffsetX());
                sample.translateYProperty().set(height / 2 - sample.getCenterOffsetY());
                break;

            case TRANSITION:
                sample = new GraphTransition(null, (DataTransition) element);
                sample.translateXProperty().set(width / 2 - sample.getCenterOffsetX());
                sample.translateYProperty().set(height / 2 - sample.getCenterOffsetY());
                break;

            default:
                return;
        }

        try {
            paneSample.getChildren().addAll(sample.getShapes());
            factoryService.StyleElement(sample);
        } catch (DataException ex) {
            messengerService.addException("Cannot render sample shape.", ex);
        } finally {
            element.getShapes().remove(sample);
        }
    }

    /**
     * Loads the element type information from the given data element. Fills the
     * GUI with type information and according subtype choices.
     *
     * @param element
     */
    private void LoadElementSubtype(IDataElement element) {

        choiceSubtype.getItems().clear();

        if (element == null) {
            return;
        }

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

    private void StoreElementType(IDataElement element) throws DataException {
        Object subtype = choiceSubtype.getSelectionModel().getSelectedItem();
        if (subtype != null) {
            dataService.changeSubtype(element, subtype);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(e -> nodeListController.Update());

        inputName.textProperty().addListener(cl -> {
            if (data != null && !data.getId().contentEquals(inputName.getText())) {
                try {
                    dataService.changeElementId(data, inputName.getText());
                    inputName.setStyle("-fx-border-color: green");
                    pauseTransition.playFromStart();
                } catch (DataException ex) {
                    inputName.setStyle("-fx-border-color: red");
                    messengerService.addException("Cannot change element name!", ex);
                }
            }
        });
        inputLabel.textProperty().addListener(cl -> {
            if (data != null && !data.getLabelText().contentEquals(inputLabel.getText())) {
                data.setLabelText(inputLabel.getText());
                pauseTransition.playFromStart();
            }
        });
        inputDescription.textProperty().addListener(cl -> {
            if (data != null) {
                data.setDescription(inputDescription.getText());
            }
        });

        choiceSubtype.valueProperty().addListener(cl -> {
            if (data != null) {
                try {
                    StoreElementType(data);
                    DisplaySampleShape(data);
                    choiceSubtype.setStyle("");
                } catch (DataException ex) {
                    choiceSubtype.setStyle("-fx-border-color: red");
                    messengerService.addException("Cannot change element subtype!", ex);
                }
            }
        });
    }
}
