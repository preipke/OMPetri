/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller;

import edu.unibi.agbi.gnius.Main;
import edu.unibi.agbi.gravisfx.graph.entity.node.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.entity.node.GravisRectangle;
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
    
    @FXML
    private void buttonLoadAllClicked(ActionEvent event) {
        
        Main.getGraph().addNode(new GravisCircle("A", Color.DODGERBLUE));
        Main.getGraph().addNode(new GravisCircle("B", Color.DODGERBLUE));
        Main.getGraph().addNode(new GravisCircle("C", Color.DODGERBLUE));
        Main.getGraph().addNode(new GravisRectangle("A_B", Color.RED));
        Main.getGraph().addNode(new GravisRectangle("A_C", Color.RED));
        Main.getGraph().addNode(new GravisRectangle("A_D", Color.RED));
        Main.getGraph().addNode(new GravisCircle("D", Color.DODGERBLUE));
        Main.getGraph().addNode(new GravisCircle("E", Color.DODGERBLUE));
        Main.getGraph().addNode(new GravisRectangle("D_E", Color.RED));
        Main.getGraph().addNode(new GravisRectangle("E_D", Color.RED));
    }
    
    @FXML
    private void buttonConnectAllClicked(ActionEvent event) {
        
        Main.getGraph().connectNodes("A" , "A_B");
        Main.getGraph().connectNodes("A_B" , "B");
        Main.getGraph().connectNodes("A" , "A_C");
        Main.getGraph().connectNodes("A_C" , "C");
        Main.getGraph().connectNodes("A" , "A_D");
        Main.getGraph().connectNodes("A_D" , "D");
        
        Main.getGraph().connectNodes("D" , "D_E");
        Main.getGraph().connectNodes("D_E" , "E");
        Main.getGraph().connectNodes("E" , "E_D");
        Main.getGraph().connectNodes("E_D" , "D");
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
