/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.editor.core.util;

import edu.unibi.agbi.editor.core.data.entity.graph.IGraphElement;
import edu.unibi.agbi.editor.core.data.entity.graph.IGraphNode;
import edu.unibi.agbi.editor.presentation.handler.MouseEventHandler;
import edu.unibi.agbi.gravisfx.entity.root.node.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.GraphPane;
import javafx.geometry.Point2D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 *
 * @author PR
 */
@Component
public class Calculator
{
    @Autowired private MouseEventHandler mouseEventHandler;

    private final double gridSize = 10;
    
    public Point2D getPositionInGrid(Point2D pos, Graph graph) {
        
        double posFinalX, posFinalY, marginX, marginY;
        
        posFinalX = pos.getX();
        posFinalY = pos.getY();
        
        marginX = posFinalX % gridSize;
        marginY = posFinalY % gridSize;
        
        if (marginX / gridSize > 0.5) {
            posFinalX = posFinalX - marginX + gridSize;
        } else {
            posFinalX = posFinalX - marginX;
        }

        if (marginY / gridSize > 0.5) {
            posFinalY = posFinalY - marginY + gridSize;
        } else {
            posFinalY = posFinalY - marginY;
        }
        
        return new Point2D(posFinalX, posFinalY);
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

    public Point2D getCenterE(Collection<IGraphElement> elements) {
        double x = 0, y = 0;
        for (IGraphElement elem : elements) {
            x += elem.translateXProperty().get();
            y += elem.translateYProperty().get();
        }
        x = x / elements.size();
        y = y / elements.size();
        return new Point2D(x, y);
    }

    public Point2D getCorrectedPosition(Graph graph, double posX, double posY) {
        double x, y;
        x = (posX - graph.translateXProperty().get()) / graph.getScale().getX();
        y = (posY - graph.translateYProperty().get()) / graph.getScale().getY();
        return new Point2D(x, y);
    }

    public Point2D getCorrectedMousePositionLatest(Graph graph) {
        return getCorrectedPosition(
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
        
        if (pane.getGraph().getNodes().isEmpty()) {
            return 1;
        }

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
        
        if (max_X - min_X == 0) {
            return 1;
        } else if (max_Y - min_Y == 0) {
            return 1;
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
