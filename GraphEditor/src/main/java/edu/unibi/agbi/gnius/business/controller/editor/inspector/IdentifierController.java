/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.inspector;

import edu.unibi.agbi.gnius.core.service.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataArc;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.data.impl.DataTransition;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphPlace;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphTransition;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
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
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private NodeListController nodeListController;

    @FXML private Pane paneSample;
    @FXML private ChoiceBox choiceSubtype;
    @FXML private TextArea inputDescription;
    @FXML private TextField inputId;
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
            inputId.setText(element.getId());
            inputName.setText(element.getName());
            inputType.setText(element.getType().toString());
        } else {
            inputDescription.setText("");
            inputId.setText("");
            inputLabel.setText("");
            inputName.setText("");
            inputType.setText("");
        }
        LoadElementSubtype(element);
        LoadSampleShape(element);
    }

    private void LoadSampleShape(IDataElement element) {

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
            dataService.styleElement(sample);
        } catch (DataServiceException ex) {
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

    private void StoreElementType(IDataElement element) throws DataServiceException {
        Object subtype = choiceSubtype.getSelectionModel().getSelectedItem();
        if (subtype != null) {
            dataService.ChangeElementSubtype(element, subtype);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        pauseTransition = new PauseTransition(Duration.seconds(1));
        pauseTransition.setOnFinished(e -> nodeListController.Update());

        inputName.textProperty().addListener(cl -> {
            if (data != null && !data.getName().contentEquals(inputName.getText())) {
                data.setName(inputName.getText());
                pauseTransition.playFromStart();
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
                    LoadSampleShape(data);
                    choiceSubtype.setStyle("");
                } catch (DataServiceException ex) {
                    choiceSubtype.setStyle("-fx-border-color: red");
                    messengerService.addException("Cannot change element subtype!", ex);
                }
            }
        });
    }
}
