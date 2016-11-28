/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.fxml;

import edu.unibi.agbi.gnius.controller.data.DataController;
import edu.unibi.agbi.gnius.exception.data.EdgeCreationException;
import edu.unibi.agbi.gnius.exception.data.NodeCreationException;
import edu.unibi.agbi.gnius.handler.MouseGestures;
import edu.unibi.agbi.gnius.model.EdgeType;
import edu.unibi.agbi.gnius.model.LayoutType;
import edu.unibi.agbi.gnius.model.NodeType;

import edu.unibi.agbi.gravisfx.presentation.layout.RandomLayout;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;

import edu.unibi.agbi.petrinet.model.PNNode;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author PR
 */
public class GraphMenuController implements Initializable
{   
    @FXML private ChoiceBox choicesAlignNodes;
    @FXML private ChoiceBox choicesCreateNode;
    
    @FXML private TextArea textLogArea;
    
    int elements = 10;
    
    public void addChoices() {
        
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

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        MouseGestures.controller = this;
        addChoices();
    }
    
    @FXML
    private void buttonAlignNodes() {
        
        LayoutType.Type type = ((LayoutType) choicesAlignNodes.getSelectionModel().getSelectedItem()).getType();
        
        switch(type) {
            case RANDOM:
                RandomLayout.applyOn(PresentationController.getGraph());
                break;
            default:
                textLogArea.appendText("Align Nodes: no method selected!"); 
                return;
        }
        
        //RandomLayout.applyOn(Main.getGraph());
    }
    
    @FXML
    public void buttonCreateNodeEnable() {
        MouseGestures.setCreatingNodes(true);
    }
    
    /**
     * Invoked by clicking in the scene.
     * @param target 
     */
    public void createNode(MouseEvent target) {
        
        NodeType.Type type = ((NodeType) choicesCreateNode.getSelectionModel().getSelectedItem()).getType();
        
        try {
            IGravisNode shape = PresentationController.create(type , target);
            PNNode node = DataController.createNode(type);
            
            node.addShape(shape);
            shape.setRelatedObject(node);
            
        } catch (NodeCreationException ex) {
            textLogArea.appendText(ex.toString());
        }
        
        MouseGestures.setCreatingNodes(false);
    }
    
    public void copyNode(IGravisNode node, MouseEvent target) {
        
        if (IGravisNode.class.isAssignableFrom(target.getTarget().getClass())) {
            
            IGravisNode shape = (IGravisNode) target.getTarget();
            
        }
    }
    
    @FXML
    private void buttonLoadExampleNodes() {
        
        IGravisNode place, transition;
        try {
            for (int i = 0; i < elements; i++) {
                place = PresentationController.create(NodeType.Type.PLACE , null);
                transition = PresentationController.create(NodeType.Type.TRANSITION , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                place = PresentationController.create(NodeType.Type.PLACE , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                transition = PresentationController.create(NodeType.Type.TRANSITION , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                place = PresentationController.create(NodeType.Type.PLACE , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                place = PresentationController.create(NodeType.Type.PLACE , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                transition = PresentationController.create(NodeType.Type.TRANSITION , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                transition = PresentationController.create(NodeType.Type.TRANSITION , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                place = PresentationController.create(NodeType.Type.PLACE , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
                
                place = PresentationController.create(NodeType.Type.PLACE , null);
                PresentationController.create(EdgeType.Type.EDGE , place , transition);
            }
        } catch (NodeCreationException | EdgeCreationException ex) {
            System.out.println(ex.toString());
        }
    }
}
