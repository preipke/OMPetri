/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.controller.editor.graph;

import edu.unibi.agbi.editor.core.data.dao.ModelDao;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.HierarchyService;
import edu.unibi.agbi.gravisfx.graph.Graph;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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
    @Autowired private ModelService dataService;
    @Autowired private HierarchyService hierarchyService;

    @FXML private TitledPane hierarchyPane;
    @FXML private TreeView<HierarchyLevel> treeGraphHierarchy;

    public void setDao(ModelDao dataDao) {
        treeGraphHierarchy.setRoot(new TreeItem(new HierarchyLevel(dataDao.getGraphRoot())));
        update();
    }

    public void update() {
        treeGraphHierarchy.getRoot().getChildren().clear();
        getItems(treeGraphHierarchy.getRoot());
        if (treeGraphHierarchy.getRoot().getChildren().size() > 0) {
            hierarchyPane.setExpanded(true);
        } else {
            hierarchyPane.setExpanded(false);
        }
    }

    /**
     *
     * @param item
     * @return indicates wether one of this item's child nodes is currently
     *         selected
     */
    private boolean getItems(TreeItem<HierarchyLevel> item) {
        Graph graph = item.getValue().getGraph();
        graph.nameProperty().addListener(cl -> item.setValue(new HierarchyLevel(graph)));
        graph.getChildGraphs().forEach(childGraph -> {
            TreeItem<HierarchyLevel> childItem = new TreeItem(new HierarchyLevel(childGraph));
            item.getChildren().add(childItem);
            getItems(childItem);
        });
        item.setExpanded(true);
        if (dataService.getGraph().equals(graph)) {
            treeGraphHierarchy.getSelectionModel().select(item);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        treeGraphHierarchy.setEventDispatcher(new TreeMouseEventDispatcher(treeGraphHierarchy.getEventDispatcher()));
        treeGraphHierarchy.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                hierarchyService.show(treeGraphHierarchy.getSelectionModel().getSelectedItem().getValue().getGraph());
            }
        });
    }

    private class HierarchyLevel
    {
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

    private class TreeMouseEventDispatcher implements EventDispatcher
    {
        private final EventDispatcher dispatcher;

        private TreeMouseEventDispatcher(EventDispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public Event dispatchEvent(Event event, EventDispatchChain edc) {
            if (!event.isConsumed()) {
                if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                    MouseEvent mouseEvent = (MouseEvent) event;
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                        hierarchyService.show(treeGraphHierarchy.getSelectionModel().getSelectedItem().getValue().getGraph());
                        event.consume();
                    }
                }
            }
            return dispatcher.dispatchEvent(event, edc);
        }
    }
}
