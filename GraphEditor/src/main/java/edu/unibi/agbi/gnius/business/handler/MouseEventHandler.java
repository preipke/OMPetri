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
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.AssignmentDeniedException;
import edu.unibi.agbi.gnius.core.service.exception.EdgeCreationException;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
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
    @Autowired private DataService dataService;
    
    @Autowired private EditorToolsController editorToolsController;
    @Autowired private EditorDetailsController editorDetailsController;
    
    @Autowired private KeyEventHandler keyEventHandler;
    @Autowired private Calculator calculator;
    
    private boolean isInitialized = false;
    private boolean isPrimaryButtonDown = false;
    
    // TODO bind GUI buttons to these later
    private final BooleanProperty isInArcCreationMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInDraggingMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInNodeCreationMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInSelectionFrameMode = new SimpleBooleanProperty(false); 
    private final BooleanProperty isInFreeMode = new SimpleBooleanProperty(true); 
    
    private MouseEvent mouseEventMovedLatest;
    private MouseEvent mouseEventMovedPrevious;
    private MouseEvent mouseEventPressed;
    
    private Rectangle selectionFrame;
    
    private IGraphArc arcInCreation;
    
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
                    
                    disableMode(isInNodeCreationMode);
                    
                    if (event.getTarget() instanceof IGraphNode) {
                        
                        final IGraphNode node = (IGraphNode)event.getTarget();
                        
                        if (!selectionService.isSelected(node)) {
                            
                            // Clicking not yet selected node
                            
                            if (!event.isControlDown()) {
                                selectionService.clear();
                                selectionService.select(node);
                                selectionService.hightlightRelated(node);
                                editorDetailsController.getDetails(node);
                            }
                        } 
                        
                        if (!isInDraggingMode.get()) {

                            /**
                             * Arc Creation Mode. Creating new arc after a 
                             * certain time if event is not consumed.
                             */
                            
                            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1d));
                            pauseTransition.setOnFinished(e -> {
                                if (!event.isConsumed()) {
                                    selectionService.select(node);
                                    try {
                                        setEditorMode(isInArcCreationMode);
                                        try {
                                            arcInCreation = dataService.connect(node , null);
                                        } catch (EdgeCreationException | AssignmentDeniedException ex) {

                                        }
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
                    
                    if (isInNodeCreationMode.get()) {
                        
                        editorToolsController.CreateNode(event); // Creating node at event location.
                        
                    } else {

                        if (!event.isControlDown()) {
                            selectionService.clear(); // Clearing current selection.
                        }
                        
                        if (event.isShiftDown()) {

                            /**
                             * Selection Frame Mode. Creating the rectangle.
                             */
                            
                            try {
                                setEditorMode(isInSelectionFrameMode);
                            } catch (EditorModeLockException ex) {

                            }
                            
                            Point2D pos = calculator.getCorrectedMousePosition(mouseEventPressed);
                            selectionFrame.setX(pos.getX());
                            selectionFrame.setY(pos.getY());
                            selectionFrame.setWidth(0);
                            selectionFrame.setHeight(0);
                            
                            graphPane.getTopLayer().getChildren().add(selectionFrame);
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
                
                if (isInSelectionFrameMode.get()) {
                    
                    /**
                     * Selection Frame Mode. Resizes the selection rectangle.
                     */
                    
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
                    
                } else if (isInArcCreationMode.get()) {
                    
                    /**
                     * Arc Creation Mode. Binds arc end (target) to mouse pointer.
                     */
                    
                    Point2D correctedMousePos = calculator.getCorrectedMousePosition(event);
                    
                    arcInCreation.endXProperty().set(correctedMousePos.getX());
                    arcInCreation.endYProperty().set(correctedMousePos.getY());
                    
                } else {

                    /**
                     * Dragging Mode. Drags selected node(s).
                     */
                    
                    try {
                        setEditorMode(isInDraggingMode);
                    } catch (EditorModeLockException ex) {

                    }

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
                        // allow dragging edges to connect places?
                    }
                }
            } 
            else if (event.isSecondaryButtonDown()) {
                
                /**
                 * Dragging the entire graph.
                 */

                graphPane.getTopLayer().setTranslateX((mouseEventMovedLatest.getX() - mouseEventMovedPrevious.getX() + graphPane.getTopLayer().translateXProperty().get()));
                graphPane.getTopLayer().setTranslateY((mouseEventMovedLatest.getY() - mouseEventMovedPrevious.getY() + graphPane.getTopLayer().translateYProperty().get()));
            }
            
            mouseEventMovedPrevious = event;

            event.consume();
        });

        graphPane.setOnMouseReleased(( event ) -> {
            
            if (isInSelectionFrameMode.get()) {

                /**
                 * Selection Frame Mode. Selecting nodes using the rectangle.
                 */
                
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
                disableMode(isInSelectionFrameMode);
                
            } else if (isInArcCreationMode.get()) {

                /**
                 * Arc Creation Mode. Binding or deleting arc.
                 */
                
                IGraphNode target;
                
                if (event.getPickResult().getIntersectedNode() instanceof IGraphNode) {
                    target = (IGraphNode) event.getPickResult().getIntersectedNode();
                } else {
                    target = null;
                }
                try {
                    dataService.connect(arcInCreation , target);
                } catch (Exception ex) {

                }
                disableMode(isInArcCreationMode);
                
            } else {
                
                if (isPrimaryButtonDown && !isInDraggingMode.get()) {

                    /**
                     * Selecting nodes by clicking.
                     */
                    
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
            disableMode(isInDraggingMode);
            
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
    private synchronized void setEditorMode(BooleanProperty mode) throws EditorModeLockException {
        if (!mode.get()) {
            if (!isInFreeMode.get()) {
                throw new EditorModeLockException("Changing mode is blocked!");
            }
            isInFreeMode.set(false);
            mode.set(true);
        }
    }
    
    /**
     * Unlock editor mode.
     * @param mode 
     */
    private synchronized void disableMode(BooleanProperty mode) {
        if (mode.get()) {
            mode.set(false);
            isInFreeMode.set(true);
        }
    }
    
    /**
     * 
     */
    public synchronized void UnlockEditorMode() {
        isInArcCreationMode.set(false);
        isInDraggingMode.set(false);
        isInNodeCreationMode.set(false);
        isInSelectionFrameMode.set(false);
        isInFreeMode.set(true);
    }
    
    /**
     * 
     * @throws EditorModeLockException 
     */
    public void setNodeCreationMode() throws EditorModeLockException {
        setEditorMode(isInNodeCreationMode);
    }
}
