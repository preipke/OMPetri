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
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import java.util.Collection;
import javafx.geometry.Point2D;
import javafx.scene.transform.Scale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author PR
 */
@Component
public class Calculator
{
    @Autowired private MouseEventHandler mouseEventHandler;

    private final int gridSize = 10;
    
    public Point2D getPositionInGrid(Point2D point, Scale scale) {
        
        double x, y;
        
        x = point.getX() * scale.getX();
        y = point.getY() * scale.getY();
        
        if (x % gridSize / gridSize > 0.5) {
            x = x - x % gridSize + gridSize;
        } else {
            x = x - x % gridSize;
        }
        
        if (y % gridSize / gridSize > 0.5) {
            y = y - y % gridSize + gridSize;
        } else {
            y = y - y % gridSize;
        }
        
        x = x / scale.getX();
        y = y / scale.getY();
        
        return new Point2D(x, y);
    }

    public Point2D getCenter(Collection<IGravisNode> nodes) {
        double x = 0, y = 0;
        for (IGravisNode node : nodes) {
            x += node.translateXProperty().get();
            y += node.translateYProperty().get();
        }
        x = x / nodes.size();
        y = y / nodes.size();
        return new Point2D(x, y);
    }

    public Point2D getCenterN(Collection<IGraphNode> nodes) {
        double x = 0, y = 0;
        for (IGravisNode node : nodes) {
            x += node.translateXProperty().get();
            y += node.translateYProperty().get();
        }
        x = x / nodes.size();
        y = y / nodes.size();
        return new Point2D(x, y);
    }

    public Point2D getCorrectedMousePosition(Graph graph, double posX, double posY) {
        double x, y;
        x = (posX - graph.translateXProperty().get()) / graph.getScale().getX();
        y = (posY - graph.translateYProperty().get()) / graph.getScale().getY();
        return new Point2D(x, y);
    }

    public Point2D getCorrectedMousePositionLatest(Graph graph) {
        return getCorrectedMousePosition(
                graph,
                mouseEventHandler.getMouseMovedEventLatest().getX(),
                mouseEventHandler.getMouseMovedEventLatest().getY()
        );
    }

    /**
     * Computes and gets the optimal scale factor for the graph present in the
     * given graph pane to fit all nodes within current window size.
     *
     * @param pane
     * @return
     */
    public synchronized double getOptimalScale(GraphPane pane) {

        double max_X = Double.MIN_VALUE;
        double min_X = Double.MAX_VALUE;
        double max_Y = Double.MIN_VALUE;
        double min_Y = Double.MAX_VALUE;

        for (IGravisNode node : pane.getGraph().getNodes()) {
            if (max_X < node.translateXProperty().get()) {
                max_X = node.translateXProperty().get();
            }
            if (min_X > node.translateXProperty().get()) {
                min_X = node.translateXProperty().get();
            }
            if (max_Y < node.translateYProperty().get()) {
                max_Y = node.translateYProperty().get();
            }
            if (min_Y > node.translateYProperty().get()) {
                min_Y = node.translateYProperty().get();
            }
        }

        double scaleX = pane.getWidth() * 2 / 3 / (max_X - min_X); // adjusted pane size divided by required nodes space
        double scaleY = pane.getHeight() * 3 / 4 / (max_Y - min_Y);

        if (scaleX < scaleY) {
            return scaleX;
        } else {
            return scaleY;
        }
    }
}
