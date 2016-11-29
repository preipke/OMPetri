/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.handler;

import edu.unibi.agbi.gnius.controller.fxml.GraphMenuController;
import edu.unibi.agbi.gnius.controller.fxml.PresentationController;
import edu.unibi.agbi.gnius.exception.data.NodeCreationException;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author PR
 */
public class MouseGestures
{
    private static Double eventMousePressedX = null;
    private static Double eventMousePressedY = null;
    
    private static MouseEvent eventLatestMousePos;
    
    private static List<IGravisNode> selectedNodes = new ArrayList();
    private static List<IGravisEdge> selectedEdges = new ArrayList();
    
    private static IGravisNode[] selectedNodesCopy = new IGravisNode[0];
    private static IGravisEdge[] selectedEdgesCopy = new IGravisEdge[0];
    
    private static final BooleanProperty isCreatingNodes = new SimpleBooleanProperty(false);
    private static final BooleanProperty isDraggingEnabled = new SimpleBooleanProperty(true);
    
    public static GraphMenuController controller;
    
    public static void setDraggingEnabled(boolean value) {
        isCreatingNodes.set(!value);
        
        isDraggingEnabled.set(value);
    }
    
    public static void setCreatingNodes(boolean value) {
        isDraggingEnabled.set(!value);
        
        isCreatingNodes.set(value);
    }
    
    /**
     * Registers several mouse and scroll event handlers.
     * @param graphPane 
     */
    public static void registerTo(GraphPane graphPane) {
        
        /**
         * Used to determine the paste position for copying / cloning.
         */
        graphPane.setOnMouseMoved((MouseEvent event) -> {
            eventLatestMousePos = event;
        });

        /**
         * Used to determine relative position for dragging.
         */
        graphPane.setOnMouseReleased(( MouseEvent event ) -> {
            eventMousePressedX = null;
            eventMousePressedY = null;
        });

        /**
         * Interacting with nodes.
         */
        graphPane.setOnMousePressed(( MouseEvent event ) -> {
            
            eventMousePressedX = event.getX();
            eventMousePressedY = event.getY();
            
            if (event.isPrimaryButtonDown()) {

                /**
                 * Clicking node objects.
                 */
                if (IGravisSelectable.class.isAssignableFrom(event.getTarget().getClass())) {
                    
                    /**
                     * Way #1
                     */
                    if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {
                        
                        IGravisNode selectableNode = (IGravisNode) event.getTarget();
                        
                        // select multiple nodes
                        if (event.isControlDown()) {
                            selectedNodes.add(selectableNode);
                        } else {
                            for (IGravisNode selected : selectedNodes) {
                                selected.setHighlight(false);
                            }
                            for (IGravisEdge selected : selectedEdges) {
                                selected.setHighlight(false);
                            }
                            selectedNodes = new ArrayList();
                            selectedNodes.add(selectableNode);
                        }
                        selectableNode.setHighlight(true);
                        selectableNode.putOnTop();
                        
                    } else if (IGravisEdge.class.isAssignableFrom(event.getTarget().getClass())) {
                        
                        IGravisEdge selectableEdge = (IGravisEdge) event.getTarget();
                        
                        // select multiple nodes
                        if (event.isControlDown()) {
                            selectedEdges.add(selectableEdge);
                        } else {
                            for (IGravisNode selected : selectedNodes) {
                                selected.setHighlight(false);
                            }
                            for (IGravisEdge selected : selectedEdges) {
                                selected.setHighlight(false);
                            }
                            selectedEdges = new ArrayList();
                            selectedEdges.add(selectableEdge);
                        }
                        selectableEdge.setHighlight(true);
                        selectableEdge.putOnTop();
                    }
                } 
                /**
                 * Clicking the graph pane.
                 */
                else if (!IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {

                    if (!event.isControlDown()) {
                        for (IGravisNode node : selectedNodes) {
                            node.setHighlight(false);
                        }
                        selectedNodes = new ArrayList();
                        for (IGravisEdge edge : selectedEdges) {
                            edge.setHighlight(false);
                        }
                        selectedEdges = new ArrayList();
                    }

                    if (isCreatingNodes.get()) {

                        // TODO
                        // pop type menu on holding ctrl?
                        controller.createNode(event);
                    }

                }

            } else if (event.isSecondaryButtonDown()) {
                // TODO
            }
        });
        
        /**
         * Node copying and deleting.
         */
        graphPane.getScene().setOnKeyPressed((KeyEvent event) -> {
            
            /**
             * Delete selected nodes.
             */
            if (event.getCode().equals(KeyCode.DELETE)) {
                for (IGravisEdge edge : selectedEdges) {
                    PresentationController.remove(edge);
                }
                for (IGravisNode node : selectedNodes) {
                    PresentationController.remove(node);
                }
            }
            /**
             * Copying selected nodes.
             */
            else if (event.isControlDown()) {
                
                if (event.getCode().equals(KeyCode.C)) {
                    
                    selectedNodesCopy = new IGravisNode[selectedNodes.size()];
                    for (int i = 0; i < selectedEdgesCopy.length; i++) {
                        selectedNodesCopy[i] = selectedNodes.get(i);
                    }
                    selectedEdgesCopy = new IGravisEdge[selectedEdges.size()];
                    for (int i = 0; i < selectedEdgesCopy.length; i++) {
                        selectedEdgesCopy[i] = selectedEdges.get(i);
                    }
                    
                } else if (event.getCode().equals(KeyCode.V)) {
                    
                    List<IGravisNode> nodes;
                    
                    try {
                        nodes = PresentationController.copy(selectedNodesCopy , eventLatestMousePos);
                        if (true) { // copying, create new pn object
                            // TODO
                        } else { // cloning, reference the same pn object
                            // TODO
                        }
                        
                        for (IGravisNode node : nodes) {
                            selectedNodes.add(node);
                            node.setHighlight(true);
                        }
                    } catch (NodeCreationException ex) {
                        System.out.println(ex.toString());
                    }
                }
            }
        });

        graphPane.setOnMouseDragged(( MouseEvent event ) -> {
            
            /**
             * Drag selected nodes.
             */
            if (event.isPrimaryButtonDown()) {
                
                if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {

                    double offsetX = event.getX() - eventMousePressedX;
                    double offsetY = event.getY() - eventMousePressedY;
                    
                    double transformX;
                    double transformY;
                    
                    for (IGravisNode node : selectedNodes) {
                        
                        /*
                        transformX = node.getShape().getTranslateX();
                        transformY = node.getShape().getTranslateY();
                        
                        transformX = transformX - graphPane.getTopLayer().translateXProperty().get();
                        transformY = transformY - graphPane.getTopLayer().translateYProperty().get();
                        
                        node.setTranslate(
                                transformX / graphPane.getTopLayer().getScaleTransform().getX() , 
                                transformY / graphPane.getTopLayer().getScaleTransform().getX()
                        );
                        */
                        
                        node.setTranslate(
                                (event.getX() - graphPane.getTopLayer().translateXProperty().get()) / graphPane.getTopLayer().getScaleTransform().getX() ,
                                (event.getY() - graphPane.getTopLayer().translateYProperty().get()) / graphPane.getTopLayer().getScaleTransform().getX()
                        );
                    }
                }
            } 
            /**
             * Drag all nodes.
             */
            else if (event.isSecondaryButtonDown()) {

                // TODO
                // somehow activate dragging, not passively active
                //if (GraphPane.class.isAssignableFrom(event.getTarget().getClass())) {

                    graphPane.getTopLayer().setTranslateX((event.getX() - eventMousePressedX + graphPane.getTopLayer().translateXProperty().get()));
                    graphPane.getTopLayer().setTranslateY((event.getY() - eventMousePressedY + graphPane.getTopLayer().translateYProperty().get()));

                    eventMousePressedX = event.getX();
                    eventMousePressedY = event.getY();
                //}
            }

            eventMousePressedX = event.getX();
            eventMousePressedY = event.getY();
        });
    }
}
