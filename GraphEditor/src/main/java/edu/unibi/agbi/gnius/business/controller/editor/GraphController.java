/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor;

import edu.unibi.agbi.gnius.business.controller.editor.graph.ElementController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.ParameterController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.ZoomController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.HierarchyController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.ModelController;
import edu.unibi.agbi.gnius.business.controller.menu.FileMenuController;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class GraphController implements Initializable
{
    @Autowired private ConfigurableApplicationContext springContext;
    
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    
    @Autowired private ModelController modelController;
    @Autowired private ElementController elementController;
    @Autowired private FileMenuController fileMenuController;
    @Autowired private HierarchyController hierarchyController;
    @Autowired private ParameterController parameterController;
    @Autowired private ZoomController zoomController;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private ScrollEventHandler scrollEventHandler;

    @Value("${css.editor.pane}") private String paneStyleClass;

    @FXML private TabPane editorTabPane;
    @FXML private Button buttonCreateTab;
    
    private final String fxmlElement = "/fxml/editor/graph/Element.fxml";
    private final String fxmlHierarchy = "/fxml/editor/graph/Hierarchy.fxml";
    private final String fxmlPanel = "/fxml/editor/graph/Panel.fxml";
    private final String fxmlParameter = "/fxml/editor/graph/Parameter.fxml";
    private final String fxmlZoom = "/fxml/editor/graph/Zoom.fxml";
    
    private Parent paneLeft;
    private Parent paneElement;
    private Parent paneHierarchy;
    private Parent paneParameter;
    private Parent paneZoom;
    
    private StackPane stackPaneActive;
    
    public void CreateTab() {
        CreateTab(null);
    }
    
    public void CreateTab(DataDao dataDao) {
        
        final DataDao dao;
        if (dataDao == null) {
            dao = dataService.createDao();
        } else {
            dao = dataDao;
        }
        
        SubScene scene = new SubScene(dao.getGraphPane(), 0, 0);
        scene.setManaged(false);
        scene.widthProperty().bind(editorTabPane.widthProperty());
        scene.heightProperty().bind(editorTabPane.heightProperty());
        
        dao.getGraphPane().maxHeightProperty().bind(scene.heightProperty());
        dao.getGraphPane().maxWidthProperty().bind(scene.widthProperty());
        dao.getGraphPane().getStyleClass().add(paneStyleClass);
        
        mouseEventHandler.registerTo(dao.getGraphPane());
        scrollEventHandler.registerTo(dao);
        
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(5));
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(scene);
        
        Tab tab = new Tab("Model");
        tab.setContent(pane);
        tab.setText(getTabName(tab, dao.getModelName()));
        dao.modelNameProperty().addListener(cl -> {
            tab.setText(getTabName(tab, dao.getModelName())); // todo: check names for all tabs
        });
        tab.selectedProperty().addListener(cl -> {
            if (tab.isSelected()) {
                ShowGraph(pane, dao);
            }
        });
        tab.setOnCloseRequest(eh -> {
            if (dao.hasChanges()) {
                ShowConfirmationDialog(eh);
            }
        });
        tab.setOnClosed(eh -> {
            dataService.remove(dao);
            dao.clear();
        });
        
        editorTabPane.getTabs().add(editorTabPane.getTabs().size() - 1, tab);
        editorTabPane.getSelectionModel().select(editorTabPane.getTabs().size() - 2);
        zoomController.CenterNodes();
        
        messengerService.printMessage("New model created!");
    }
    
    public void HideElementPane() {
        if (stackPaneActive != null) {
            stackPaneActive.getChildren().remove(paneElement);
            stackPaneActive.getChildren().remove(paneParameter);
        }
    }
    
    public void ShowGraph(StackPane pane, DataDao dao) {
        stackPaneActive = pane;
        if (pane != null && dao != null) {
            
            HideElementPane();
            
            pane.getChildren().add(paneHierarchy);
            pane.getChildren().add(paneLeft);
            pane.getChildren().add(paneZoom);
            
            StackPane.setAlignment(paneHierarchy, Pos.BOTTOM_LEFT);
            StackPane.setAlignment(paneLeft, Pos.TOP_LEFT);
            StackPane.setAlignment(paneZoom, Pos.BOTTOM_CENTER);
//            StackPane.setMargin(paneZoom, new Insets(0, 0, 5, 0));

            dataService.setDao(dao);
            modelController.setDao(dao);
            hierarchyController.setDao(dao);
        }
    }
    
    public void ShowElementPane(IGraphElement element) {
        stackPaneActive.getChildren().remove(paneParameter);
        stackPaneActive.getChildren().add(paneElement);
        StackPane.setAlignment(paneElement, Pos.TOP_RIGHT);
        elementController.ShowElementDetails(element);
    }
    
    public void ShowParameterPane(IDataElement element) {
        stackPaneActive.getChildren().remove(paneElement);
        stackPaneActive.getChildren().add(paneParameter);
        StackPane.setAlignment(paneParameter, Pos.TOP_RIGHT);
        parameterController.ShowParameters(element);
    }
    
    private String getTabName(Tab tab, String modelName) {

        String prefix;
        int count = 0, index;
        
        for (Tab t : editorTabPane.getTabs()) {

            if (t.equals(tab)) {
                continue;
            }

            // models with similar name
            if (t.getText() != null && t.getText().contains(modelName)) {

                index = t.getText().indexOf(modelName);
                prefix = t.getText().substring(index);

                // models have exact same name
                if (prefix.contentEquals(modelName)) {
                    prefix = t.getText().substring(0, index).trim();

                    // generate prefix
                    if (prefix.matches("\\([0-9]+\\)")) {
                        prefix = prefix.replace("(", "").replace(")", "");
                        count = Integer.parseInt(prefix) + 1;
                    } else if (prefix.isEmpty()) {
                        count = 1;
                    }
                }
            }
        }
        if (count == 0) {
            return modelName;
        } else {
            return "(" + count + ") " + modelName;
        }
    }
    
    private void ShowConfirmationDialog(Event event) {

        DataDao dao = dataService.getDao();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        ButtonType buttonSave = new ButtonType("Save");
        ButtonType buttonClose = new ButtonType("Close");
        ButtonType buttonCancel = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close model");
        alert.setHeaderText("Confirm closing model '" + dao.getModelName() + "'");
        alert.setContentText("The model contains unsaved work. Are you sure you want to close the model and discard any changes?");
        alert.getButtonTypes().setAll(buttonSave, buttonClose, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            boolean isSaved = fileMenuController.SaveAs(stage, dao);
            if (!isSaved) {
                event.consume();
            }
        } else if (result.get() == buttonCancel) {
            event.consume();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        buttonCreateTab.setOnAction(el -> CreateTab());
        
        editorTabPane.getTabs().remove(0);
        editorTabPane.getTabs().addListener((Observable ll) -> {
            if (editorTabPane.getTabs().size() == 1) {
                ShowGraph(null, null);
            }
        });

        try {

            FXMLLoader fxmlLoader;
            
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlElement));
            paneElement = fxmlLoader.load();

            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlHierarchy));
            paneHierarchy = fxmlLoader.load();

            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlPanel));
            paneLeft = fxmlLoader.load();

            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlParameter));
            paneParameter = fxmlLoader.load();

            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlZoom));
            paneZoom = fxmlLoader.load();
            
        } catch (IOException ex) {
            System.out.println("Importing pane from FXML failed! " + ex.getMessage());
        }
    }
}
