/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.util;

import edu.unibi.agbi.gnius.dao.GraphDao;
import edu.unibi.agbi.gnius.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.graph.layer.TopLayer;
import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class Calculator
{
    @Autowired 
    private MouseEventHandler mouseEventHandler;
    
    private final TopLayer topLayer;
    
    @Autowired 
    public Calculator(GraphDao graphDao) {
        topLayer = graphDao.getTopLayer();
    }
    
    /**
     * Computes center of weight.
     * @param nodes
     * @return 
     */
    public Point2D getCenter(IGravisNode[] nodes) {
        double x = 0, y = 0;
        for (IGravisNode node : nodes) {
            x += node.getTranslateX();
            y += node.getTranslateY();
        }
        x = x / nodes.length;
        y = y / nodes.length;
        return new Point2D(x , y);
    }
    
    /**
     * Computes center of weight.
     * @param nodes
     * @return 
     */
    public Point2D getCenter(List<IGravisNode> nodes) {
        double x = 0, y = 0;
        for (IGravisNode node : nodes) {
            x += node.getTranslateX();
            y += node.getTranslateY();
        }
        x = x / nodes.size();
        y = y / nodes.size();
        
        return new Point2D(x , y);
    }
    
    public Point2D getCorrectedMousePosition(MouseEvent event) {
        double x, y;
        x = (event.getX() - topLayer.translateXProperty().get()) / topLayer.getScale().getX();
        y = (event.getY() - topLayer.translateYProperty().get()) / topLayer.getScale().getY();
        return new Point2D(x, y);
    }
    
    public Point2D getCorrectedMousePositionLatest() {
        return getCorrectedMousePosition(mouseEventHandler.getMouseMovedEventLatest());
    }
}
