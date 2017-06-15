/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.editor.model;

import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.service.HierarchyService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gravisfx.graph.Graph;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author PR
 */
@Controller
public class HierarchyController implements Initializable
{
    @Autowired private HierarchyService hierarchyService;
    @Autowired private SelectionService selectionService;
    
    @FXML private TreeView<HierarchyLevel> treeGraphHierarchy;
    
    public HierarchyController() {
    }
    
    public void update() {
        TreeItem<HierarchyLevel> root = getItem(treeGraphHierarchy.getRoot().getValue().getGraph());
        treeGraphHierarchy.setRoot(root);
    }
    
    public void setDao(DataDao dataDao) {
        TreeItem<HierarchyLevel> root = getItem(dataDao.getGraphRoot());
        treeGraphHierarchy.setRoot(root);
    }
    
    private TreeItem<HierarchyLevel> getItem(Graph graph) {
        TreeItem<HierarchyLevel> item = new TreeItem(new HierarchyLevel(graph));
        graph.getChildGraphs().forEach(g -> item.getChildren().add(getItem(g)));
        graph.nameProperty().addListener(cl -> item.setValue(new HierarchyLevel(graph)));
        return item;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        treeGraphHierarchy.setOnMouseClicked((MouseEvent mouseEvent) -> {
            if (mouseEvent.getClickCount() == 2) {
                hierarchyService.show(treeGraphHierarchy.getSelectionModel().getSelectedItem().getValue().getGraph());
            }
        });
        
        treeGraphHierarchy.setOnKeyPressed(eh -> {
            if (eh.getCode() == KeyCode.ENTER) {
                hierarchyService.show(treeGraphHierarchy.getSelectionModel().getSelectedItem().getValue().getGraph());
            }
        });
        
        treeGraphHierarchy.selectionModelProperty().addListener(cl -> {
//            HierarchyLevel item = treeGraphHierarchy.getSelectionModel().getSelectedItem().getValue();
//            if (item != null) {
//                selectionService.select((IGraphElement) item.getValue());
//                mainController.ShowElementDetails((IGraphElement) item.getValue());
//            }
        });
        
        treeGraphHierarchy.focusModelProperty().addListener(cl -> {
//            HierarchyLevel item = treeGraphHierarchy.getFocusModel().getFocusedItem().getValue();
//            if (item != null) {
//                selectionService.hover(null);
//                selectionService.hover((IGraphElement) item.getValue());
//            }
        });
    }
    
    private class HierarchyLevel {
        
        private final Graph graph;
        
        public HierarchyLevel(Graph graph) {
            this.graph = graph;
        }
        
        public Graph getGraph() {
            return graph;
        }
        
        @Override
        public String toString() {
            return graph.getName();
        }
    }
}
