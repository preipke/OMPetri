/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.core.dao.GraphDao;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.model.LayoutTypeChoice;
import edu.unibi.agbi.gnius.core.model.NodeTypeChoice;
import edu.unibi.agbi.gnius.core.service.DataService;

import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.presentation.layout.RandomLayout;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
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
    
    public LayoutTypeChoice getLayoutChoice() {
        return (LayoutTypeChoice) choicesAlignNodes.getSelectionModel().getSelectedItem();
    }
    
    public NodeTypeChoice getNodeChoice() {
        return (NodeTypeChoice) choicesCreateNode.getSelectionModel().getSelectedItem();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        ObservableList<LayoutTypeChoice> alignChoices = FXCollections.observableArrayList();
        alignChoices.add(new LayoutTypeChoice(LayoutTypeChoice.Type.RANDOM, "Random"));
        alignChoices.add(new LayoutTypeChoice(LayoutTypeChoice.Type.DEFAULT, "..."));
        
        choicesAlignNodes.setItems(alignChoices);
        choicesAlignNodes.getSelectionModel().selectFirst();
        
        ObservableList<NodeTypeChoice> nodeChoices = FXCollections.observableArrayList();
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.PLACE, "Place"));
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.TRANSITION, "Transition"));
        nodeChoices.add(new NodeTypeChoice(PN_Element.Type.ARC, "..."));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void buttonAlignNodes() {
        
        LayoutTypeChoice.Type type = ((LayoutTypeChoice) choicesAlignNodes.getSelectionModel().getSelectedItem()).getType();
        
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
        mouseEventHandler.setCreatingNodes(true);
    }
    
    @FXML
    private void buttonLoadExampleNodes() {
        
        IGravisNode place, transition;
        try {
            for (int i = 0; i < elements; i++) {
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                transition = dataService.create(PN_Element.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(PN_Element.Type.PLACE , null, null);
                dataService.create(place , transition);
            }
        } catch (NodeCreationException | EdgeCreationException ex) {
            System.out.println(ex.toString());
        }
    }
}
