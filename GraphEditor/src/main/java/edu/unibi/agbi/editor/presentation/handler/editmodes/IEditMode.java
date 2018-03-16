package edu.unibi.agbi.editor.presentation.handler.editmodes;

import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.scene.input.MouseEvent;

public interface IEditMode {

    public void performMouseMovedAction(final MouseEvent event, GraphPane pane);

    public void performMousePressedActionPrimaryButton(final MouseEvent event, GraphPane pane);

    public void performMouseDraggedAction(final MouseEvent event, GraphPane pane);

    public void performMouseReleasedAction(final MouseEvent event, GraphPane pane);
}
