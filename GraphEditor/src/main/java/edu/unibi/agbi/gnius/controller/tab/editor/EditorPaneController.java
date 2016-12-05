/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.tab.editor;

import edu.unibi.agbi.gnius.dao.GraphDao;
import edu.unibi.agbi.gnius.handler.KeyEventHandler;
import edu.unibi.agbi.gnius.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.handler.ScrollEventHandler;

import edu.unibi.agbi.gravisfx.presentation.GraphScene;

import java.net.URL;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorPaneController implements Initializable
{
    @FXML private BorderPane editorPane;
    
    @Autowired private GraphDao graph;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private KeyEventHandler keyEventHandler;
    @Autowired private ScrollEventHandler scrollEventHandler;
    
    private static GraphScene graphScene = null;

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        graphScene = new GraphScene(graph);
        graphScene.setId("editorScene");
        graphScene.widthProperty().bind(editorPane.widthProperty());
        graphScene.heightProperty().bind(editorPane.heightProperty());
        
        editorPane.setCenter(graphScene);
        
        String css = "-fx-background-color: white;"
                + "-fx-border-color: grey;"
        //        + "-fx-border-insets: 5;"
                + "-fx-border-width: 2;"
        //        + "-fx-border-style: dashed;"
        ;
        graphScene.getGraphPane().setStyle(css);
            
        // register handler
        mouseEventHandler.registerTo(graphScene.getGraphPane());
        scrollEventHandler.registerTo(graphScene.getGraphPane());
        keyEventHandler.setGraphPane(graphScene.getGraphPane());
    }
}