/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.util;

import edu.unibi.agbi.gnius.business.handler.MouseEventHandler;
import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphNode;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import java.util.List;
import javafx.geometry.Point2D;
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
    
    /**
     * Computes center of weight.
     * @param nodes
     * @return 
     */
    public Point2D getCenter(List<IGraphNode> nodes) {
        double x = 0, y = 0;
        for (IGravisNode node : nodes) {
            x += node.translateXProperty().get();
            y += node.translateYProperty().get();
        }
        x = x / nodes.size();
        y = y / nodes.size();
        return new Point2D(x , y);
    }
    
    public Point2D getCorrectedMousePosition(Graph graph, double posX, double posY) {
        double x, y;
        x = (posX - graph.getTopLayer().translateXProperty().get()) / graph.getTopLayer().getScale().getX();
        y = (posY - graph.getTopLayer().translateYProperty().get()) / graph.getTopLayer().getScale().getY();
        return new Point2D(x, y);
    }
    
    public Point2D getCorrectedMousePositionLatest(Graph graph) {
        return getCorrectedMousePosition(
                graph, 
                mouseEventHandler.getMouseMovedEventLatest().getX(), 
                mouseEventHandler.getMouseMovedEventLatest().getY()
        );
    }
}
