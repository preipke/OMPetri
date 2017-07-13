/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor;

import edu.unibi.agbi.gnius.business.controller.editor.graph.ElementController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.ZoomController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.HierarchyController;
import edu.unibi.agbi.gnius.business.controller.editor.graph.ModelController;
import edu.unibi.agbi.gnius.business.controller.menu.FileMenuController;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.business.handler.ScrollEventHandler;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
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
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
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
    @Autowired private SelectionService selectionService;
    
    @Autowired private InspectorController inspectorController;
    @Autowired private ModelController modelController;
    @Autowired private ElementController elementController;
    @Autowired private FileMenuController fileMenuController;
    @Autowired private HierarchyController hierarchyController;
    @Autowired private ZoomController zoomController;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    @Autowired private ScrollEventHandler scrollEventHandler;

    @Value("${css.editor.pane}") private String paneStyleClass;

    @FXML private TabPane editorTabPane;
    @FXML private Button buttonCreateTab;
    
    private final String fxmlInspector = "/fxml/editor/Inspector.fxml";
    private final String fxmlElement = "/fxml/editor/graph/Element.fxml";
    private final String fxmlHierarchy = "/fxml/editor/graph/Hierarchy.fxml";
    private final String fxmlPanel = "/fxml/editor/graph/Panel.fxml";
    private final String fxmlZoom = "/fxml/editor/graph/Zoom.fxml";
    
    private Parent paneInspector;
    private Parent paneElement;
    private Parent paneHierarchy;
    private Parent panePanel;
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
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(scene);
        pane.setPadding(new Insets(2));
        
        Tab tab = new Tab("Model");
        tab.setContent(pane);
        tab.setText(getTabName(tab, dao.getModelName()));
        dao.modelNameProperty().addListener(cl -> {
            tab.setText(getTabName(tab, dao.getModelName())); // todo: check names for all tabs
        });
        tab.selectedProperty().addListener(cl -> {
            if (tab.isSelected()) {
                setPane(pane, dao);
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
    
    public void FocusGraphElement(IGraphElement element) {
        
        ShowGraphEditor();
        
        if (element != null) {
            double posX, posY;
            
            dataService.getDao().setScalePower(0);
            dataService.getGraph().getScale().setX(1);
            dataService.getGraph().getScale().setY(1);
            dataService.getGraph().translateXProperty().set(0);
            dataService.getGraph().translateYProperty().set(0);
            
            if (element instanceof IGraphArc) {
                IGraphNode source = ((IGraphArc) element).getSource();
                IGraphNode target = ((IGraphArc) element).getSource();
                posX = source.translateXProperty().get() + (target.translateXProperty().get() - source.translateXProperty().get()) / 2;
                posY = source.translateYProperty().get() + (target.translateYProperty().get() - source.translateYProperty().get()) / 2;
            } else {
                posX = element.translateXProperty().get();
                posY = element.translateYProperty().get();
            }
            
            dataService.getGraph().translateXProperty().set(dataService.getDao().getGraphPane().getWidth() / 2 - posX);
            dataService.getGraph().translateYProperty().set(dataService.getDao().getGraphPane().getHeight() / 2 - posY);
            
            selectionService.unselectAll();
            selectionService.select(element);
            selectionService.highlightRelated(element);
        }
    }
    
    public void HideElementPane() {
        if (stackPaneActive != null) {
            stackPaneActive.getChildren().remove(paneElement);
        }
    }
    
    public void ShowInspector(IDataElement element) {
        if (element != null) {
            if (element.getElementType() == Element.Type.CLUSTER || element.getElementType() == Element.Type.CLUSTERARC) {
                return;
            }
        }
        if (stackPaneActive != null) {
            stackPaneActive.getChildren().remove(paneHierarchy);
            stackPaneActive.getChildren().remove(panePanel);
            stackPaneActive.getChildren().remove(paneZoom);
            stackPaneActive.getChildren().add(paneInspector);
            inspectorController.setElement(element);
        }
    }
    
    public void ShowGraphEditor() {
        if (stackPaneActive != null) {
            stackPaneActive.getChildren().remove(paneInspector);
            stackPaneActive.getChildren().add(paneHierarchy);
            stackPaneActive.getChildren().add(panePanel);
            stackPaneActive.getChildren().add(paneZoom);
        }
    }
    
    public void ShowElementInfoPane(IGraphElement element) {
        stackPaneActive.getChildren().add(paneElement);
        elementController.ShowElementInfo(element);
    }
    
    public void setPane(StackPane pane, DataDao dao) {
        stackPaneActive = pane;
        if (pane != null && dao != null) {
            dataService.setDao(dao);
            inspectorController.clear();
            modelController.setDao(dao);
            hierarchyController.setDao(dao);
            ShowGraphEditor();
        }
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
            boolean isSaved = fileMenuController.SaveAs(dao);
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
                setPane(null, null);
            }
        });

        try {
            FXMLLoader fxmlLoader;
            
            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlInspector));
            paneInspector = fxmlLoader.load();
            
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
            panePanel = fxmlLoader.load();

            fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(springContext::getBean);
            fxmlLoader.setLocation(getClass().getResource(fxmlZoom));
            paneZoom = fxmlLoader.load();
            
            StackPane.setAlignment(paneElement, Pos.TOP_RIGHT);
            StackPane.setAlignment(paneInspector, Pos.CENTER);
            StackPane.setAlignment(paneHierarchy, Pos.BOTTOM_LEFT);
            StackPane.setAlignment(panePanel, Pos.TOP_LEFT);
            StackPane.setAlignment(paneZoom, Pos.BOTTOM_CENTER);
            
        } catch (IOException ex) {
            System.out.println("Importing graph pane from FXML failed! " + ex.getMessage());
        }
    }
}
