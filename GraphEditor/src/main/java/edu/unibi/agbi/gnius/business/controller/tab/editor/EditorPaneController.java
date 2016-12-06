/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab.editor;

import edu.unibi.agbi.gnius.core.dao.GraphDao;
import edu.unibi.agbi.gnius.business.handler.KeyEventHandler;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;

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
    @Autowired private ScrollEventHandler scrollEventHandler;
    
    private static GraphScene graphScene = null;

    @Override
    public void initialize(URL location , ResourceBundle resources) {
        
        graphScene = new GraphScene(graph);
        graphScene.widthProperty().bind(editorPane.widthProperty());
        graphScene.heightProperty().bind(editorPane.heightProperty());
        graphScene.getGraphPane().getStyleClass().add("editorPane");
        
        editorPane.setCenter(graphScene);
            
        // register handler
        mouseEventHandler.registerTo(graphScene.getGraphPane());
        scrollEventHandler.registerTo(graphScene.getGraphPane());
    }
}