/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.handler;

import edu.unibi.agbi.gnius.controller.tab.EditorTabController;
import edu.unibi.agbi.gnius.service.SelectionService;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import javafx.application.Platform;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class MouseEventHandler
{
    @Autowired private SelectionService selectionService;
    @Autowired private EditorTabController editorTabController;
    @Autowired private KeyEventHandler keyEventHandler;
    
    private boolean keyEventsRegistered = false; 
    
    private MouseEvent movedMouseEventLatest;
    public MouseEvent getLatestMovedMouseEvent() {
        return movedMouseEventLatest;
    }
    
    private Double eventMousePressedStartPosX = null;
    private Double eventMousePressedStartPosY = null;
    
    private Double eventPreviousMousePosX = null;
    private Double eventPreviousMousePosY = null;
    
    private final BooleanProperty isCreatingNodes = new SimpleBooleanProperty(false);
    private final BooleanProperty isDraggingEnabled = new SimpleBooleanProperty(true);
    private final BooleanProperty isRectangleActive = new SimpleBooleanProperty(false);
    
    public void setDraggingEnabled(boolean value) {
        isCreatingNodes.set(!value);
        isRectangleActive.set(!value);
        
        isDraggingEnabled.set(value);
    }
    
    public void setCreatingNodes(boolean value) {
        isDraggingEnabled.set(!value);
        isRectangleActive.set(!value);
        
        isCreatingNodes.set(value);
    }
    
    public void setRectangleActive(boolean value) {
        isDraggingEnabled.set(!value);
        isCreatingNodes.set(!value);
        
        isRectangleActive.set(value);
    }
    
    private Rectangle rect;
    
    /**
     * Registers several mouse and scroll event handlers.
     * @param graphPane 
     */
    public void registerTo(GraphPane graphPane) {
        
        /**
         * Preparing selection rectangle.
         */
        rect = new Rectangle(0 , 0 , 0 , 0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0 , 1.2 , 1 , 0.6));
        
        /**
         * Used for multiple actions.
         */
        graphPane.setOnMouseMoved((MouseEvent event) -> {
            movedMouseEventLatest = event;
            if (!keyEventsRegistered) {
                keyEventHandler.register();
                keyEventsRegistered = true;
            }
        });
        
        graphPane.setOnMouseClicked((MouseEvent event) -> {
            System.out.println("Clicked!");
        });

        /**
         * Interacting with nodes.
         */
        graphPane.setOnMousePressed(( MouseEvent event ) -> {
            
            eventMousePressedStartPosX = event.getX();
            eventMousePressedStartPosY = event.getY();
            
            // used for toplayer dragging
            eventPreviousMousePosX = event.getX();
            eventPreviousMousePosY = event.getY();
            
            /**
             * Left clicking.
             */
            if (event.isPrimaryButtonDown()) {

                /**
                 * Clicking node objects.
                 */
                if (IGravisSelectable.class.isAssignableFrom(event.getTarget().getClass())) {
                    
                    if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {
                        IGravisNode node = (IGravisNode) event.getTarget();
                        if (!event.isControlDown()) {
                                selectionService.clear();
                        } 
                        selectionService.add(node);
                    } else 
                    if (IGravisEdge.class.isAssignableFrom(event.getTarget().getClass())) {
                        IGravisEdge edge = (IGravisEdge) event.getTarget();
                        if (!event.isControlDown()) {
                                selectionService.clear();
                        }
                        selectionService.add(edge);
                    }
                } 
                /**
                 * Clicking the pane.
                 */
                else if (!IGravisSelectable.class.isAssignableFrom(event.getTarget().getClass())) {

                    if (isCreatingNodes.get()) {

                        // TODO
                        // pop type menu on holding ctrl?
                        editorTabController.CreateNode(event);
                        
                    } else {

                        /**
                         * Clearing current selection.
                         */
                        if (!event.isControlDown()) {
                            selectionService.clear();
                        }
                        
                        /**
                         * Selection rectangle.
                         */
                        // TODO
                        // use min offset to activate rectangle
                        if (event.isShiftDown()) {

                            // TODO
                            // apply translation and scaling
                            rect.setX(eventMousePressedStartPosX);
                            rect.setY(eventMousePressedStartPosY);
                            rect.setWidth(0);
                            rect.setHeight(0);
                            
                            setRectangleActive(true);
                            graphPane.getTopLayer().getChildren().add(rect);
                        }
                    }
                }

            } else if (event.isSecondaryButtonDown()) {
                // TODO
            }

            event.consume();
        });

        graphPane.setOnMouseDragged(( MouseEvent event ) -> {
            
            if (event.isPrimaryButtonDown()) {
                
                /**
                 * Resizing the selection rectangle.
                 */
                if (isRectangleActive.get()) {
                    // TODO
                    // apply translation!
                    
                    double offsetX = event.getX() - eventMousePressedStartPosX;
                    double offsetY = event.getY() - eventMousePressedStartPosY;

                    if (offsetX > 0) {
                        rect.setWidth(offsetX);
                    } else {
                        rect.setX(event.getX());
                        rect.setWidth(movedMouseEventLatest.getX()- rect.getX());
                    }

                    if (offsetY > 0) {
                        rect.setHeight(offsetY);
                    } else {
                        rect.setY(event.getY());
                        rect.setHeight(movedMouseEventLatest.getY() - rect.getY());
                    }
                    
                } else {

                    /**
                     * Drag selected node(s).
                     */
                    if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {

                        double offsetX = event.getX() - movedMouseEventLatest.getX();
                        double offsetY = event.getY() - movedMouseEventLatest.getY();

                        double transformX;
                        double transformY;
                        
                        IGravisNode node = (IGravisNode) event.getTarget();
                        
                        // TODO
                        // translate relative to mouse pointer and point cloud center position
                        //for (IGravisNode node : selectedNodes) {

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
                                    (event.getX() - graphPane.getTopLayer().translateXProperty().get()) / graphPane.getTopLayer().getScale().getX() ,
                                    (event.getY() - graphPane.getTopLayer().translateYProperty().get()) / graphPane.getTopLayer().getScale().getX()
                            );
                        //}
                    } else if (IGravisEdge.class.isAssignableFrom(event.getTarget().getClass())) {

                        /**
                         * TODO
                         * Apply dragging edges to connect places
                         */
                        
                    } else {

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
                    graphPane.getTopLayer().setTranslateX((event.getX() - eventPreviousMousePosX + graphPane.getTopLayer().translateXProperty().get()));
                    graphPane.getTopLayer().setTranslateY((event.getY() - eventPreviousMousePosY + graphPane.getTopLayer().translateYProperty().get()));
                //}
                eventPreviousMousePosX = event.getX();
                eventPreviousMousePosY = event.getY();
            }

            event.consume();
        });

        /**
         * Used to determine relative position for dragging.
         */
        graphPane.setOnMouseReleased(( MouseEvent event ) -> {
            
            eventMousePressedStartPosX = null;
            eventMousePressedStartPosY = null;

            if (isRectangleActive.get()) {
                
                if (!event.isShiftDown() && !event.isControlDown()) {
                    selectionService.clear();
                }

                for (Node node : graphPane.getTopLayer().getNodeLayer().getChildren()) {

                    if (node instanceof IGravisNode) {

                        if (node.getBoundsInParent().intersects(rect.getBoundsInParent())) {

                            Platform.runLater(() -> {
                                if (event.isControlDown()) {
                                    if (!selectionService.remove((IGravisNode)node)) {
                                        selectionService.add((IGravisNode)node);
                                    }
                                }
                                selectionService.add((IGravisNode)node);
                            });
                        }
                    }
                }
                graphPane.getTopLayer().getChildren().remove(rect);
                isRectangleActive.set(false);
            }

            event.consume();
        });
    }
}
