/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.core.model.dao.GraphDao;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;

import edu.unibi.agbi.gravisfx.presentation.GraphScene;

import java.net.URL;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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
    @Autowired private GraphDao graph;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private ScrollEventHandler scrollEventHandler;

    @FXML private BorderPane editorPane;
    
    @Value("${css.editor.pane}")
    private String paneStyleClass;
    
    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        GraphScene graphScene = new GraphScene(graph);
        graphScene.widthProperty().bind(editorPane.widthProperty());
        graphScene.heightProperty().bind(editorPane.heightProperty());
        graphScene.getGraphPane().getStyleClass().add(paneStyleClass);
        
        editorPane.setCenter(graphScene);
            
        // register handler
        mouseEventHandler.registerTo(graphScene.getGraphPane()); // also registers key event handler
        scrollEventHandler.registerTo(graphScene.getGraphPane());
    }
    
    public Stage getStage() {
        return (Stage) editorPane.getScene().getWindow();
    }
}