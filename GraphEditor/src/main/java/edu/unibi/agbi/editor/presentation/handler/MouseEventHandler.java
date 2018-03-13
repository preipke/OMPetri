/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.handler;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.service.HierarchyService;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.editor.presentation.controller.editor.GraphController;
import edu.unibi.agbi.editor.presentation.controller.editor.graph.ToolsController;
import edu.unibi.agbi.gravisfx.entity.IGravisItem;
import edu.unibi.agbi.gravisfx.entity.child.IGravisChild;
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

import java.util.List;

/**
 * @author PR
 */
@Component
public class MouseEventHandler {

    @Autowired
    private Calculator calculator;
    @Autowired
    private ModelService dataService;
    @Autowired
    private HierarchyService hierarchyService;
    @Autowired
    private MessengerService messengerService;
    @Autowired
    private SelectionService selectionService;

    @Autowired
    private GraphController graphController;
    @Autowired
    private ToolsController editorToolsController;

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

    private IDataNode dataNodeToBeCloned;
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
    public void registerMouseEventHandlersToPane(GraphPane graphPane) {
        graphPane.setOnMouseMoved(e -> onMouseMoved(e, graphPane));
        graphPane.setOnMousePressed(e -> onMousePressed(e, graphPane));
        graphPane.setOnMouseDragged(e -> onMouseDragged(e, graphPane));
        graphPane.setOnMouseReleased(e -> onMouseReleased(e, graphPane));
        graphPane.setOnMouseClicked(e -> onMouseClicked(e));
        // order: pressed -> dragged -> released -> clicked
    }

    private void onMouseMoved(MouseEvent event, GraphPane pane) {

        eventMouseMoved = event;

        enableHoveredStyleToEventTarget(event);
    }

    private void enableHoveredStyleToEventTarget(MouseEvent event) {
        if (!isPrimaryButtonDown) {
            if (event.getTarget() instanceof IGravisItem) {
                selectionService.hover((IGravisItem) event.getTarget());
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
        if (event.isPrimaryButtonDown()) { // Holding shift will open the selection rectangle in a following handler.

            isPrimaryButtonDown = true;

            if (event.isShiftDown()) {

                deselectAllNodes(event);

            } else if (event.getTarget() instanceof IGravisItem) { // Clicking graph elements.

                UnlockEditorMode();

                final IGraphNode node = getGraphNodeFromEventTarget(event);

                if (node != null) {

                    if (!selectionService.isElementSelected(node)) {

                        if (!event.isControlDown()) {
                            selectElementAndDeselectOthers(node);
                        }
                    }

                    if (event.getClickCount() == 2) {
                        if (node instanceof GraphCluster) {
                            openClusterNode((GraphCluster) node);
                            return;
                        }
                    }

                    enableTransitionForArcCreation(event, node);
                }

            } else { // Clicking the pane.

                deselectAllNodes(event);
            }

        } else {
            UnlockEditorMode();
        }
    }

    /**
     * Arc Creation Mode. Creates a new arc after a certain time
     * if event is not consumed.
     */
    private void enableTransitionForArcCreation(MouseEvent event, IGraphNode node) {
        pauseTransition.setOnFinished(e -> {
            if (!event.isConsumed()) {
                try {
                    selectionService.unselectAll();
                    selectionService.highlight(node);
                    arcTemp = dataService.createTmpArc(node);
                    setEditorMode(isInArcCreationMode);
                } catch (Exception ex) {
                    messengerService.addException("Cannot switch to arc creation mode!", ex);
                }
            }
        });
        pauseTransition.playFromStart();
    }

    private void openClusterNode(GraphCluster node) {
        hierarchyService.open(
                node,
                dataService.getDao()
        );
    }

    private void selectElementAndDeselectOthers(IGraphElement element) {
        selectionService.unselectAll();
        selectionService.select(element);
        selectionService.highlightRelated(element);
    }

    private void deselectAllNodes(MouseEvent event) {
        if (!event.isControlDown()) {
            selectionService.unselectAll(); // Clearing current selection.
        }
    }

    private void onMouseDragged(MouseEvent event, GraphPane pane) {

        eventMousePressed.consume(); // for PauseTransition
        eventMouseMoved = event;

        try {

            if (event.isPrimaryButtonDown()) {

                if (isInSelectionFrameMode.get()) {

                    resizeSelectionRectangle();

                } else if (isInArcCreationMode.get()) {

                    moveArcEndToMousePointer(event);

                } else if (isInDraggingMode.get()) {

                    dragSelectElementsIfEventTargetIsSelected(event);

                } else {

                    UnlockEditorMode();

                    IGraphNode node = getGraphNodeFromEventTarget(event);

                    if (event.isShiftDown() || node == null) {

                        enableSelectionRectangle(pane);

                    } else {

                        setEditorMode(isInDraggingMode);
                    }
                }

            } else if (event.isSecondaryButtonDown()) {

                translatePaneByDistanceBetweenEvents(pane, eventMouseMoved, eventMouseMovedPrevious);
            }

        } catch (Exception ex) {
            messengerService.addException("Cannot switch editing mode!", ex);
            return;
        }

        eventMouseMovedPrevious = event;
    }

    private void translatePaneByDistanceBetweenEvents(GraphPane pane, MouseEvent currentEvent, MouseEvent previousEvent) {
        pane.getGraph().setTranslateX((currentEvent.getX() - previousEvent.getX() + pane.getGraph().translateXProperty().get()));
        pane.getGraph().setTranslateY((currentEvent.getY() - previousEvent.getY() + pane.getGraph().translateYProperty().get()));
    }

    private void enableSelectionRectangle(GraphPane pane) throws Exception {

        setEditorMode(isInSelectionFrameMode);

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

    private void dragSelectElementsIfEventTargetIsSelected(MouseEvent event) {

        IGraphNode node = getGraphNodeFromEventTarget(event);

        if (node != null) {

            if (selectionService.isElementSelected(node)) {

                dragElements(selectionService.getSelectedElements());
            }
        }
    }

    private IGraphNode getGraphNodeFromEventTarget(MouseEvent event) {

        Object eventTarget = event.getTarget();

        return getGraphNodeFromObject(eventTarget);
    }

    private IGraphNode getGraphNodeFromEventPickResult(MouseEvent event) {

        Object pickResult = event.getPickResult().getIntersectedNode();

        return getGraphNodeFromObject(pickResult);
    }

    private IGraphNode getGraphNodeFromObject(Object eventTarget) {

        if (eventTarget instanceof IGravisChild) {
            eventTarget = ((IGravisChild) eventTarget).getParentShape();
        }

        if (eventTarget instanceof IGraphNode) {
            return (IGraphNode) eventTarget;
        } else {
            return null;
        }
    }

    private void dragElements(List<IGraphElement> elements) {
        Point2D pos_t0, pos_t1;
        pos_t0 = calculator.getCorrectedPosition(dataService.getGraph(), eventMouseMovedPrevious.getX(), eventMouseMovedPrevious.getY());
        pos_t1 = calculator.getCorrectedPosition(dataService.getGraph(), eventMouseMoved.getX(), eventMouseMoved.getY());

        for (IGraphElement element : elements) {
            element.translateXProperty().set(element.translateXProperty().get() + pos_t1.getX() - pos_t0.getX());
            element.translateYProperty().set(element.translateYProperty().get() + pos_t1.getY() - pos_t0.getY());
        }
    }

    private void moveArcEndToMousePointer(MouseEvent event) {
        Point2D correctedMousePos
                = calculator.getCorrectedPosition(
                dataService.getGraph(),
                event.getX(),
                event.getY());

        arcTemp.endXProperty().set(correctedMousePos.getX());
        arcTemp.endYProperty().set(correctedMousePos.getY());
    }

    /**
     * Resizes the selection rectangle.
     */
    private void resizeSelectionRectangle() {

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
    }

    private void onMouseReleased(final MouseEvent event, GraphPane pane) {

        if (eventMouseReleased != null) {
            eventMouseReleased.consume(); // event related to the mouse click transition
        }
        eventMouseReleased = event;

        try {

            if (isInSelectionFrameMode.get()) {

                removeRectangleAndSelectElementsInside(pane);
                disableMode(isInSelectionFrameMode);

            } else if (isInArcCreationMode.get()) {

                createArcToEventTarget(event);
                disableMode(isInArcCreationMode);

            } else if (isInNodeCreationMode.get()) {

                createNodeAtEventPosition(event);

            } else if (isInNodeCloningMode.get()) {

                cloneNodeAtEventPosition(event);
                disableMode(isInNodeCloningMode);

            } else if (isInDraggingMode.get()) {

                alignSelectedElementsToGrid();
                disableMode(isInDraggingMode);

            } else {

                if (isPrimaryButtonDown) {

                    /**
                     * Selecting elements by clicking.
                     */
                    Object eventTarget = event.getTarget();

                    if (eventTarget instanceof IGravisChild) {
                        eventTarget = ((IGravisChild) event.getTarget()).getParentShape();
                    }

                    if (eventTarget instanceof IGravisItem) {

                        IGraphElement element;
                        element = (IGraphElement) eventTarget;

                        if (event.isControlDown()) {

                            selectOrDeselectElement(element);

                        } else {

                            selectElementAndDeselectOthers(element);
                            enableTransitionForSwitchingToInspectorView(event, element);
                        }
                    }
                }
            }
        } catch (DataException ex) {
            messengerService.addException("Cannot create node!", ex);
        }
    }

    private void enableTransitionForSwitchingToInspectorView(MouseEvent event, IGraphElement element) {
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

    private void selectOrDeselectElement(IGraphElement element) {
        if (selectionService.isElementSelected(element)) {
            selectionService.unselect(element);
            selectionService.unhighlightRelated(element);
        } else {
            selectionService.select(element);
            selectionService.highlightRelated(element);
        }
        graphController.HideInfo();
    }

    private void alignSelectedElementsToGrid() {
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
    }

    private void createNodeAtEventPosition(MouseEvent event) throws DataException {
        /**
         * Node Creation Mode. Create node at target location.
         */
        if (isPrimaryButtonDown) {
            dataService.create(
                    dataService.getDao(),
                    editorToolsController.getCreateNodeType(),
                    event.getX(),
                    event.getY()
            );
        }
    }

    private void cloneNodeAtEventPosition(MouseEvent event) throws DataException {
        if (isPrimaryButtonDown) {
            dataService.clone(dataService.getDao(), dataNodeToBeCloned, event.getX(), event.getY());
        }

    }

    private void createArcToEventTarget(MouseEvent event) {

        IGraphNode node = getGraphNodeFromEventPickResult(event);

        if (node != null) {
            try {
                dataService.connect(dataService.getDao(), arcTemp.getSource(), node);
            } catch (DataException ex) {
                messengerService.printMessage("Cannot connect nodes!");
                messengerService.setStatusAndAddExceptionToLog("Selected nodes cannot be connected!", ex);
            }
        }

//        /**
//         * Arc Creation Mode. Binding or deleting arc.
//         */
//            Object eventTarget = event.getPickResult().getIntersectedNode();
//
//            if (eventTarget instanceof IGravisChild) {
//                eventTarget = ((IGravisChild) eventTarget).getParentShape();
//            }
//            if (eventTarget instanceof IGraphNode) {
//                try {
//                    dataService.connect(dataService.getDao(), arcTemp.getSource(), (IGraphNode) eventTarget);
//                } catch (DataException ex) {
//                    messengerService.printMessage("Cannot connect nodes!");
//                    messengerService.setStatusAndAddExceptionToLog("Selected nodes cannot be connected!", ex);
//                }
//            }

        selectionService.unhighlight(arcTemp.getSource());
        try {
            dataService.remove(arcTemp);
        } catch (DataException ex) {
            messengerService.addException(ex);
        }

    }

    private void removeRectangleAndSelectElementsInside(GraphPane pane) {
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
     * @param dataToClone the node that will be cloned
     * @throws Exception
     */
    public synchronized void setCloningMode(IDataNode dataToClone) throws Exception {
        setEditorMode(isInNodeCloningMode);
        this.dataNodeToBeCloned = dataToClone;
    }

    /**
     * Enables the Event Handler to create nodes on the graph pane.
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
