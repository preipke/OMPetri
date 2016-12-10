/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.core.model.EdgeTypeChoice;
import edu.unibi.agbi.gnius.core.model.PlaceTypeChoice;
import edu.unibi.agbi.gnius.core.model.TransitionTypeChoice;
import edu.unibi.agbi.gnius.core.model.entity.DataEdge;
import edu.unibi.agbi.gnius.core.model.entity.IDataNode;
import edu.unibi.agbi.gnius.core.model.entity.DataPlace;
import edu.unibi.agbi.gnius.core.model.entity.DataTransition;

import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;

import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorDetailsController implements Initializable
{
    @FXML private TextField detailsNodeType;
    @FXML private ChoiceBox detailsNodeSubtype;
    @FXML private VBox detailsInfoBox;
    @FXML private VBox detailsComputeBox;
    
    /**
     * Shows the details for the given entity.
     * @param selectable 
     */
    public void getDetails(IGravisSelectable selectable) {
        
        IDataNode node = (IDataNode) selectable.getRelatedObject();
        
        PN_Element.Type elementType = node.getElementType();
        
        ObservableList<Object> choices = FXCollections.observableArrayList();
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
                    choices.add(new PlaceTypeChoice(placeType , placeType.toString()));
                }
                break;
                
            case TRANSITION:
                DataTransition transition = (DataTransition) node;
                DataTransition.Type transitionType;
                for (int i = 0; i < DataTransition.Type.values().length; i++) {
                    transitionType = DataTransition.Type.values()[i];
                    if (transitionType == transition.getTransitionType()) {
                        selectionIndex = i;
                    }
                    choices.add(new TransitionTypeChoice(transitionType , transitionType.toString()));
                }
                break;
                
            case ARC:
                DataEdge edge = (DataEdge) node;
                DataEdge.Type edgeType;
                for (int i = 0; i < DataEdge.Type.values().length; i++) {
                    edgeType = DataEdge.Type.values()[i];
                    if (edgeType == edge.getArcType()) {
                        selectionIndex = i;
                    }
                    choices.add(new EdgeTypeChoice(edgeType , edgeType.toString()));
                }
                break;
                
            default:
                break;
        }
        
        detailsNodeType.setText(elementType.toString());
        
        detailsNodeSubtype.setItems(choices);
        detailsNodeSubtype.getSelectionModel().select(selectionIndex);
        
        detailsInfoBox.getChildren().clear();
        detailsComputeBox.getChildren().clear();
        
        List<Parameter> parameter = node.getParameter();

        FXMLLoader fxmlLoader;
        HBox parameterBox;
        ObservableList<Node> nodes;
        Label label;
        TextField textField;
        
        try {
            for (Parameter param : parameter) {
                
                fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/tab/editor/details/Parameter.fxml"));
                parameterBox = fxmlLoader.load();
                nodes = parameterBox.getChildren();

                for (int j = 0; j < nodes.size(); j++) {
                    System.out.println(j + ": " + nodes.get(j).toString());
                }
                
                label = (Label)((VBox)nodes.get(0)).getChildren().get(0);
                label.setText(param.getName());
                
                textField = (TextField)nodes.get(1);
                textField.setText("" + param.getNote());

                switch (param.getType()) {

                    case INFO:
                        detailsInfoBox.getChildren().add(parameterBox);
                        break;

                    case COMPUTE:
                        detailsComputeBox.getChildren().add(parameterBox);
                        break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception in EditorDetailsController - setDetails():");
            System.out.println(ex.toString());
        }
    }
    
    /**
     * Updates entries within the according entity.
     */
    public void setDetails() {
        
    }
    
    /**
     * Resets texfield entries, discarding changes.
     */
    public void reset() {
        
    }
    
    /**
     * Clears displayed elements. Use for deselecting.
     */
    public void clear() {
        detailsNodeType.setText("");
        detailsNodeSubtype.getItems().clear();
        detailsInfoBox.getChildren().clear();
        detailsComputeBox.getChildren().clear();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
}
