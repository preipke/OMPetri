/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class GraphPaneController implements Initializable
{
    @Autowired private DataGraphService dataGraphService;
    @Autowired private SelectionService selectionService;

    @FXML private BorderPane editorPane;

    @Value("${css.editor.pane}") private String paneStyleClass;
    
    private GraphScene graphScene;

    public GraphPane getGraphPane() {
        return graphScene.getGraphPane();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        graphScene = new GraphScene(new Graph());
        graphScene.widthProperty().bind(editorPane.widthProperty());
        graphScene.heightProperty().bind(editorPane.heightProperty());
        graphScene.getGraphPane().getStyleClass().add(paneStyleClass);

        graphScene.getObjects().add(dataGraphService);
        graphScene.getObjects().add(selectionService);

        editorPane.setCenter(graphScene);
    }
}
