/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.handler;

import edu.unibi.agbi.gnius.core.service.DataGraphService;
import edu.unibi.agbi.gnius.core.service.MessengerService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.exception.DataGraphServiceException;
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
    @Autowired private DataGraphService dataService;
    @Autowired private MessengerService messengerService;
    @Autowired private MouseEventHandler mouseEventHandler;
    
    private boolean isCloning;
    
    public void registerTo(Scene scene) {
        scene.setOnKeyPressed(e -> onKeyPressed(e));
    }
    
    private void onKeyPressed(KeyEvent event) {

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            mouseEventHandler.UnlockEditorMode();
            selectionService.unselectAll();
        } /**
         * Delete selected nodes.
         */
        else if (event.getCode().equals(KeyCode.DELETE)) {
            Platform.runLater(() -> {
                try {
                    dataService.remove(selectionService.getSelectedElements());
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex.getMessage());
                }
                selectionService.unselectAll();
            });
        } /**
         * Copy, clone or paste selected nodes.
         */
        else if (event.isControlDown()) {

            if (event.getCode().equals(KeyCode.C)) {
                selectionService.copy();
                isCloning = false;

            } else if (event.getCode().equals(KeyCode.X)) {
                selectionService.copy();
                isCloning = true;

            } else if (event.getCode().equals(KeyCode.V)) {
                selectionService.unselectAll();
                try {
                    selectionService.selectAll(dataService.paste(selectionService.getCopiedNodes(), isCloning));
                } catch (DataGraphServiceException ex) {
                    messengerService.addToLog(ex);
                }
            }
        }
        event.consume();
    }
}
