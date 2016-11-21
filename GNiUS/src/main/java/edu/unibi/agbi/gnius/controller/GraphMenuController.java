/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller;

import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;
import edu.unibi.agbi.gravisfx.layout.RandomLayout;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;

/**
 *
 * @author PR
 */
public class GraphMenuController implements Initializable
{
    @FXML private Button buttonLoad;
    @FXML private Button buttonCreate;
    @FXML private Button buttonAlign;
    
    @FXML private ChoiceBox choicesNodes;
    @FXML private ChoiceBox choicesAlignment;
    
    private int elements = 1;
    
    @FXML
    private void buttonLoadAllClicked(ActionEvent event) {
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
    private void buttonConnectAllClicked(ActionEvent event) {
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
    private void buttonAlignNodes(ActionEvent event) {
        System.out.println("Applying random layout.");
        RandomLayout.applyOn(Main.getGraph());
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        // TODO
    }
}
