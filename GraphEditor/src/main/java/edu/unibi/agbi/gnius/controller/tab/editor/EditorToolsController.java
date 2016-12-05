/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.tab.editor;

import edu.unibi.agbi.gnius.dao.GraphDao;
import edu.unibi.agbi.gnius.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.service.model.LayoutSelectionChoice;
import edu.unibi.agbi.gnius.service.model.NodeSelectionChoice;
import edu.unibi.agbi.gnius.service.DataService;

import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.presentation.layout.RandomLayout;

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
    
    public LayoutSelectionChoice getLayoutChoice() {
        return (LayoutSelectionChoice) choicesAlignNodes.getSelectionModel().getSelectedItem();
    }
    
    public NodeSelectionChoice getNodeChoice() {
        return (NodeSelectionChoice) choicesCreateNode.getSelectionModel().getSelectedItem();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        ObservableList<LayoutSelectionChoice> alignChoices = FXCollections.observableArrayList();
        alignChoices.add(new LayoutSelectionChoice(LayoutSelectionChoice.Type.RANDOM, "Random"));
        alignChoices.add(new LayoutSelectionChoice(LayoutSelectionChoice.Type.DEFAULT, "..."));
        
        choicesAlignNodes.setItems(alignChoices);
        choicesAlignNodes.getSelectionModel().selectFirst();
        
        ObservableList<NodeSelectionChoice> nodeChoices = FXCollections.observableArrayList();
        nodeChoices.add(new NodeSelectionChoice(NodeSelectionChoice.Type.PLACE, "Place"));
        nodeChoices.add(new NodeSelectionChoice(NodeSelectionChoice.Type.TRANSITION, "Transition"));
        nodeChoices.add(new NodeSelectionChoice(NodeSelectionChoice.Type.DEFAULT, "..."));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void buttonAlignNodes() {
        
        LayoutSelectionChoice.Type type = ((LayoutSelectionChoice) choicesAlignNodes.getSelectionModel().getSelectedItem()).getType();
        
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
                place = dataService.create(NodeSelectionChoice.Type.PLACE , null, null);
                transition = dataService.create(NodeSelectionChoice.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(NodeSelectionChoice.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                transition = dataService.create(NodeSelectionChoice.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(NodeSelectionChoice.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(NodeSelectionChoice.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                transition = dataService.create(NodeSelectionChoice.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                transition = dataService.create(NodeSelectionChoice.Type.TRANSITION , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(NodeSelectionChoice.Type.PLACE , null, null);
                dataService.create(place , transition);
                
                place = dataService.create(NodeSelectionChoice.Type.PLACE , null, null);
                dataService.create(place , transition);
            }
        } catch (NodeCreationException | EdgeCreationException ex) {
            System.out.println(ex.toString());
        }
    }
}
