/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.DataException;
import edu.unibi.agbi.gnius.core.model.dao.DataDao;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gnius.core.service.HierarchyService;
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
    @Autowired private DataService dataService;
    @Autowired private HierarchyService hierarchyService;
    @Autowired private MessengerService messengerService;
    @Autowired private SelectionService selectionService;
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
        } else if (event.getCode().equals(KeyCode.BACK_SPACE)) {
            hierarchyService.climb();
        } else if (event.isControlDown()) {
            /**
             * Copy, cut or paste selected nodes.
             */
            if (event.getCode().equals(KeyCode.C)) {
                activeDao = dataService.getDao();
                nodes = selectionService.copy();
                isCutting = false;
            } else if (event.getCode().equals(KeyCode.X)) {
                activeDao = dataService.getDao();
                nodes = selectionService.copy();
                isCutting = true;
            } else if (event.getCode().equals(KeyCode.V)) {
                selectionService.unselectAll();
                try {
                    if (activeDao != dataService.getDao()) {
                        selectionService.selectAll(dataService.paste(dataService.getDao(), nodes, false));
                    } else {
                        selectionService.selectAll(dataService.paste(dataService.getDao(), nodes, isCutting));
                    }
                } catch (DataException ex) {
                    messengerService.addException(ex);
                }
            }
        }
        event.consume();
    }
}
