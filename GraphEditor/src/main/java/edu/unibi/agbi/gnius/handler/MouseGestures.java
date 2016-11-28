/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.handler;

import edu.unibi.agbi.gnius.controller.fxml.GraphMenuController;
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
import javafx.scene.input.ScrollEvent;

/**
 *
 * @author PR
 */
public class MouseGestures
{
    private static Double eventMousePressedX = null;
    private static Double eventMousePressedY = null;
    
    private static List<IGravisSelectable> selectedNodes = new ArrayList();
    private static IGravisSelectable[] selectedNodesCopy;
    
    private static final BooleanProperty isCreatingNodes = new SimpleBooleanProperty(false);
    private static final BooleanProperty isDraggingEnabled = new SimpleBooleanProperty(true);
    
    private static final BooleanProperty isHoldingControl = new SimpleBooleanProperty(false);
    
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

        graphPane.setOnMousePressed(( MouseEvent event ) -> {
            
            eventMousePressedX = event.getX();
            eventMousePressedY = event.getY();
            
            if (event.isPrimaryButtonDown()) {

                /**
                 * Clicking node objects.
                 */
                if (IGravisSelectable.class.isAssignableFrom(event.getTarget().getClass())) {

                    IGravisSelectable selectable = (IGravisSelectable) event.getTarget();

                    // select multiple nodes
                    if (event.isControlDown()) {
                        
                        selectedNodes.add(selectable);
                        selectable.setHighlight(true);

                    } else {
                        
                        for (IGravisSelectable selected : selectedNodes) {
                            selected.setHighlight(false);
                        }
                        selectedNodes = new ArrayList();
                        selectedNodes.add(selectable);
                        
                        selectable.setHighlight(true);
                    }
                } 
                /**
                 * Clicking the graph pane.
                 */
                else if (!IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {

                    if (!event.isControlDown()) {
                        for (IGravisSelectable selected : selectedNodes) {
                            selected.setHighlight(false);
                        }
                        selectedNodes = new ArrayList();
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
        
        graphPane.setOnKeyPressed((KeyEvent event) -> {
            
            /**
             * Delete selected nodes.
             */
            if (event.getCode().equals(KeyCode.DELETE)) {
                
                for (int i = 0; i < selectedNodes.size(); i++) {
                    
                    // deleting
                    
                }

            }
            /**
             * Copying nodes.
             */
            else if (event.isControlDown()) {
                
                if (event.getCode().equals(KeyCode.C)) {
                
                    selectedNodesCopy = (IGravisSelectable[]) selectedNodes.toArray();
                    
                } else if (event.getCode().equals(KeyCode.V)) {
                    
                    for (int i = 0; i < selectedNodesCopy.length; i++) {
                        
                        // copying
                        
                    }
                    
                }
            }
            
        });
        
        

        graphPane.setOnMouseReleased(( MouseEvent event ) -> {
            eventMousePressedX = null;
            eventMousePressedY = null;
        });

        graphPane.setOnMouseDragged(( MouseEvent event ) -> {
            
            if (event.isPrimaryButtonDown()) {
                
                // TODO
                // take mouse pointer position relative to object center*scale into account (looks more smooth)
                if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {

                    IGravisNode node = (IGravisNode)event.getTarget();
                    node.setTranslate(
                            (event.getX() - graphPane.getTopLayer().translateXProperty().get()) / graphPane.getTopLayer().getScaleTransform().getX() ,
                            (event.getY() - graphPane.getTopLayer().translateYProperty().get()) / graphPane.getTopLayer().getScaleTransform().getX()
                    );
                }
                
            } else if (event.isSecondaryButtonDown()) {

                // TODO
                // properly activate dragging
                
                //if (GraphPane.class.isAssignableFrom(event.getTarget().getClass())) {

                    graphPane.getTopLayer().setTranslateX((event.getX() - eventMousePressedX + graphPane.getTopLayer().translateXProperty().get()));
                    graphPane.getTopLayer().setTranslateY((event.getY() - eventMousePressedY + graphPane.getTopLayer().translateYProperty().get()));

                    eventMousePressedX = event.getX();
                    eventMousePressedY = event.getY();
                //}

            }
        });
    }
}
