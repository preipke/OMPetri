/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller;

import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gnius.handler.MouseGestures;
import edu.unibi.agbi.gravisfx.graph.layout.GravisLayoutType;
import edu.unibi.agbi.gravisfx.graph.layout.GravisLayoutType.LayoutType;
import edu.unibi.agbi.gravisfx.graph.node.GravisNodeType;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;
import edu.unibi.agbi.gravisfx.graph.layout.algorithm.RandomLayout;
import edu.unibi.agbi.gravisfx.graph.node.GravisNodeType.NodeType;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author PR
 */
public class GraphMenuController implements Initializable
{   
    @FXML private ChoiceBox choicesAlignNodes;
    @FXML private ChoiceBox choicesCreateNode;
    
    @FXML private TextArea textLogArea;
    
    int elements = 1;
    
    public void addChoices() {
        
        ObservableList<GravisLayoutType> alignChoices = FXCollections.observableArrayList();
        alignChoices.add(new GravisLayoutType(LayoutType.RANDOM, "Random"));
        alignChoices.add(new GravisLayoutType(LayoutType.DEFAULT, "..."));
        
        choicesAlignNodes.setItems(alignChoices);
        choicesAlignNodes.getSelectionModel().selectFirst();
        
        ObservableList<GravisNodeType> nodeChoices = FXCollections.observableArrayList();
        nodeChoices.add(new GravisNodeType(NodeType.CIRCLE, "Place"));
        nodeChoices.add(new GravisNodeType(NodeType.RECTANGLE, "Transition"));
        nodeChoices.add(new GravisNodeType(NodeType.DEFAULT, "..."));
        
        choicesCreateNode.setItems(nodeChoices);
        choicesCreateNode.getSelectionModel().selectFirst();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        MouseGestures.controller = this;
        addChoices();
    }
    
    @FXML
    private void buttonLoadAllClicked() {
        for (int i = 0; i < elements; i++) {
            Main.getGraph().addNode(new GravisCircle("A" + i , Color.YELLOWGREEN));
            Main.getGraph().addNode(new GravisCircle("B" + i , Color.YELLOWGREEN));
            Main.getGraph().addNode(new GravisCircle("C" + i , Color.YELLOWGREEN));
            Main.getGraph().addNode(new GravisRectangle("A_B" + i , Color.RED));
            Main.getGraph().addNode(new GravisRectangle("A_C" + i , Color.RED));
            Main.getGraph().addNode(new GravisRectangle("A_D" + i , Color.RED));
            Main.getGraph().addNode(new GravisCircle("D" + i , Color.DODGERBLUE));
            Main.getGraph().addNode(new GravisCircle("E" + i , Color.DODGERBLUE));
            Main.getGraph().addNode(new GravisRectangle("D_E" + i , Color.PURPLE));
            Main.getGraph().addNode(new GravisRectangle("E_D" + i , Color.PURPLE));
        }
    }
    
    @FXML
    private void buttonConnectAllClicked() {
        for (int i = 0; i < elements; i++) {
            Main.getGraph().createEdge("A" + i , "A_B" + i);
            Main.getGraph().createEdge("A_B" + i , "B" + i);
            Main.getGraph().createEdge("A" + i , "A_C" + i);
            Main.getGraph().createEdge("A_C" + i , "C" + i);
            Main.getGraph().createEdge("A" + i , "A_D" + i);
            Main.getGraph().createEdge("A_D" + i , "D" + i);
            Main.getGraph().createEdge("D" + i , "D_E" + i);
            Main.getGraph().createEdge("D_E" + i , "E" + i);
            Main.getGraph().createEdge("E" + i , "E_D" + i);
            Main.getGraph().createEdge("E_D" + i , "D" + i);
        }
    }
    
    @FXML
    private void buttonAlignNodes() {
        
        LayoutType type = ((GravisLayoutType) choicesAlignNodes.getSelectionModel().getSelectedItem()).getType();
        
        switch(type) {
            case RANDOM:
                RandomLayout.applyOn(Main.getGraph());
                break;
            default:
                textLogArea.appendText("Align Nodes: no method selected!"); 
                return;
        }
        
        //RandomLayout.applyOn(Main.getGraph());
    }
    
    @FXML
    public void buttonCreateNodeEnable() {
        MouseGestures.setCreatingNode(true);
    }
    
    /**
     * Invoked on clicking in the scene.
     * @param event 
     */
    public void createNode(MouseEvent event) {
        
        NodeType type = ((GravisNodeType) choicesCreateNode.getSelectionModel().getSelectedItem()).getType();
        
        IGravisNode node;
        
        switch(type) {
            case CIRCLE:
                node = new GravisCircle(event.toString()); // TODO create temp id
                break;
            case RECTANGLE:
                node = new GravisRectangle(event.toString()); // TODO create temp id
                break;
            default:
                textLogArea.appendText("Create Node: no node type selected!"); 
                return;
        }
        
        MouseGestures.setCreatingNode(false);
        
        node.setTranslate(
                (event.getX() - Main.getGraph().getTopLayer().translateXProperty().get()) / Main.getGraph().getTopLayer().getScaleTransform().getX() ,
                (event.getY() - Main.getGraph().getTopLayer().translateYProperty().get()) / Main.getGraph().getTopLayer().getScaleTransform().getX()
        );
        
        Main.getGraph().addNode(node);
    }
}
