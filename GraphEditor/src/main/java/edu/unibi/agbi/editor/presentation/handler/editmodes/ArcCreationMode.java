package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.service.MessengerService;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphArc;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArcCreationMode implements IEditMode {

    @Autowired
    private Calculator calculator;

    @Autowired
    private MouseEventHandler mouseEventHandler;

    @Autowired
    private ModelService modelService;

    @Autowired
    private MessengerService messengerService;

    @Autowired
    private SelectionService selectionService;

    private IGraphArc arcTemp;

    @Override
    public void performMouseMovedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMousePressedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMouseDraggedAction(MouseEvent event, GraphPane pane) {
        moveArcEndToMousePointer(event);
    }

    @Override
    public void performMouseReleasedAction(MouseEvent event, GraphPane pane) {

        createArcToEventTarget(event);
        mouseEventHandler.setEditModeToFreeMode();
    }

    public void setTemporaryArcWithSourceNode(IGraphNode node) {
        arcTemp = modelService.createTmpArc(node);
    }

    private void createArcToEventTarget(MouseEvent event) {

        IGraphNode node = mouseEventHandler.getGraphNodeFromEventPickResult(event);

        if (node != null) {
            try {
                modelService.connect(modelService.getDao(), arcTemp.getSource(), node);
            } catch (DataException ex) {
                messengerService.printMessage("Cannot connect nodes!");
                messengerService.setStatusAndAddExceptionToLog("Selected nodes cannot be connected!", ex);
            }
        }

        selectionService.unhighlight(arcTemp.getSource());
        try {
            modelService.remove(arcTemp);
        } catch (DataException ex) {
            messengerService.addException(ex);
        }

    }

    private void moveArcEndToMousePointer(MouseEvent event) {
        Point2D correctedMousePos
                = calculator.getCorrectedPositionInGraph(
                modelService.getGraph(),
                event.getX(),
                event.getY());

        arcTemp.endXProperty().set(correctedMousePos.getX());
        arcTemp.endYProperty().set(correctedMousePos.getY());
    }
}
