/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.exception.DataServiceException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class KeyEventHandler
{
    @Autowired private SelectionService selectionService;
    @Autowired private DataService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private MouseEventHandler mouseEventHandler;

    private DataDao activeDao;
    private List<IGraphNode> nodes;
    private boolean isCutting;

    public void registerTo(Scene scene) {
        scene.setOnKeyPressed(e -> onKeyPressed(e));
    }

    private void onKeyPressed(KeyEvent event) {

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            mouseEventHandler.UnlockEditorMode();
            selectionService.unselectAll();
        } 
        else if (event.getCode().equals(KeyCode.DELETE)) {
            /**
             * Delete selected nodes.
             */
            Platform.runLater(() -> {
                dataService.remove(selectionService.getSelectedElements());
                selectionService.unselectAll();
            });
        } else if (event.isControlDown()) {
            /**
             * Copy, clone or paste selected nodes.
             */
            if (event.getCode().equals(KeyCode.C)) {
                activeDao = dataService.getActiveDao();
                nodes = selectionService.copy();
                isCutting = false;
            } else if (event.getCode().equals(KeyCode.X)) {
                activeDao = dataService.getActiveDao();
                nodes = selectionService.copy();
                isCutting = true;
            } else if (event.getCode().equals(KeyCode.V)) {
                selectionService.unselectAll();
                try {
                    if (activeDao != dataService.getActiveDao()) {
                        selectionService.selectAll(dataService.paste(nodes, false));
                    } else {
                        selectionService.selectAll(dataService.paste(nodes, isCutting));
                    }
                } catch (DataServiceException ex) {
                    messengerService.addException(ex);
                }
            }
        }
        event.consume();
    }
}
