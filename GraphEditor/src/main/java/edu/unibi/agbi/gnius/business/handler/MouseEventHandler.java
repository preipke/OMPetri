/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.business.controller.tab.EditorTabController;
import edu.unibi.agbi.gnius.business.service.SelectionService;
import edu.unibi.agbi.gnius.util.Calculator;

import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.IGravisSelectable;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
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
    @Autowired private Calculator calculator;
    
    private Rectangle selectionRectangle;
    
    private boolean keyEventsRegistered = false;
    
    private boolean isPrimaryButtonDown = false;
    private boolean isDraggingNodes = false;
    private boolean isSelectionRectangleActive = false;
    
    private final BooleanProperty isCreatingNodes = new SimpleBooleanProperty(false); // TODO bind this to according GUI button later
    
    public void setCreatingNodes(boolean value) {
        isCreatingNodes.set(value);
    }
    
    public void setRectangleActive(boolean value) {
        isCreatingNodes.set(!value);
        isSelectionRectangleActive = value;
    }
    
    private MouseEvent mouseEventMovedLatest;
    private MouseEvent mouseEventMovedPrevious;
    private MouseEvent mouseEventPressed;
    
    public MouseEvent getMouseMovedEventLatest() {
        return mouseEventMovedLatest;
    }
    
    public MouseEvent geMouseMovedEventPrevious() {
        return mouseEventMovedPrevious;
    }
    
    public MouseEvent getMousePressedEvent() {
        return mouseEventPressed;
    }
    
    /**
     * Registers several mouse and scroll event handlers.
     * @param graphPane 
     */
    public void registerTo(GraphPane graphPane) {
        
        /**
         * Preparing selection rectangle.
         */
        selectionRectangle = new Rectangle(0 , 0 , 0 , 0);
        selectionRectangle.setStroke(Color.BLUE);
        selectionRectangle.setStrokeWidth(1);
        selectionRectangle.setStrokeLineCap(StrokeLineCap.ROUND);
        selectionRectangle.setFill(Color.LIGHTBLUE.deriveColor(0 , 1.2 , 1 , 0.6));
        
        /**
         * Register key events. Must be done after showing stage (Scene is null
         * before).
         */
        graphPane.setOnMouseMoved((MouseEvent event) -> {
            
            mouseEventMovedLatest = event;
            
            if (!keyEventsRegistered) {
                keyEventHandler.registerTo(graphPane.getScene());
                keyEventsRegistered = true;
            }
        });

        /**
         * Interacting with nodes.
         */
        graphPane.setOnMousePressed(( MouseEvent event ) -> {
            
            mouseEventMovedLatest = event;
            mouseEventMovedPrevious = event; // used for several actions, i.e. dragging
            mouseEventPressed = event;
            
            /**
             * Left clicking.
             */
            if (event.isPrimaryButtonDown()) {
                
                isPrimaryButtonDown = true;

                if (event.getTarget() instanceof IGravisSelectable) {
                    
                    /**
                     * Clicking node objects.
                     */
                    if (event.getTarget() instanceof IGravisNode) {
                        
                        IGravisNode node = (IGravisNode)event.getTarget();
                        
                        if (!selectionService.contains(node)) {
                            if (!event.isControlDown()) {
                                selectionService.clear();
                                selectionService.addAll(node);
                            }
                        } else {
                            // Maybe dragging - wait!
                        }
                        
                    } else if (event.getTarget() instanceof IGravisEdge) {
                        
                        IGravisEdge edge = (IGravisEdge)event.getTarget();
                        
                        if (!event.isControlDown()) {
                            selectionService.clear();
                        }
                        selectionService.add(edge);
                    }
                } else {
                    
                    /**
                     * Clicking the pane.
                     */
                    if (isCreatingNodes.get()) {
                        
                        editorTabController.CreateNode(event); // Create node at event location.
                        
                    } else {

                        if (!event.isControlDown()) {
                            selectionService.clear(); // Clearing current selection.
                        }
                        
                        /**
                         * Selection rectangle.
                         */
                        if (event.isShiftDown()) {
                            
                            Point2D pos = calculator.getCorrectedMousePosition(mouseEventPressed);

                            // TODO
                            // apply translation
                            selectionRectangle.setX(pos.getX());
                            selectionRectangle.setY(pos.getY());
                            selectionRectangle.setWidth(0);
                            selectionRectangle.setHeight(0);
                            
                            setRectangleActive(true);
                            graphPane.getTopLayer().getChildren().add(selectionRectangle);
                        }
                    }
                }
            } else if (event.isSecondaryButtonDown()) {
                // TODO
            }

            event.consume();
        });

        graphPane.setOnMouseDragged(( MouseEvent event ) -> {
            
            mouseEventMovedLatest = event;
            
            if (event.isPrimaryButtonDown()) {
                
                /**
                 * Resizing the selection rectangle.
                 */
                if (isSelectionRectangleActive) {
                    
                    Point2D pos_t0 = calculator.getCorrectedMousePosition(mouseEventPressed);
                    Point2D pos_t1 = calculator.getCorrectedMousePosition(mouseEventMovedLatest);
                    
                    double offsetX = pos_t1.getX() - pos_t0.getX();
                    double offsetY = pos_t1.getY() - pos_t0.getY();

                    if (offsetX > 0) {
                        selectionRectangle.setWidth(offsetX);
                    } else {
                        selectionRectangle.setX(pos_t1.getX());
                        selectionRectangle.setWidth(pos_t0.getX() - selectionRectangle.getX());
                    }

                    if (offsetY > 0) {
                        selectionRectangle.setHeight(offsetY);
                    } else {
                        selectionRectangle.setY(pos_t1.getY());
                        selectionRectangle.setHeight(pos_t0.getY() - selectionRectangle.getY());
                    }
                    
                } else {

                    isDraggingNodes = true;

                    /**
                     * Drag selected node(s).
                     */
                    if (event.getTarget() instanceof IGravisNode) {
                        
                        Point2D pos_t0 = calculator.getCorrectedMousePosition(mouseEventMovedPrevious);
                        Point2D pos_t1 = calculator.getCorrectedMousePosition(mouseEventMovedLatest);
                        
                        for (IGravisNode node : selectionService.getNodes()) {
                            node.setTranslate(
                                    node.getTranslateX() + pos_t1.getX() - pos_t0.getX() ,
                                    node.getTranslateY() + pos_t1.getY() - pos_t0.getY()
                            );
                        }
                    } else if (event.getTarget() instanceof IGravisEdge) {
                        // TODO
                        // allow dragging edges to connect places
                    }
                }
            } 
            /**
             * Drag the toplayer / pane.
             */
            else if (event.isSecondaryButtonDown()) {
                graphPane.getTopLayer().setTranslateX((mouseEventMovedLatest.getX() - mouseEventMovedPrevious.getX() + graphPane.getTopLayer().translateXProperty().get()));
                graphPane.getTopLayer().setTranslateY((mouseEventMovedLatest.getY() - mouseEventMovedPrevious.getY() + graphPane.getTopLayer().translateYProperty().get()));
            }
            
            mouseEventMovedPrevious = event;

            event.consume();
        });

        /**
         * Used to determine relative position for dragging.
         */
        graphPane.setOnMouseReleased(( MouseEvent event ) -> {

            if (isSelectionRectangleActive) {
                
                /**
                 * Selecting node objects using the rectangle.
                 */
                for (Node node : graphPane.getTopLayer().getNodeLayer().getChildren()) {
                    if (node instanceof IGravisNode) {
                        if (node.getBoundsInParent().intersects(selectionRectangle.getBoundsInParent())) {
                            Platform.runLater(() -> {
                                selectionService.addAll((IGravisNode)node);
                            });
                        }
                    }
                }
                graphPane.getTopLayer().getChildren().remove(selectionRectangle);
                isSelectionRectangleActive = false;
                
            } else {
                
                /**
                 * Selecting node objects by clicking.
                 */
                if (isPrimaryButtonDown && !isDraggingNodes) {
                    
                    if (event.getTarget() instanceof IGravisNode) {
                        
                        IGravisNode node = (IGravisNode)event.getTarget();
                        
                        if (event.isControlDown()) {
                            if (!selectionService.remove(node)) {
                                selectionService.add(node);
                            }
                        }
                        
                    } else if (event.getTarget() instanceof IGravisEdge) {
                        
                        IGravisEdge edge = (IGravisEdge)event.getTarget();
                        
                        if (!event.isControlDown()) {
                            selectionService.clear();
                        }
                        selectionService.add(edge);
                    }
                }
            }

            event.consume();
        });
        
        // reihenfolge: pressed -> released -> clicked
        graphPane.setOnMouseClicked((MouseEvent event) -> {
            System.out.println("Clicked!");
            
            mouseEventPressed = null;

            isPrimaryButtonDown = false;
            isDraggingNodes = false;
            
            event.consume();
        });
    }
}
