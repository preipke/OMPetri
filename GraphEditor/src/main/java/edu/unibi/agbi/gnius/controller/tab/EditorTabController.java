/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.controller.tab;

import edu.unibi.agbi.gnius.controller.tab.editor.EditorPaneController;
import edu.unibi.agbi.gnius.controller.tab.editor.EditorToolsController;
import edu.unibi.agbi.gnius.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.service.DataService;
import edu.unibi.agbi.gnius.service.SelectionService;
import edu.unibi.agbi.gnius.service.exception.NodeCreationException;
import edu.unibi.agbi.gnius.util.Calculator;

import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import edu.unibi.agbi.gravisfx.graph.node.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisCircle;
import edu.unibi.agbi.gravisfx.graph.node.entity.GravisRectangle;
import edu.unibi.agbi.petrinet.model.entity.PNNode;

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
    @Autowired private SelectionService selectionService;
    
    @Autowired private EditorToolsController editorToolsController;
    
    @Autowired private MouseEventHandler mouseEventHandler;
    
    public void CreateNode(MouseEvent target) {
        try {
            dataService.create(editorToolsController.getNodeChoice().getType() , target , Point2D.ZERO);
        } catch (NodeCreationException ex) {

        }
    }
    
    public void PasteNodes(boolean isCopying) {
        IGravisNode[] nodes = selectionService.getNodesCopy();
        TopLayer topLayer = null;
        
        Point2D center = Calculator.getCenter(nodes);
        double offsetX, offsetY;
        
        IGravisNode node;
        for (int i = 0; i < nodes.length; i++) {
            
            offsetX = (nodes[i].getTranslateX() - center.getX());
            offsetY = (nodes[i].getTranslateY() - center.getY());

            if (isCopying) {
                
                
                
                Object pnNode = nodes[i].getRelatedObject();
                node = nodes[i].getCopy();
                
                
                if (GravisCircle.class.isAssignableFrom(pnNode.getClass())) {
                    
                } else if (GravisRectangle.class.isAssignableFrom(pnNode.getClass())) {
                    
                }
            } else {
                node = nodes[i].getClone();
                ((PNNode)node.getRelatedObject()).addShape(node);
            }
            node.setActiveStyleClass(nodes[i].getActiveStyleClass());

            if (topLayer == null) {
                topLayer = ((TopLayer)nodes[i].getShape().getParent().getParent());
            }

            node.setTranslate(
                    offsetX + (mouseEventHandler.getLatestMovedMouseEvent().getX() - topLayer.translateXProperty().get()) / topLayer.getScale().getX() ,
                    offsetY + (mouseEventHandler.getLatestMovedMouseEvent().getY() - topLayer.translateYProperty().get()) / topLayer.getScale().getX()
            );

            dataService.add(node);
            selectionService.add(node);
        }
    }
    
    public void RemoveSelected() {
        for (IGravisEdge edge : selectionService.getEdges()) {
            dataService.remove(edge);
        }
        for (IGravisNode node : selectionService.getNodes()) {
            dataService.remove(node);
        }
        selectionService.clear();
    }

    @Override
    public void initialize(URL location , ResourceBundle resources) {
    }
}
