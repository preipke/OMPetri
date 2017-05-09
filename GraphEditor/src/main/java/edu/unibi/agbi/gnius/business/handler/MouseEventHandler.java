/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.business.controller.ElementController;
import edu.unibi.agbi.gnius.business.controller.ToolsController;
import edu.unibi.agbi.gnius.business.controller.MainController;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphEdge;
import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.IGravisElement;
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
import edu.unibi.agbi.gravisfx.entity.IGravisChildElement;

/**
 *
 * @author PR
 */
@Component
public class MouseEventHandler {

    @Autowired private SelectionService selectionService;
    @Autowired private DataGraphService dataService;
    @Autowired private MessengerService messengerService;

    @Autowired private MainController mainController;
    @Autowired private ElementController elementController;
    @Autowired private ToolsController editorToolsController;

    @Autowired private Calculator calculator;

    private boolean isInitialized = false;
    private boolean isPrimaryButtonDown = false;
    private boolean isSecondaryButtonDown = false;

    private final double secondsBeforeCreatingArc = .35d; // TODO assign custom value in preferences

    private final Rectangle selectionFrame;

    // TODO bind GUI buttons to these later
    private final BooleanProperty isInArcCreationMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInDraggingMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInNodeCreationMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInSelectionFrameMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInFreeMode = new SimpleBooleanProperty(true);

    private MouseEvent mouseEventMovedLatest;
    private MouseEvent mouseEventMovedPrevious;
    private MouseEvent mouseEventPressed;

    private GraphEdge arcTemp;
    
    public MouseEventHandler() {
        selectionFrame = new Rectangle(0, 0, 0, 0);
        selectionFrame.setStroke(Color.BLUE);
        selectionFrame.setStrokeWidth(1);
        selectionFrame.setStrokeLineCap(StrokeLineCap.ROUND);
        selectionFrame.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));
    }

    /**
     * Registers several mouse and scroll event handlers.
     *
     * @param graphPane
     */
    public void registerTo(GraphPane graphPane) {
        graphPane.setOnMouseMoved(e -> onMouseMoved(e, graphPane));
        graphPane.setOnMouseDragged(e -> onMouseDragged(e, graphPane));
        graphPane.setOnMousePressed(e -> onMousePressed(e, graphPane));
        graphPane.setOnMouseReleased(e -> onMouseReleased(e, graphPane));
        graphPane.setOnMouseClicked(e -> onMouseClicked(e));
        // reihenfolge: pressed -> released -> clicked
    }

    /**
     * Sets the on mouse move event actions.
     * Also registers key events. Must be done after showing stage (Scene is
     * null before).
     */
    private void onMouseMoved(MouseEvent event, GraphPane pane) {

        mouseEventMovedLatest = event;

        if (!isInitialized) {
            mouseEventPressed = event; // to avoid null-pointer on dragged
            isInitialized = true;
        }

        if (!isPrimaryButtonDown) {
            if (event.getTarget() instanceof IGravisElement) {
                selectionService.hover((IGravisElement) event.getTarget());
            } else {
                selectionService.hover(null);
            }
        }
    }

    private void onMouseDragged(MouseEvent event, GraphPane pane) {

        mouseEventPressed.consume(); // for PauseTransition
        mouseEventMovedLatest = event;

        if (event.isPrimaryButtonDown()) {

            if (isInSelectionFrameMode.get()) {

                /**
                 * Selection Frame Mode. Resizes the selection rectangle.
                 */
                Point2D pos_t0 = calculator.getCorrectedMousePosition(mouseEventPressed.getX(), mouseEventPressed.getY());
                Point2D pos_t1 = calculator.getCorrectedMousePosition(mouseEventMovedLatest.getX(), mouseEventMovedLatest.getY());

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
                Point2D correctedMousePos = calculator.getCorrectedMousePosition(event.getX(), event.getY());

                arcTemp.endXProperty().set(correctedMousePos.getX());
                arcTemp.endYProperty().set(correctedMousePos.getY());

            } else if (!isInNodeCreationMode.get()) {

                /**
                 * Dragging Mode. Drags selected node(s).
                 */
                try {
                    setEditorMode(isInDraggingMode);
                    mainController.HideElementBox();
                } catch (Exception ex) {
                    messengerService.addToLog(ex.getMessage());
                }

                Object eventTarget = event.getTarget();

                if (eventTarget instanceof IGravisChildElement) {
                    eventTarget = ((IGravisChildElement) eventTarget).getParentElement();
                }

                if (eventTarget instanceof IGraphNode) {

                    Point2D pos_t0 = calculator.getCorrectedMousePosition(mouseEventMovedPrevious.getX(), mouseEventMovedPrevious.getY());
                    Point2D pos_t1 = calculator.getCorrectedMousePosition(mouseEventMovedLatest.getX(), mouseEventMovedLatest.getY());

                    for (IGraphElement element : selectionService.getSelectedElements()) {
                        element.translateXProperty().set(element.translateXProperty().get() + pos_t1.getX() - pos_t0.getX());
                        element.translateYProperty().set(element.translateYProperty().get() + pos_t1.getY() - pos_t0.getY());
                    }
                }
            }
        } else if (event.isSecondaryButtonDown()) {

            /**
             * Dragging the entire graph.
             */
            pane.getTopLayer().setTranslateX((mouseEventMovedLatest.getX() - mouseEventMovedPrevious.getX() + pane.getTopLayer().translateXProperty().get()));
            pane.getTopLayer().setTranslateY((mouseEventMovedLatest.getY() - mouseEventMovedPrevious.getY() + pane.getTopLayer().translateYProperty().get()));
        }

        mouseEventMovedPrevious = event;
    }

    private void onMousePressed(MouseEvent event, GraphPane pane) {

        mainController.HideElementBox();

        isPrimaryButtonDown = false;
        isSecondaryButtonDown = false;

        mouseEventMovedPrevious = event; // used for dragging to avoid initial null pointer
        mouseEventPressed.consume(); // avoids multiple pause transitions
        mouseEventPressed = event;

        /**
         * Left clicking.
         */
        if (event.isPrimaryButtonDown()) {

            isPrimaryButtonDown = true;

            /**
             * Clicking graph elements.
             */
            if (event.getTarget() instanceof IGravisElement) {

                disableMode(isInNodeCreationMode);

                IGravisElement element;
                final IGraphNode node;

                if (event.getTarget() instanceof IGravisChildElement) {
                    element = ((IGravisChildElement) event.getTarget()).getParentElement();
                    if (element instanceof IGraphNode) {
                        node = (IGraphNode) element;
                    } else {
                        node = null;
                    }
                } else if (event.getTarget() instanceof IGraphNode) {
                    node = (IGraphNode) event.getTarget();
                } else {
                    node = null;
                }

                if (node != null) {

                    if (!node.getElementHandles().get(0).isSelected()) {

                        // Clicking not yet selected node
                        // Node has to be set to selected to allow immediate dragging
                        if (!event.isControlDown()) {
                            selectionService.unselectAll();
                            selectionService.select(node);
                            selectionService.highlightRelated(node);
                        }
                    }

                    if (isInFreeMode.get()) {

                        /**
                         * Arc Creation Mode. Creates a new arc after a certain
                         * time if event is not consumed.
                         */
                        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(secondsBeforeCreatingArc));
                        pauseTransition.setOnFinished(e -> {
                            if (!event.isConsumed()) {
                                try {
                                    setEditorMode(isInArcCreationMode);
                                    selectionService.unselectAll();
                                    selectionService.highlight(node);
                                    arcTemp = dataService.createTemporaryArc(node);
                                } catch (Exception ex) {
                                    messengerService.addToLog(ex.getMessage());
                                }
                            }
                        });
                        pauseTransition.playFromStart();
                    }
                }

            } else {

                /**
                 * Clicking the pane.
                 */

                if (!event.isControlDown()) {
                    selectionService.unselectAll(); // Clearing current selection.
                } else {
                    mainController.HideElementBox();
                }

                if (event.isShiftDown()) {

                    disableMode(isInNodeCreationMode);

                    /**
                     * Selection Frame Mode. Creating the rectangle.
                     */
                    try {
                        setEditorMode(isInSelectionFrameMode);
                    } catch (Exception ex) {
                        messengerService.addToLog(ex.getMessage());
                    }

                    Point2D pos = calculator.getCorrectedMousePosition(mouseEventPressed.getX(), mouseEventPressed.getY());
                    selectionFrame.setX(pos.getX());
                    selectionFrame.setY(pos.getY());
                    selectionFrame.setWidth(0);
                    selectionFrame.setHeight(0);

                    pane.getTopLayer().getChildren().add(selectionFrame);

                } else if (isInNodeCreationMode.get()) {

                    // Creating node at event location.
                    try {
                        dataService.create(editorToolsController.getCreateNodeType(), event.getX(), event.getY());
                    } catch (DataGraphServiceException ex) {
                        messengerService.addToLog(ex.getMessage());
                    }
                }
            }
        } else if (event.isSecondaryButtonDown()) {

            isSecondaryButtonDown = true;

            // TODO
            // Context menu
        }
    }

    private void onMouseReleased(MouseEvent event, GraphPane pane) {

        if (isInSelectionFrameMode.get()) {

            /**
             * Selection Frame Mode. Selecting nodes using the rectangle.
             */
            for (Node node : pane.getTopLayer().getNodeLayer().getChildren()) {
                if (node instanceof IGraphNode) {
                    if (node.getBoundsInParent().intersects(selectionFrame.getBoundsInParent())) {
                        Platform.runLater(() -> {
                            selectionService.select((IGraphNode) node);
                        });
                    }
                }
            }
            pane.getTopLayer().getChildren().remove(selectionFrame);
            disableMode(isInSelectionFrameMode);

        } else if (isInArcCreationMode.get()) {

            /**
             * Arc Creation Mode. Binding or deleting arc.
             */
            Object eventTarget = event.getPickResult().getIntersectedNode();

            if (eventTarget instanceof IGravisChildElement) {
                eventTarget = ((IGravisChildElement) eventTarget).getParentElement();
            }
            if (eventTarget instanceof IGraphNode) {
                try {
                    dataService.connect(arcTemp.getSource(), (IGraphNode) eventTarget);
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
                }
            }

            selectionService.unhighlight(arcTemp.getSource());
            try {
                dataService.remove(arcTemp);
            } catch (DataGraphServiceException ex) {
                messengerService.addToLog(ex.getMessage());
            }

            disableMode(isInArcCreationMode);

        } else {

            if (isPrimaryButtonDown && !isInDraggingMode.get()) {

                /**
                 * Selecting elements by clicking.
                 */
                Object eventTarget = event.getTarget();
                IGraphElement node;

                if (eventTarget instanceof IGravisChildElement) {
                    eventTarget = ((IGravisChildElement) event.getTarget()).getParentElement();
                }

                if (eventTarget instanceof IGravisElement) {

                    node = (IGraphElement) eventTarget;

                    if (event.isControlDown()) {
                        if (node.getElementHandles().get(0).isSelected()) {
                            selectionService.unselect(node);
                            selectionService.unhighlightRelated(node);
                        } else {
                            selectionService.select(node);
                            selectionService.highlightRelated(node);
                        }
                        mainController.HideElementBox();
                    } else {
                        selectionService.unselectAll();
                        selectionService.select(node);
                        selectionService.highlightRelated(node);
                        mainController.ShowDetails(node);
                    }
                }
            }
        }
        event.consume();
    }

    private void onMouseClicked(MouseEvent event) {

        mouseEventPressed.consume();
        event.consume();

        isPrimaryButtonDown = false;
        disableMode(isInDraggingMode);
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

    /**
     * Lock editor mode. Prevents more than one mode to be active at any time.
     *
     * @param mode
     * @throws Exception
     */
    private synchronized void setEditorMode(BooleanProperty mode) throws Exception {
        if (!mode.get()) {
            if (!isInFreeMode.get()) {
                if (isInArcCreationMode.get()) {
                    throw new Exception("Changing mode is blocked! In ArcCreation Mode.");
                } else if (isInDraggingMode.get()) {
                    throw new Exception("Changing mode is blocked! In Draggin Mode.");
                } else if (isInNodeCreationMode.get()) {
                    throw new Exception("Changing mode is blocked! In NodeCreation Mode.");
                } else if (isInSelectionFrameMode.get()) {
                    throw new Exception("Changing mode is blocked! In SelectionFrame Mode.");
                }
            }
            isInFreeMode.set(false);
            mode.set(true);
        }
    }

    /**
     * Unlock editor mode.
     *
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
     * @throws Exception
     */
    public void setNodeCreationMode() throws Exception {
        setEditorMode(isInNodeCreationMode);
    }
}
