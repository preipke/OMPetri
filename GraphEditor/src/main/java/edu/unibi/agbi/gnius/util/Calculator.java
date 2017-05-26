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
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import java.util.Collection;
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
     * Computes center.
     *
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
        return new Point2D(x, y);
    }

    /**
     * Computes center.
     *
     * @param nodes
     * @return
     */
    public Point2D getNodeCenter(Collection<IGravisNode> nodes) {
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

    /**
     * Calculates the factor describing the difference in pane size and required
     * node space.
     *
     * @param graph
     * @param pane
     * @return
     */
    public synchronized double getScaleDifference(Graph graph, GraphPane pane) {

        double max_X = Double.MIN_VALUE;
        double min_X = Double.MAX_VALUE;
        double max_Y = Double.MIN_VALUE;
        double min_Y = Double.MAX_VALUE;

        for (IGravisNode node : graph.getNodes()) {
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

        double scaleX = pane.getWidth() * 5/6 / (max_X - min_X); // adjusted pane size divided by required nodes space
        double scaleY = pane.getHeight() * 3/4 / (max_Y - min_Y);

        if (scaleX < scaleY) {
            return scaleX;
        } else {
            return scaleY;
        }
    }
}
