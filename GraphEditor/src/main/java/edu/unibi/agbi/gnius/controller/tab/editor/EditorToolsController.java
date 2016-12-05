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
import edu.unibi.agbi.gnius.service.model.EdgeType;
import edu.unibi.agbi.gnius.service.model.LayoutType;
import edu.unibi.agbi.gnius.service.model.NodeType;
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
    
    public LayoutType getLayoutChoice() {
        return (LayoutType) choicesAlignNodes.getSelectionModel().getSelectedItem();
    }
    
    public NodeType getNodeChoice() {
        return (NodeType) choicesCreateNode.getSelectionModel().getSelectedItem();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        ObservableList<LayoutType> alignChoices = FXCollections.observableArrayList();
        alignChoices.add(new LayoutType(LayoutType.Type.RANDOM, "Random"));
        alignChoices.add(new LayoutType(LayoutType.Type.DEFAULT, "..."));
        
        choicesAlignNodes.setItems(alignChoices);
        choicesAlignNodes.getSelectionModel().selectFirst();
        
        ObservableList<NodeType> nodeChoices = FXCollections.observableArrayList();
        nodeChoices.add(new NodeType(NodeType.Type.PLACE, "Place"));
        nodeChoices.add(new NodeType(NodeType.Type.TRANSITION, "Transition"));
        nodeChoices.add(new NodeType(NodeType.Type.DEFAULT, "..."));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void buttonAlignNodes() {
        
        LayoutType.Type type = ((LayoutType) choicesAlignNodes.getSelectionModel().getSelectedItem()).getType();
        
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
                place = dataService.create(NodeType.Type.PLACE , null, null);
                transition = dataService.create(NodeType.Type.TRANSITION , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                place = dataService.create(NodeType.Type.PLACE , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                transition = dataService.create(NodeType.Type.TRANSITION , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                place = dataService.create(NodeType.Type.PLACE , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                place = dataService.create(NodeType.Type.PLACE , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                transition = dataService.create(NodeType.Type.TRANSITION , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                transition = dataService.create(NodeType.Type.TRANSITION , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                place = dataService.create(NodeType.Type.PLACE , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
                
                place = dataService.create(NodeType.Type.PLACE , null, null);
                dataService.create(EdgeType.Type.EDGE , place , transition);
            }
        } catch (NodeCreationException | EdgeCreationException ex) {
            System.out.println(ex.toString());
        }
    }
}
