/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.business.controller.editor.graph.ToolsController;
import edu.unibi.agbi.gnius.business.controller.editor.GraphController;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphArc;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.model.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.entity.data.IDataNode;
import edu.unibi.agbi.gnius.core.service.HierarchyService;
import edu.unibi.agbi.gnius.util.Calculator;
import edu.unibi.agbi.gravisfx.entity.IGravisChild;
import edu.unibi.agbi.gravisfx.entity.IGravisElement;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
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

    @Autowired private Calculator calculator;
    @Autowired private DataService dataService;
    @Autowired private HierarchyService hierarchyService;
    @Autowired private MessengerService messengerService;
    @Autowired private SelectionService selectionService;

    @Autowired private GraphController graphController;
    @Autowired private ToolsController editorToolsController;

    // TODO bind GUI buttons to these later
    private final BooleanProperty isInArcCreationMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInDraggingMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInNodeCreationMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInNodeCloningMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInSelectionFrameMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isInFreeMode = new SimpleBooleanProperty(true);

    private final Rectangle selectionFrame;
    private final PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.35));
    private final PauseTransition clickTransition = new PauseTransition(Duration.seconds(0.2));

    private boolean isPrimaryButtonDown = false;

    private MouseEvent eventMouseMoved;
    private MouseEvent eventMouseMovedPrevious;
    private MouseEvent eventMousePressed;
    private MouseEvent eventMouseReleased;

    private IDataNode data;
    private IGraphArc arcTemp;

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
        graphPane.setOnMousePressed(e -> onMousePressed(e, graphPane));
        graphPane.setOnMouseDragged(e -> onMouseDragged(e, graphPane));
        graphPane.setOnMouseReleased(e -> onMouseReleased(e, graphPane));
        graphPane.setOnMouseClicked(e -> onMouseClicked(e));
        // order: pressed -> dragged -> released -> clicked
    }

    private void onMouseMoved(MouseEvent event, GraphPane pane) {

        eventMouseMoved = event;

        if (!isPrimaryButtonDown) {
            if (event.getTarget() instanceof IGravisElement) {
                selectionService.hover((IGravisElement) event.getTarget());
            } else {
                selectionService.hover(null);
            }
        }
    }

    private void onMousePressed(final MouseEvent event, GraphPane pane) {

        isPrimaryButtonDown = false;

        eventMouseMovedPrevious = event; // used for dragging to avoid initial null pointer
        if (eventMousePressed != null) {
            eventMousePressed.consume(); // avoids multiple pause transitions
        }
        eventMousePressed = event;

        /**
         * Left clicking.
         */
        if (event.isPrimaryButtonDown()) {

            isPrimaryButtonDown = true;

            if (event.isShiftDown()) {

                /**
                 * Holding shift. Will open selection rectangle.
                 */
                if (!event.isControlDown()) {
                    selectionService.unselectAll(); // Clearing current selection.
                }

            } else if (event.getTarget() instanceof IGravisElement) {

                /**
                 * Clicking graph elements.
                 */
                UnlockEditorMode();

                IGravisElement element;
                final IGraphNode node;

                if (event.getTarget() instanceof IGravisChild) {
                    element = ((IGravisChild) event.getTarget()).getParentShape();
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

                    if (!node.getElementHandles().iterator().next().isSelected()) {

                        // Clicking not yet selected node
                        // Node has to be set to selected to allow immediate dragging
                        if (!event.isControlDown()) {
                            selectionService.unselectAll();
                            selectionService.select(node);
                            selectionService.highlightRelated(node);
                        }
                    }

                    /**
                     * Double click.
                     */
                    if (event.getClickCount() == 2) {
                        if (node instanceof GraphCluster) {
                            hierarchyService.open((GraphCluster) node);
                            return;
                        }
                    }

                    /**
                     * Arc Creation Mode. Creates a new arc after a certain time
                     * if event is not consumed.
                     */
                    pauseTransition.setOnFinished(e -> {
                        if (!event.isConsumed()) {
                            try {
                                selectionService.unselectAll();
                                selectionService.highlight(node);
                                arcTemp = dataService.CreateConnectionTmp(node);
                                setEditorMode(isInArcCreationMode);
                            } catch (Exception ex) {
                                messengerService.addException("Cannot switch to arc creation mode!", ex);
                            }
                        }
                    });
                    pauseTransition.playFromStart();
                }

            } else {

                /**
                 * Clicking the pane.
                 */
                if (!event.isControlDown()) {
                    selectionService.unselectAll(); // Clearing current selection.
                }
            }

        } else {
            UnlockEditorMode();
        }
    }

    private void onMouseDragged(MouseEvent event, GraphPane pane) {

        eventMousePressed.consume(); // for PauseTransition
        eventMouseMoved = event;

        if (event.isPrimaryButtonDown()) {

            if (isInSelectionFrameMode.get()) {

                /**
                 * Selection Frame Mode. Resizes the selection rectangle.
                 */
                Point2D pos_t0
                        = calculator.getCorrectedPosition(
                                dataService.getGraph(),
                                eventMousePressed.getX(),
                                eventMousePressed.getY());
                Point2D pos_t1
                        = calculator.getCorrectedPosition(
                                dataService.getGraph(),
                                eventMouseMoved.getX(),
                                eventMouseMoved.getY());

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
                Point2D correctedMousePos
                        = calculator.getCorrectedPosition(
                                dataService.getGraph(),
                                event.getX(),
                                event.getY());

                arcTemp.endXProperty().set(correctedMousePos.getX());
                arcTemp.endYProperty().set(correctedMousePos.getY());

            } else if (isInDraggingMode.get()) {

                /**
                 * Dragging Mode. Drags selected node(s).
                 */
                Object eventTarget = event.getTarget();

                if (eventTarget instanceof IGravisChild) {
                    eventTarget = ((IGravisChild) eventTarget).getParentShape();
                }

                if (eventTarget instanceof IGraphNode) {

                    Point2D pos_t0, pos_t1;

                    pos_t0 = calculator.getCorrectedPosition(dataService.getGraph(), eventMouseMovedPrevious.getX(), eventMouseMovedPrevious.getY());
                    pos_t1 = calculator.getCorrectedPosition(dataService.getGraph(), eventMouseMoved.getX(), eventMouseMoved.getY());

                    for (IGraphElement element : selectionService.getSelectedElements()) {
                        element.translateXProperty().set(element.translateXProperty().get() + pos_t1.getX() - pos_t0.getX());
                        element.translateYProperty().set(element.translateYProperty().get() + pos_t1.getY() - pos_t0.getY());
                    }
                }

            } else {

                Object eventTarget = event.getTarget();

                if (eventTarget instanceof IGravisChild) {
                    eventTarget = ((IGravisChild) eventTarget).getParentShape();
                }

                if (event.isShiftDown() || !(eventTarget instanceof IGraphNode)) {

                    double distance
                            = Math.sqrt(Math.pow(event.getX() - eventMousePressed.getX(), 2)
                                    + Math.pow(event.getY() - eventMousePressed.getY(), 2));

                    if (event.isShiftDown() || distance > 5) {

                        /**
                         * Selection Frame Mode. Creating the rectangle.
                         */
                        try {
                            UnlockEditorMode();
                            setEditorMode(isInSelectionFrameMode);
                        } catch (Exception ex) {
                            messengerService.addException("Cannot switch to selection frame mode!", ex);
                            return;
                        }

                        Point2D pos = calculator.getCorrectedPosition(
                                dataService.getGraph(),
                                eventMousePressed.getX(),
                                eventMousePressed.getY());

                        selectionFrame.setX(pos.getX());
                        selectionFrame.setY(pos.getY());
                        selectionFrame.setWidth(0);
                        selectionFrame.setHeight(0);

                        pane.getGraph().getChildren().add(selectionFrame);

                    }

                } else {

                    /**
                     * Dragging Nodes Mode. Activate node dragging.
                     */
                    try {
                        UnlockEditorMode();
                        setEditorMode(isInDraggingMode);
                    } catch (Exception ex) {
                        messengerService.addException("Cannot switch to node dragging mode!", ex);
                    }

                }
            }

        } else if (event.isSecondaryButtonDown()) {

            /**
             * Dragging the entire graph.
             */
            pane.getGraph().setTranslateX((eventMouseMoved.getX() - eventMouseMovedPrevious.getX() + pane.getGraph().translateXProperty().get()));
            pane.getGraph().setTranslateY((eventMouseMoved.getY() - eventMouseMovedPrevious.getY() + pane.getGraph().translateYProperty().get()));
        }

        eventMouseMovedPrevious = event;
    }

    private void onMouseReleased(MouseEvent event, GraphPane pane) {

        if (eventMouseReleased != null) {
            eventMouseReleased.consume(); // click transition event
        }
        eventMouseReleased = event;

        if (isInSelectionFrameMode.get()) {

            /**
             * Selection Frame Mode. Selecting nodes using the rectangle.
             */
            for (Node node : pane.getGraph().getNodeLayerChildren()) {
                if (node instanceof IGraphNode) {
                    if (node.getBoundsInParent().intersects(selectionFrame.getBoundsInParent())) {
                        Platform.runLater(() -> {
                            selectionService.select((IGraphNode) node);
                        });
                    }
                }
            }
            pane.getGraph().getChildren().remove(selectionFrame);

            disableMode(isInSelectionFrameMode);

        } else if (isInArcCreationMode.get()) {

            /**
             * Arc Creation Mode. Binding or deleting arc.
             */
            Object eventTarget = event.getPickResult().getIntersectedNode();

            if (eventTarget instanceof IGravisChild) {
                eventTarget = ((IGravisChild) eventTarget).getParentShape();
            }
            if (eventTarget instanceof IGraphNode) {
                try {
                    dataService.connect(dataService.getDao(), arcTemp.getSource(), (IGraphNode) eventTarget);
                } catch (DataServiceException ex) {
                    messengerService.printMessage("Cannot connect nodes!");
                    messengerService.setStatusAndAddExceptionToLog("Selected nodes cannot be connected!", ex);
                }
            }

            selectionService.unhighlight(arcTemp.getSource());
            try {
                dataService.remove(arcTemp);
            } catch (DataServiceException ex) {
                messengerService.addException(ex);
            }

            disableMode(isInArcCreationMode);

        } else if (isInNodeCreationMode.get()) {

            /**
             * Node Creation Mode. Create node at target location.
             */
            if (isPrimaryButtonDown) {
                try {
                    dataService.CreateNode(dataService.getDao(), editorToolsController.getCreateNodeType(), event.getX(), event.getY());
                } catch (DataServiceException ex) {
                    messengerService.addException("Cannot create node!", ex);
                }
            }

        } else if (isInNodeCloningMode.get()) {

            if (isPrimaryButtonDown) {
                try {
                    dataService.CreateClone(dataService.getDao(), data, event.getX(), event.getY());
                } catch (DataServiceException ex) {
                    messengerService.addException("Cannot create node!", ex);
                }
            }

            disableMode(isInNodeCloningMode);

        } else if (isInDraggingMode.get()) {

            /**
             * Dragging Mode. Align dragged nodes to grid if enabled.
             */
            if (dataService.isGridEnabled()) {

                Point2D pos;
                for (IGraphElement elem : selectionService.getSelectedElements()) {

                    pos = new Point2D(elem.translateXProperty().get(), elem.translateYProperty().get());
                    pos = calculator.getPositionInGrid(pos, dataService.getGraph());

                    elem.translateXProperty().set(pos.getX());
                    elem.translateYProperty().set(pos.getY());
                }
            }

            disableMode(isInDraggingMode);

        } else {

            if (isPrimaryButtonDown) {

                /**
                 * Selecting elements by clicking.
                 */
                Object eventTarget = event.getTarget();
                IGraphElement element;

                if (eventTarget instanceof IGravisChild) {
                    eventTarget = ((IGravisChild) event.getTarget()).getParentShape();
                }

                if (eventTarget instanceof IGravisElement) {

                    element = (IGraphElement) eventTarget;

                    if (event.isControlDown()) {

                        if (element.getElementHandles().iterator().next().isSelected()) {
                            selectionService.unselect(element);
                            selectionService.unhighlightRelated(element);
                        } else {
                            selectionService.select(element);
                            selectionService.highlightRelated(element);
                        }
                        graphController.HideInfo();

                    } else {

                        selectionService.unselectAll();
                        selectionService.select(element);
                        selectionService.highlightRelated(element);

                        clickTransition.setOnFinished(e -> {
                            if (!event.isConsumed()) {
                                if (event.getClickCount() == 2) {
                                    graphController.ShowInspector(element.getData());
                                } else {
                                    graphController.ShowInfo(element);
                                }
                            }
                        });
                        clickTransition.playFromStart();
                    }
                }
            }
        }
    }

    private void onMouseClicked(MouseEvent event) {
        eventMousePressed.consume();
        event.consume();
    }

    public MouseEvent getMouseMovedEventLatest() {
        return eventMouseMoved;
    }

    public MouseEvent geMouseMovedEventPrevious() {
        return eventMouseMovedPrevious;
    }

    public MouseEvent getMousePressedEvent() {
        return eventMousePressed;
    }

    /**
     * Unlocks a mode.
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
     * Enables the Event Handler to clone a node on the graph pane.
     *
     * @param data the node that will be cloned
     * @throws Exception
     */
    public synchronized void setCloningMode(IDataNode data) throws Exception {
        setEditorMode(isInNodeCloningMode);
        this.data = data;
    }

    /**
     * Enables the Event Handler to CreateNode nodes on the graph pane.
     *
     * @throws Exception
     */
    public synchronized void setNodeCreationMode() throws Exception {
        setEditorMode(isInNodeCreationMode);
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
                    throw new Exception("Blocked! In arc creation mode.");
                } else if (isInDraggingMode.get()) {
                    throw new Exception("Blocked! In node dragging mode.");
                } else if (isInNodeCreationMode.get()) {
                    throw new Exception("Blocked! In node creation mode.");
                } else if (isInNodeCloningMode.get()) {
                    throw new Exception("Blocked! In node cloning mode.");
                } else if (isInSelectionFrameMode.get()) {
                    throw new Exception("Blocked! In selection frame mode.");
                }
            }
            isInFreeMode.set(false);
            mode.set(true);
        }
    }

    /**
     * Unlocks all modes so that no actions are performed on the graph pane.
     */
    public synchronized void UnlockEditorMode() {
        isInArcCreationMode.set(false);
        isInDraggingMode.set(false);
        isInNodeCreationMode.set(false);
        isInNodeCloningMode.set(false);
        isInSelectionFrameMode.set(false);
        isInFreeMode.set(true);
    }
}
