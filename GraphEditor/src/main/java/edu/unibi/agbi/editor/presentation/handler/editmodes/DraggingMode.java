package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DraggingMode implements IEditMode {

    @Autowired
    private ModelService modelService;

    @Autowired
    private SelectionService selectionService;

    @Autowired
    private Calculator calculator;

    @Autowired
    private MouseEventHandler mouseEventHandler;

    @Override
    public void performMouseMovedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMousePressedActionPrimaryButton(MouseEvent event, GraphPane pane) {
        mouseEventHandler.defaultMousePressedActionPrimaryButtonDown(event);
    }

    @Override
    public void performMouseDraggedAction(MouseEvent event, GraphPane pane) {
        dragSelectedElementsIfEventTargetIsSelected(event);
    }

    @Override
    public void performMouseReleasedAction(final MouseEvent event, GraphPane pane) {
        alignSelectedElementsToGrid();
        mouseEventHandler.setEditModeToFreeMode();
    }

    private void alignSelectedElementsToGrid() {
        if (modelService.isGridEnabled()) {

            Point2D pos;
            for (IGraphElement elem : selectionService.getSelectedElements()) {

                pos = new Point2D(elem.translateXProperty().get(), elem.translateYProperty().get());
                pos = calculator.getPositionInGrid(pos, modelService.getGraph());

                elem.translateXProperty().set(pos.getX());
                elem.translateYProperty().set(pos.getY());
            }
        }
    }

    private void dragSelectedElementsIfEventTargetIsSelected(MouseEvent event) {
        IGraphNode node = mouseEventHandler.getGraphNodeFromEventTarget(event);

        if (node != null) {

            if (selectionService.isElementSelected(node)) {

                dragElements(selectionService.getSelectedElements());
            }
        }
    }

    private void dragElements(List<IGraphElement> elements) {

        Graph graph;
        graph = modelService.getGraph();

        MouseEvent eventNow, eventPrevious;
        eventPrevious = mouseEventHandler.getEventMouseDraggedPrevious();
        eventNow = mouseEventHandler.getEventMouseDraggedOrMovedLatest();

        Point2D pos_t0, pos_t1;
        pos_t0 = calculator.getCorrectedPositionInGraph(graph, eventPrevious.getX(), eventPrevious.getY());
        pos_t1 = calculator.getCorrectedPositionInGraph(graph, eventNow.getX(), eventNow.getY());

        for (IGraphElement element : elements) {
            element.translateXProperty().set(element.translateXProperty().get() + pos_t1.getX() - pos_t0.getX());
            element.translateYProperty().set(element.translateYProperty().get() + pos_t1.getY() - pos_t0.getY());
        }
    }
}
