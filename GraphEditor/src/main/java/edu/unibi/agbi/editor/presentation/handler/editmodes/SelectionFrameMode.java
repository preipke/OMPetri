package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectionFrameMode implements IEditMode {

    @Autowired
    private MouseEventHandler mouseEventHandler;

    @Autowired
    private Calculator calculator;

    @Autowired
    private ModelService modelService;

    @Autowired
    private SelectionService selectionService;

    @Override
    public void performMouseMovedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMousePressedActionPrimaryButton(MouseEvent event, GraphPane pane) {
        mouseEventHandler.defaultMousePressedActionPrimaryButtonDown(event);
    }

    @Override
    public void performMouseDraggedAction(MouseEvent event, GraphPane pane) {
        resizeSelectionRectangle();
    }

    @Override
    public void performMouseReleasedAction(MouseEvent event, GraphPane pane) {
        removeRectangleAndSelectElementsInside(pane);
        mouseEventHandler.setEditModeToFreeMode();
    }

    private void resizeSelectionRectangle() {

        Graph graph;
        graph = modelService.getGraph();

        MouseEvent event_t0, event_t1;
        event_t0 = mouseEventHandler.getEventMousePressedLatest();
        event_t1 = mouseEventHandler.getEventMouseDraggedOrMovedLatest();

        Point2D pos_t0, pos_t1;
        pos_t0 = calculator.getCorrectedPositionInGraph(
                graph,
                event_t0.getX(),
                event_t0.getY());
        pos_t1 = calculator.getCorrectedPositionInGraph(
                graph,
                event_t1.getX(),
                event_t1.getY());

        double offsetX = pos_t1.getX() - pos_t0.getX();
        double offsetY = pos_t1.getY() - pos_t0.getY();

        Rectangle selectionFrame = mouseEventHandler.getSelectionFrame();

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

    private void removeRectangleAndSelectElementsInside(GraphPane pane) {

        Rectangle selectionFrame = mouseEventHandler.getSelectionFrame();

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
}
