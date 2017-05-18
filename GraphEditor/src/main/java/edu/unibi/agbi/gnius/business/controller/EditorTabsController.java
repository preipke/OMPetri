/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller;

import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorTabsController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private ScrollEventHandler scrollEventHandler;

    @FXML private TabPane editorTabPane;
    @FXML private Button buttonCreateTab;

    @Value("${css.editor.pane}") private String paneStyleClass;
    
    private GraphScene graphScene;

    public GraphPane getGraphPane() {
        return graphScene.getGraphPane();
    }
    
    public void CreateModelTab(DataDao dataDao) {
        
        final DataDao dao;
        if (dataDao == null) {
            dao = new DataDao(1, 1, 1);
        } else {
            dao = dataDao;
            dao.getGraph().getNodes().forEach(node -> {
                try {
                    dataService.styleElement((IGraphNode) node);
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
                }
            });
            dao.getGraph().getConnections().forEach(connection -> {
                try {
                    dataService.styleElement((IGraphNode) connection);
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
                }
            });
        }
        
        GraphScene scene = new GraphScene(dao.getGraph());
        scene.widthProperty().bind(editorTabPane.widthProperty());
        scene.heightProperty().bind(editorTabPane.heightProperty());
        scene.getGraphPane().getStyleClass().add(paneStyleClass);
        
        mouseEventHandler.registerTo(scene.getGraphPane());
        scrollEventHandler.registerTo(scene.getGraphPane());
        
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(5));
        pane.setCenter(scene);
        
        Tab tab = new Tab("Model");
        tab.setContent(pane);
        tab.selectedProperty().addListener(cl -> {
            if (tab.isSelected()) {
                dataService.setActiveDataDao(dao);
            }
        });
        
        editorTabPane.getTabs().add(0, tab);
        editorTabPane.getSelectionModel().select(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonCreateTab.setOnAction(el -> CreateModelTab(null));
    }
}
