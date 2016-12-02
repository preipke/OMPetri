/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.handler;

import edu.unibi.agbi.gnius.controller.tab.presentation.PresentationPaneController;
import edu.unibi.agbi.gnius.exception.data.NodeCreationException;
import edu.unibi.agbi.gnius.model.SelectionModel;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author PR
 */
public class KeyStrokeHandler
{
    private static GraphPane pane;
    public static void setGraphPane(GraphPane pane) {
        KeyStrokeHandler.pane = pane;
    }
    
    private static SelectionModel selectionModel;
    private static DoubleProperty eventLatestMousePosX;
    private static DoubleProperty eventLatestMousePosY;
    
    public static void setSelectionModel(SelectionModel model) {
        selectionModel = model;
    }
    public static void setLatestMousePosX(DoubleProperty posX) {
        eventLatestMousePosX = posX;
    }
    public static void setLatestMousePosY(DoubleProperty posY) {
        eventLatestMousePosY = posY;
    }
    
    private static IGravisNode[] selectedNodesCopy = new IGravisNode[0];
    private static IGravisEdge[] selectedEdgesCopy = new IGravisEdge[0];
    
    public static void register() {
        
        /**
         * Node copying and deleting.
         */
        pane.getScene().setOnKeyPressed((KeyEvent event) -> {
            
            /**
             * Delete selected nodes.
             */
            if (event.getCode().equals(KeyCode.DELETE)) {
                for (IGravisEdge edge : selectionModel.getSelectedEdges()) {
                    PresentationPaneController.remove(edge);
                }
                for (IGravisNode node : selectionModel.getSelectedNodes()) {
                    PresentationPaneController.remove(node);
                }
            }
            /**
             * Copying selected nodes.
             */
            else if (event.isControlDown()) {
                
                if (event.getCode().equals(KeyCode.C)) {
                    
                    selectedNodesCopy = selectionModel.getSelectedNodesArray();
                    selectedEdgesCopy = selectionModel.getSelectedEdgesArray();
                    
                } else if (event.getCode().equals(KeyCode.V)) {
                    
                    List<IGravisNode> nodes;
                    try {
                        nodes = PresentationPaneController.copy(selectedNodesCopy , eventLatestMousePosX.get(), eventLatestMousePosY.get());
                        if (true) { // copying, create new pn object
                            // TODO
                            Platform.runLater(() -> {
                                selectionModel.clear();
                            });
                        } else { // cloning, reference the same pn object
                            // TODO
                        }
                        Platform.runLater(() -> {
                            selectionModel.add(nodes);
                        });
                    } catch (NodeCreationException ex) {
                        System.out.println(ex.toString());
                    }
                }
            }

            event.consume();
        });
    }
}
