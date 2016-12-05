/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.handler;

import edu.unibi.agbi.gnius.controller.tab.EditorTabController;
import edu.unibi.agbi.gnius.service.SelectionService;

import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import javafx.application.Platform;

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
    @Autowired private EditorTabController editorTabController;
    
    private GraphPane pane;
    public void setGraphPane(GraphPane pane) {
        this.pane = pane;
    }
    
    public void register() {
        
        pane.getScene().setOnKeyPressed((KeyEvent event) -> {
            
            /**
             * Delete selected nodes.
             */
            if (event.getCode().equals(KeyCode.DELETE)) {
                Platform.runLater(() -> {
                    editorTabController.RemoveSelected();
                });
            }
            /**
             * Copy, clone or paste selected nodes.
             */
            else if (event.isControlDown()) {
                
                if (event.getCode().equals(KeyCode.C)) {
                    
                    selectionService.copy();
                    
                } else if (event.getCode().equals(KeyCode.V)) {

                    if (true) { // copying, create new objects
                        selectionService.clear();
                    } else { // cloning, reference the same pn object, keep selection
                        // TODO
                    }
                    editorTabController.PasteNodes();
                }
            }

            event.consume();
        });
    }
}
