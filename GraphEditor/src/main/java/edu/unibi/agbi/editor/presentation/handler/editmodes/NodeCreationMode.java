package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.editor.business.service.ModelService;
import edu.unibi.agbi.editor.presentation.controller.editor.graph.ToolsController;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodeCreationMode implements IEditMode {

    @Autowired
    private MouseEventHandler mouseEventHandler;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ToolsController toolsController;

    @Override
    public void performMouseMovedAction(MouseEvent event, GraphPane pane) {

    }

    @Override
    public void performMousePressedActionPrimaryButton(MouseEvent event, GraphPane pane) {
        mouseEventHandler.defaultMousePressedActionPrimaryButtonDown(event);
    }

    @Override
    public void performMouseDraggedAction(MouseEvent event, GraphPane pane) {
        mouseEventHandler.defaultMouseDraggedAction(event, pane);
    }

    @Override
    public void performMouseReleasedAction(MouseEvent event, GraphPane pane) {
        createNodeAtEventPosition(event);
    }

    private void createNodeAtEventPosition(MouseEvent event) {
        /**
         * Node Creation Mode. Create node at target location.
         */
        if (mouseEventHandler.isPrimaryButtonDownForLatestMousePressedEvent()) {
            try {
                modelService.create(
                        modelService.getDao(),
                        toolsController.getCreateNodeType(),
                        event.getX(),
                        event.getY()
                );
            } catch (Exception e) {
                e.printStackTrace(); // todo send to logger
            }
        }
    }
}
