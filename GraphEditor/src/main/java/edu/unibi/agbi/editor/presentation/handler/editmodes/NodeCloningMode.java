package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.editor.business.exception.DataException;
import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.core.util.Calculator;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodeCloningMode implements IEditMode {

    @Autowired
    private MouseEventHandler mouseEventHandler;

    @Autowired
    private ModelService modelService;

    @Autowired
    private Calculator calculator;

    @Override
    public void performMouseMovedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMousePressedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMouseDraggedAction(MouseEvent event, GraphPane pane) {
        mouseEventHandler.performGeneralMouseDraggedAction(event, pane);
    }

    @Override
    public void performMouseReleasedAction(MouseEvent event, GraphPane pane) {
        cloneNodeAtEventPosition(event);
        mouseEventHandler.setEditModeToFreeMode();
    }

    private void cloneNodeAtEventPosition(MouseEvent event) {

        if (mouseEventHandler.isPrimaryButtonDownForLatestMousePressedEvent()) {
            try {
                modelService.clone(
                        modelService.getDao(),
                        mouseEventHandler.getNodeStoredForCloning(),
                        event.getX(),
                        event.getY());
            } catch (DataException e) {
                e.printStackTrace(); // todo send to logger
            }
        }

    }
}
