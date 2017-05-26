/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor;

import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.business.controller.menu.FileMenuController;
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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    @Autowired private FileMenuController fileMenuController;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private ScrollEventHandler scrollEventHandler;

    @FXML private TabPane editorTabPane;
    @FXML private Button buttonCreateTab;

    @Value("${css.editor.pane}") private String paneStyleClass;
    
    private GraphPane activePane;
    
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
                    messengerService.addToLog("Failed to style nodes for existing model.", ex);
                }
            });
            dao.getGraph().getConnections().forEach(connection -> {
                try {
                    dataService.styleElement((IGraphArc) connection);
                } catch (DataServiceException ex) {
                    messengerService.addToLog("Failed to style arcs for existing model.", ex);
                }
            });
        }
        
        GraphScene scene = new GraphScene(dao.getGraph());
        scene.widthProperty().bind(editorTabPane.widthProperty());
        scene.heightProperty().bind(editorTabPane.heightProperty());
        scene.getGraphPane().getStyleClass().add(paneStyleClass);
        
        mouseEventHandler.registerTo(scene.getGraphPane());
        scrollEventHandler.registerTo(scene.getGraphPane(), dao);
        
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
                activePane = scene.getGraphPane();
                dataService.setActiveDataDao(dao);
                mainController.HideElementPanel();
                mainController.ShowModel(dao);
            }
        });
        tab.setOnCloseRequest(eh -> {
            if (dao.hasChanges()) {
                ShowConfirmationDialog(eh);
            }
        });
        tab.setOnClosed(eh -> {
            dataService.removeDataDao(dao);
            dao.clear();
        });
        
        editorTabPane.getTabs().add(0, tab);
        editorTabPane.getSelectionModel().select(0);
        
        messengerService.setTopStatus("New model created!", null);
    }
    
    public GraphPane getActiveGraphPane() {
        return activePane;
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
    
    private void ShowConfirmationDialog(Event event) {

        DataDao dao = dataService.getActiveDao();

        ButtonType buttonSave = new ButtonType("Save");
        ButtonType buttonClose = new ButtonType("Close");
        ButtonType buttonCancel = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close model");
        alert.setHeaderText("Confirm closing model '" + dao.getModel().getName() + "'");
        alert.setContentText("The model contains unsaved work. Are you sure you want to close the model and discard any changes?");
        alert.getButtonTypes().setAll(buttonSave, buttonClose, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            boolean isSaved = fileMenuController.SaveAs(dao);
            if (!isSaved && event != null) {
                event.consume();
            }
        } else if (result.get() == buttonCancel) {
            if (event != null) {
                event.consume();
            }
        }
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
