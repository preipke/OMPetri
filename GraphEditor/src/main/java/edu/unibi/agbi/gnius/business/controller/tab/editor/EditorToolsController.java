/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.business.mode.exception.EditorModeLockException;
import edu.unibi.agbi.gnius.core.dao.GraphDao;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;

import edu.unibi.agbi.gravisfx.presentation.layout.RandomLayout;
import edu.unibi.agbi.petrinet.entity.PN_Element;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorToolsController implements Initializable
{
    @FXML private ChoiceBox choicesAlignNodes;
    @FXML private ChoiceBox choicesCreateNode;
    
    @FXML private TextArea textLogArea;
    
    @Autowired private DataService dataService;
    @Autowired private GraphDao graphDao;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    
    int elements = 10;
    
    public void CreateNode(MouseEvent target) {
        try {
            dataService.create(((NodeTypeChoice) choicesCreateNode.getSelectionModel().getSelectedItem()).getType() , target , Point2D.ZERO);
        } catch (NodeCreationException | AssignmentDeniedException ex) {

        }
    }
    
    public void addToLog(String msg) {
        textLogArea.appendText(msg + "\n");
    }
    
    public void addToLog(Throwable thr) {
        textLogArea.appendText(thr.toString() + "\n");
    }
    
    @FXML
    private void buttonAlignNodes() {
        LayoutType type = ((LayoutTypeChoice) choicesAlignNodes.getSelectionModel().getSelectedItem()).getType();
        switch(type) {
            case RANDOM:
                RandomLayout.applyOn(graphDao);
                break;
            default:
                textLogArea.appendText("Align Nodes: no method selected!"); 
                return;
        }
    }
    
    @FXML
    public void buttonCreateNodeEnable() {
        try {
            mouseEventHandler.setNodeCreationMode();
        } catch (EditorModeLockException ex) {
            addToLog(getClass() + ":");
            addToLog(ex);
        }
    }
    
    @FXML
    private void buttonLoadExampleNodes() {
        IGraphNode place, transition;
        try {
            for (int i = 0; i < elements; i++) {
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.connect(place , transition);
                dataService.connect(transition , place);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.connect(transition , place);
                dataService.connect(place , transition);
                
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.connect(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.connect(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.connect(place , transition);
                
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.connect(place , transition);
                
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.connect(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.connect(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.connect(place , transition);
            }
        } catch (NodeCreationException | EdgeCreationException | AssignmentDeniedException ex) {
            addToLog(getClass() + ":");
            addToLog(ex);
        }
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        ObservableList<LayoutTypeChoice> alignChoices = FXCollections.observableArrayList();
        alignChoices.add(new LayoutTypeChoice(LayoutType.RANDOM, "Random"));
        
        choicesAlignNodes.setItems(alignChoices);
        choicesAlignNodes.getSelectionModel().selectFirst();
        
        ObservableList<NodeTypeChoice> nodeChoices = FXCollections.observableArrayList();
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.PLACE, "Place"));
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.TRANSITION, "Transition"));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }

    private class NodeTypeChoice
    {
        private final PN_Element.Type type;
        private final String name;

        public NodeTypeChoice(PN_Element.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        public PN_Element.Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class LayoutTypeChoice
    {
        private final LayoutType type;
        private final String name;

        public LayoutTypeChoice(LayoutType type, String name) {
            this.type = type;
            this.name = name;
        }

        public LayoutType getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum LayoutType
    {
        RANDOM, DEFAULT;
    }
}
