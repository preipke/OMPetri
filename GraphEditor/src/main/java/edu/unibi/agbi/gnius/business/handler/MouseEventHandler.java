/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.business.controller.tab.editor.EditorDetailsController;
import edu.unibi.agbi.gnius.business.controller.tab.editor.EditorToolsController;
import edu.unibi.agbi.gnius.business.mode.exception.EditorModeLockException;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.util.Calculator;

import edu.unibi.agbi.gravisfx.presentation.GraphPane;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

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
    
    @Autowired private EditorToolsController editorToolsController;
    @Autowired private EditorDetailsController editorDetailsController;
    
    @Autowired private KeyEventHandler keyEventHandler;
    @Autowired private Calculator calculator;
    
    private boolean isInitialized = false;
    
    private boolean isPrimaryButtonDown = false;
    private boolean isModeLocked = false;
    
    // TODO bind GUI buttons to these later
    private final BooleanProperty isArcCreationModeActive = new SimpleBooleanProperty(false);
    private final BooleanProperty isDraggingModeActive = new SimpleBooleanProperty(false);
    private final BooleanProperty isNodeCreationModeActive = new SimpleBooleanProperty(false);
    private final BooleanProperty isSelectionFrameActive = new SimpleBooleanProperty(false); 
    
    private MouseEvent mouseEventMovedLatest;
    private MouseEvent mouseEventMovedPrevious;
    private MouseEvent mouseEventPressed;
    
    private Rectangle selectionFrame;
    
    /**
     * Registers several mouse and scroll event handlers.
     * @param graphPane 
     */
    public void registerTo(GraphPane graphPane) {
        
        /**
         * Preparing selection rectangle.
         */
        selectionFrame = new Rectangle(0 , 0 , 0 , 0);
        selectionFrame.setStroke(Color.BLUE);
        selectionFrame.setStrokeWidth(1);
        selectionFrame.setStrokeLineCap(StrokeLineCap.ROUND);
        selectionFrame.setFill(Color.LIGHTBLUE.deriveColor(0 , 1.2 , 1 , 0.6));
        
        /**
         * Register key events. Must be done after showing stage (Scene is null
         * before).
         */
        graphPane.setOnMouseMoved((MouseEvent event) -> {
            
            mouseEventMovedLatest = event;
            
            if (!isInitialized) {
                mouseEventPressed = event;
                keyEventHandler.registerTo(graphPane.getScene());
                isInitialized = true;
            }
        });

        /**
         * Interacting with nodes.
         */
        graphPane.setOnMousePressed(( event ) -> {
            
            System.out.println("Pressed!");
            
            mouseEventMovedLatest = event;
            mouseEventMovedPrevious = event; // used for several actions, i.e. dragging
            mouseEventPressed.consume();
            mouseEventPressed = event;
            
            /**
             * Left clicking.
             */
            if (event.isPrimaryButtonDown()) {
                
                isPrimaryButtonDown = true;

                if (event.getTarget() instanceof IGraphElement) {
                    
                    /**
                     * Clicking graph elements.
                     */
                    
                    MouseEventHandler.this.UnlockEditorMode(isNodeCreationModeActive);
                    
                    if (event.getTarget() instanceof IGraphNode) {
                        
                        final IGraphNode node = (IGraphNode)event.getTarget();
                        
                        if (!selectionService.isSelected(node)) {
                            
                            // Clicked not yet selected node
                            
                            if (!event.isControlDown()) {
                                selectionService.clear();
                                selectionService.select(node);
                                selectionService.hightlightRelated(node);
                                editorDetailsController.getDetails(node);
                            }
                        } 
                        
                        if (!isDraggingModeActive.get()) {
                            
                            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1d));
                            pauseTransition.setOnFinished(e -> {
                                if (!event.isConsumed()) {
                                    selectionService.select(node);
                                    try {
                                        LockEditorMode(isArcCreationModeActive);
                                        UnlockEditorMode(isArcCreationModeActive);
                                    } catch (EditorModeLockException ex) {

                                    }
                                    System.out.println("Creating ARC!");
                                }
                            });
                            pauseTransition.playFromStart();
                        }
                        
                    } else {
                        
                        IGraphArc arc = (IGraphArc)event.getTarget();
                        
                        if (!event.isControlDown()) {
                            selectionService.clear();
                            editorDetailsController.getDetails(arc);
                        }
                        selectionService.select(arc);
                    }
                    
                } else {
                    
                    /**
                     * Clicking the pane.
                     */
                    
                    editorDetailsController.hide();
                    
                    if (isNodeCreationModeActive.get()) {
                        
                        editorToolsController.CreateNode(event); // Create node at event location.
                        
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
                            selectionFrame.setX(pos.getX());
                            selectionFrame.setY(pos.getY());
                            selectionFrame.setWidth(0);
                            selectionFrame.setHeight(0);
                            
                            graphPane.getTopLayer().getChildren().add(selectionFrame);
                            try {
                                LockEditorMode(isSelectionFrameActive);
                            } catch (EditorModeLockException ex) {

                            }
                        }
                    }
                }
            } else if (event.isSecondaryButtonDown()) {
                // TODO
            }
        });

        graphPane.setOnMouseDragged(( event ) -> {
            
            mouseEventPressed.consume(); // for PauseTransition
            
            mouseEventMovedLatest = event;
            
            if (event.isPrimaryButtonDown()) {
                
                /**
                 * Selection Mode. Resizing the selection rectangle.
                 */
                if (isSelectionFrameActive.get()) {
                    
                    Point2D pos_t0 = calculator.getCorrectedMousePosition(mouseEventPressed);
                    Point2D pos_t1 = calculator.getCorrectedMousePosition(mouseEventMovedLatest);
                    
                    double offsetX = pos_t1.getX() - pos_t0.getX();
                    double offsetY = pos_t1.getY() - pos_t0.getY();

                    if (offsetX > 0) {
                        selectionFrame.setWidth(offsetX);
                    } else {
                        selectionFrame.setX(pos_t1.getX());
                        selectionFrame.setWidth(pos_t0.getX() - selectionFrame.getX());
                    }

                    if (offsetY > 0) {
                        selectionFrame.setHeight(offsetY);
                    } else {
                        selectionFrame.setY(pos_t1.getY());
                        selectionFrame.setHeight(pos_t0.getY() - selectionFrame.getY());
                    }
                    
                } else {

                    /**
                     * Dragging Mode. Drag selected node(s).
                     */
                    try {
                        LockEditorMode(isDraggingModeActive);

                        if (event.getTarget() instanceof IGraphNode) {

                            Point2D pos_t0 = calculator.getCorrectedMousePosition(mouseEventMovedPrevious);
                            Point2D pos_t1 = calculator.getCorrectedMousePosition(mouseEventMovedLatest);

                            for (IGraphNode node : selectionService.getSelectedNodes()) {
                                node.setTranslate(
                                        node.getTranslateX() + pos_t1.getX() - pos_t0.getX() ,
                                        node.getTranslateY() + pos_t1.getY() - pos_t0.getY()
                                );
                            }
                            
                        } else if (event.getTarget() instanceof IGraphArc) {
                            // TODO
                            // allow dragging edges to connect places
                        }
                    } catch (EditorModeLockException ex) {

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

        graphPane.setOnMouseReleased(( event ) -> {
            
            /**
             * Selection Frame Mode. Selecting node objects using the rectangle.
             */
            if (isSelectionFrameActive.get()) {
                for (Node node : graphPane.getTopLayer().getNodeLayer().getChildren()) {
                    if (node instanceof IGraphNode) {
                        if (node.getBoundsInParent().intersects(selectionFrame.getBoundsInParent())) {
                            Platform.runLater(() -> {
                                selectionService.select((IGraphNode)node);
                            });
                        }
                    }
                }
                graphPane.getTopLayer().getChildren().remove(selectionFrame);
                MouseEventHandler.this.UnlockEditorMode(isSelectionFrameActive);
                
            } else {
                
                /**
                 * Selecting node objects by clicking.
                 */
                if (isPrimaryButtonDown && !isDraggingModeActive.get()) {
                    
                    if (event.getTarget() instanceof IGraphNode) {
                        
                        IGraphNode node = (IGraphNode)event.getTarget();
                        
                        if (event.isControlDown()) {
                            if (!selectionService.remove(node)) {
                                selectionService.select(node);
                            }
                            editorDetailsController.hide();
                        } else {
                            selectionService.clear();
                            selectionService.select(node);
                            selectionService.hightlightRelated(node);
                            editorDetailsController.getDetails(node);
                        }
                        
                    } else if (event.getTarget() instanceof IGraphArc) {
                        
                        IGraphArc edge = (IGraphArc)event.getTarget();
                        
                        if (event.isControlDown()) {
                            editorDetailsController.hide();
                        } else {
                            editorDetailsController.getDetails(edge);
                        }
                        selectionService.select(edge);
                    }
                }
            }

            event.consume();
        });
        
        // reihenfolge: pressed -> released & clicked
        graphPane.setOnMouseClicked(( event ) -> {
            
            System.out.println("Clicked!");
            mouseEventPressed.consume();
            isPrimaryButtonDown = false;
            UnlockEditorMode(isDraggingModeActive);
            
            event.consume();
        });
    }
    
    public MouseEvent getMouseMovedEventLatest() {
        return mouseEventMovedLatest;
    }
    
    public MouseEvent geMouseMovedEventPrevious() {
        return mouseEventMovedPrevious;
    }
    
    public MouseEvent getMousePressedEvent() {
        return mouseEventPressed;
    }
    
    private void DragArc(MouseEvent event) {
        
        event.getTarget();
        
    }
    
    /**
     * Lock editor mode. Prevents more than one mode to be active at any time.
     * @param mode
     * @throws EditorModeLockException 
     */
    private synchronized void LockEditorMode(BooleanProperty mode) throws EditorModeLockException {
        if (!mode.get()) {
            if (isModeLocked) {
                throw new EditorModeLockException("Changing mode is locked!");
            }
            isModeLocked = true;
            mode.set(isModeLocked);
        }
    }
    
    /**
     * Unlock editor mode.
     * @param mode 
     */
    private synchronized void UnlockEditorMode(BooleanProperty mode) {
        if (mode.get()) {
            mode.set(false);
            isModeLocked = false;
        }
    }
    
    /**
     * 
     */
    public void UnlockEditorMode() {
        isArcCreationModeActive.set(false);
        isDraggingModeActive.set(false);
        isNodeCreationModeActive.set(false);
        isSelectionFrameActive.set(false);
        synchronized (this) {
            isModeLocked = false;
        }
    }
    
    /**
     * 
     * @throws EditorModeLockException 
     */
    public void setNodeCreationMode() throws EditorModeLockException {
        LockEditorMode(isNodeCreationModeActive);
    }
}
