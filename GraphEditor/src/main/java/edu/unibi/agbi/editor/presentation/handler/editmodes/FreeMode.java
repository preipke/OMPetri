package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.editor.business.service.SelectionService;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.presentation.controller.editor.GraphController;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.entity.IGravisItem;
import edu.unibi.agbi.gravisfx.entity.child.IGravisChild;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.animation.PauseTransition;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FreeMode implements IEditMode {

    @Autowired
    private MouseEventHandler mouseEventHandler;

    @Autowired
    private SelectionService selectionService;

    @Autowired
    private GraphController graphController;

    private final PauseTransition clickTransition = new PauseTransition(Duration.seconds(0.2));

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
        if (mouseEventHandler.isPrimaryButtonDownForLatestMousePressedEvent()) {
            selectEventTarget(event);
        }
    }

    private void selectEventTarget(MouseEvent event) {

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

    private void selectElementAndDeselectOthers(IGraphElement element) {
        selectionService.unselectAll();
        selectionService.select(element);
        selectionService.highlightRelated(element);
    }

    private void deselectAllNodesOrKeepThem(MouseEvent event) {
        if (!event.isControlDown()) {
            selectionService.unselectAll(); // Clearing current selection.
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
}
