/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.presentation.handler;

import edu.unibi.agbi.editor.business.service.HierarchyService;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.editor.core.data.entity.data.IDataNode;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.data.entity.graph.impl.GraphCluster;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.editor.presentation.handler.editmodes.*;
import edu.unibi.agbi.gravisfx.entity.IGravisItem;
import edu.unibi.agbi.gravisfx.entity.child.IGravisChild;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private ArcCreationMode arcCreationMode;
    @Autowired
    private DraggingMode draggingMode;
    @Autowired
    private FreeMode freeMode;
    @Autowired
    private NodeCloningMode nodeCloningMode;
    @Autowired
    private NodeCreationMode nodeCreationMode;
    @Autowired
    private SelectionFrameMode selectionFrameMode;

    private IEditMode editModeCurrentlyActive;

    private final PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.35));

    @Getter
    private final Rectangle selectionFrame;
    @Getter
    private IDataNode nodeStoredForCloning;

    @Getter
    private MouseEvent eventMouseDraggedOrMovedLatest;
    @Getter
    private MouseEvent eventMouseDraggedPrevious;
    @Getter
    private MouseEvent eventMousePressedLatest;
    private MouseEvent eventMousePressedPrevious;
    @Getter
    private MouseEvent eventMouseReleasedLatest;

    public MouseEventHandler() {
        selectionFrame = new Rectangle(0, 0, 0, 0);
        selectionFrame.setStroke(Color.BLUE);
        selectionFrame.setStrokeWidth(1);
        selectionFrame.setStrokeLineCap(StrokeLineCap.ROUND);
        selectionFrame.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));
    }

    public void registerMouseEventHandlersToPane(GraphPane graphPane) {
        graphPane.setOnMouseMoved(e -> onMouseMoved(e, graphPane));
        graphPane.setOnMousePressed(e -> onMousePressed(e, graphPane));
        graphPane.setOnMouseDragged(e -> onMouseDragged(e, graphPane));
        graphPane.setOnMouseReleased(e -> onMouseReleased(e, graphPane));
        graphPane.setOnMouseClicked(e -> onMouseClicked(e));
        // order: pressed -> dragged -> released -> clicked
    }

    private void onMouseMoved(MouseEvent event, GraphPane pane) {

        eventMouseDraggedOrMovedLatest = event;

        enableHoveredStyleToEventTarget(event);
    }

    private void onMousePressed(final MouseEvent event, GraphPane pane) {

        if (eventMousePressedLatest != null) {
            eventMousePressedLatest.consume(); // avoids multiple pause transitions
            eventMousePressedPrevious = eventMousePressedLatest;
        }
        eventMousePressedLatest = event;

        if (event.isPrimaryButtonDown()) {

            editModeCurrentlyActive.performMousePressedActionPrimaryButton(event, pane);

        } else {

            setEditModeToFreeMode();
        }
    }

    private void onMouseDragged(MouseEvent event, GraphPane pane) {

        eventMousePressedLatest.consume(); // for PauseTransition, avoid creating new arc
        eventMouseDraggedPrevious = eventMouseDraggedOrMovedLatest;
        eventMouseDraggedOrMovedLatest = event;

        if (event.isPrimaryButtonDown()) {

            getCurrentlyActiveEditMode().performMouseDraggedAction(event, pane);

        } else if (event.isSecondaryButtonDown()) {

            if (eventMouseDraggedPrevious == null) {
                eventMouseDraggedPrevious = eventMousePressedLatest; // avoid initial null pointer
            }

            translatePaneByDistanceBetweenEvents(pane, eventMouseDraggedOrMovedLatest, eventMouseDraggedPrevious);
        }
    }

    private void onMouseReleased(final MouseEvent event, GraphPane pane) {

        if (eventMouseReleasedLatest != null) {
            eventMouseReleasedLatest.consume(); // event related to the mouse click transition
        }
        eventMouseReleasedLatest = event;

        getCurrentlyActiveEditMode()
                .performMouseReleasedAction(event, pane);
    }

    private void onMouseClicked(MouseEvent event) {
        eventMousePressedLatest.consume(); // for PauseTransition, avoid creating new arc, in case mouse has not been dragged
        event.consume();
    }

    private IEditMode getCurrentlyActiveEditMode() {
        if (editModeCurrentlyActive == null) {
            editModeCurrentlyActive = freeMode;
        }
        return editModeCurrentlyActive;
    }

    public synchronized void setEditModeToFreeMode() {
        editModeCurrentlyActive = freeMode;
    }

    public synchronized void setCloningMode(IDataNode dataToClone) {
        setCurrentlyActiveEditMode(nodeCloningMode);
        this.nodeStoredForCloning = dataToClone;
    }

    public synchronized void setNodeCreationMode() {
        setCurrentlyActiveEditMode(nodeCreationMode);
    }

    public void defaultMousePressedActionPrimaryButtonDown(MouseEvent event) {

        IGraphNode node;

        if (!event.isShiftDown() && // holding shift will open selection rectangle
                event.getTarget() instanceof IGravisItem) { // clicking any graph element, including label.

            setEditModeToFreeMode();

            node = getGraphNodeFromEventTarget(event);

            if (node != null) {

                if (!selectionService.isElementSelected(node)) {
                    if (!event.isControlDown()) {
                        selectElementAndUnselectOthers(node);
                    }
                }

                resetTransitionEnablingArcCreation(event, node);

                openClusterNodeOnDoubleClick(event, node);
            }

        } else { // Clicking the pane.

            if (!event.isControlDown()) {
                selectionService.unselectAll();
            }
        }
    }

    private void openClusterNodeOnDoubleClick(MouseEvent event, IGraphNode node) {

        if (event.getClickCount() == 2) {

            if (eventMousePressedPrevious != null) {

                if (eventMousePressedPrevious.getPickResult() ==
                        eventMousePressedLatest.getPickResult()) {

                    if (node instanceof GraphCluster) {
                        openClusterNode((GraphCluster) node);
                    }
                }
            }
        }
    }

    public void defaultMouseDraggedAction(MouseEvent event, GraphPane pane) {

        setEditModeToFreeMode();

        IGraphNode node = getGraphNodeFromEventTarget(event);

        if (event.isShiftDown() || node == null) {

            enableSelectionRectangle(pane);

        } else {

            setCurrentlyActiveEditMode(draggingMode);
        }
    }

    public boolean isPrimaryButtonDownForLatestMousePressedEvent() {
        if (eventMousePressedLatest != null) {
            return eventMousePressedLatest.isPrimaryButtonDown();
        } else {
            return false;
        }
    }

    private synchronized void setCurrentlyActiveEditMode(IEditMode mode) {

        if (getCurrentlyActiveEditMode() != mode) {

            if (getCurrentlyActiveEditMode() == freeMode) {

                editModeCurrentlyActive = mode;

            } else {

                messengerService.addMessage("Changing edit mode is blocked! Switch to free mode first.");
            }
        }
    }

    private void enableHoveredStyleToEventTarget(MouseEvent event) {
        if (!isPrimaryButtonDownForLatestMousePressedEvent()) {
            if (event.getTarget() instanceof IGravisItem) {
                selectionService.hover((IGravisItem) event.getTarget());
            } else {
                selectionService.hover(null);
            }
        }
    }

    private void resetTransitionEnablingArcCreation(MouseEvent event, IGraphNode node) {
        pauseTransition.setOnFinished(e -> {
            if (!event.isConsumed()) {
                try {
                    selectionService.unselectAll();
                    selectionService.highlight(node);
                    arcCreationMode.setTemporaryArcWithSourceNode(node);
                    setCurrentlyActiveEditMode(arcCreationMode);
                } catch (Exception ex) {
                    messengerService.addException("Cannot switch to arc creation mode!", ex);
                }
            }
        });
        pauseTransition.playFromStart();
    }

    private void openClusterNode(GraphCluster node) {
        hierarchyService.open(node, dataService.getDao());
    }

    private void selectElementAndUnselectOthers(IGraphElement element) {
        selectionService.unselectAll();
        selectionService.select(element);
        selectionService.highlightRelated(element);
    }

    private void translatePaneByDistanceBetweenEvents(GraphPane pane, MouseEvent currentEvent, MouseEvent previousEvent) {
        pane.getGraph().setTranslateX((currentEvent.getX() - previousEvent.getX() + pane.getGraph().translateXProperty().get()));
        pane.getGraph().setTranslateY((currentEvent.getY() - previousEvent.getY() + pane.getGraph().translateYProperty().get()));
    }

    private void enableSelectionRectangle(GraphPane pane) {

        setCurrentlyActiveEditMode(selectionFrameMode);

        Point2D pos = calculator.getCorrectedPositionInGraph(
                dataService.getGraph(),
                eventMousePressedLatest.getX(),
                eventMousePressedLatest.getY());

        selectionFrame.setX(pos.getX());
        selectionFrame.setY(pos.getY());
        selectionFrame.setWidth(0);
        selectionFrame.setHeight(0);

        pane.getGraph().getChildren().add(selectionFrame);
    }

    public IGraphNode getGraphNodeFromEventTarget(MouseEvent event) {

        Object eventTarget = event.getTarget();

        return getGraphNodeFromObject(eventTarget);
    }

    public IGraphNode getGraphNodeFromEventPickResult(MouseEvent event) {

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
}
