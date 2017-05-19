/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor;

import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import edu.unibi.agbi.gravisfx.presentation.GraphScene;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.Observable;
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
public class TabsController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    
    @Autowired private MainController mainController;
    
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
            dao = dataService.createDao();
        } else {
            dao = dataDao;
            dao.getGraph().getNodes().forEach(node -> {
                try {
                    dataService.styleElement((IGraphNode) node);
                } catch (DataServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
                }
            });
            dao.getGraph().getConnections().forEach(connection -> {
                try {
                    dataService.styleElement((IGraphArc) connection);
                } catch (DataServiceException ex) {
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
        tab.setText(getTabName(tab, dao.getModel().getName()));
        dao.getModel().getNameProperty().addListener(cl -> {
            tab.setText(getTabName(tab, dao.getModel().getName()));
        });
        tab.selectedProperty().addListener(cl -> {
            if (tab.isSelected()) {
                dataService.setActiveDataDao(dao);
                mainController.HideElementPanel();
                mainController.ShowModel(dao);
            }
        });
        tab.onCloseRequestProperty().addListener(cl -> {
            System.out.println("Fire close request!");
        });
        tab.onClosedProperty().addListener(cl -> {
            System.out.println("Fire closed!");
        });
        
        editorTabPane.getTabs().add(0, tab);
        editorTabPane.getSelectionModel().select(0);
    }
    
    private String getTabName(Tab tab, String modelName) {

        String prefix = null;
        for (Tab t : editorTabPane.getTabs()) {

            if (t.equals(tab)) {
                continue;
            }

            // models with similar name
            if (t.getText() != null && t.getText().contains(modelName)) {

                int indexStart = t.getText().indexOf(modelName);
                prefix = t.getText().substring(indexStart);

                // models have exact same name
                if (prefix.contentEquals(modelName)) {
                    prefix = t.getText().substring(0, indexStart).trim();

                    // generate prefix
                    if (prefix.matches("\\([0-9]+\\)")) {
                        prefix = prefix.replace("(", "").replace(")", "");
                        prefix = "(" + (Integer.parseInt(prefix) + 1) + ") ";
                        break;
                    } else if (prefix.isEmpty()) {
                        prefix = "(1) ";
                        break;
                    }
                }
            }
            prefix = "";
        }
        return prefix + modelName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonCreateTab.setOnAction(el -> CreateModelTab(null));
        editorTabPane.getTabs().remove(0);
        editorTabPane.getTabs().addListener((Observable ll) -> {
            if (editorTabPane.getTabs().size() == 1) {
                mainController.HideModelPanel();
            }
        });
    }
}
