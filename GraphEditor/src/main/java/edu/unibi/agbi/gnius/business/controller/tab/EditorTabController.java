/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.business.controller.tab;

import edu.unibi.agbi.gnius.business.controller.tab.editor.EditorPaneController;
import edu.unibi.agbi.gnius.business.controller.tab.editor.EditorToolsController;
import edu.unibi.agbi.gnius.core.model.entity.IDataNode;
import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.service.DataService;
import edu.unibi.agbi.gnius.core.service.SelectionService;
import edu.unibi.agbi.gnius.core.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.util.Calculator;

import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;

import java.net.URL;

import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class EditorTabController implements Initializable
{
    @Autowired private DataService dataService;
    @Autowired private EditorToolsController editorToolsController;
    
    public void CreateNode(MouseEvent target) {
        try {
            dataService.create(editorToolsController.getNodeChoice().getType() , target , Point2D.ZERO);
        } catch (NodeCreationException ex) {

        }
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
}
