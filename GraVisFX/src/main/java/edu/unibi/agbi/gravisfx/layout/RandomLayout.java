/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.layout;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.entity.node.GravisNode;
import edu.unibi.agbi.gravisfx.pane.GraphPane;
import java.util.Random;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public class RandomLayout
{
    /**
     * Applies a random layout on the given graph.
     * @param graph 
     */
    public static void applyOn(Graph graph) {
        
        GraphPane graphPane = (GraphPane) graph.getTopLayer().getParent();
        Scale scaling = graph.getScaling();
        
        double layoutX = graphPane.getWidth();
        double layoutY = graphPane.getHeight();

        GravisNode[] nodes = graph.getNodes();
        
        Random rnd = new Random();

        for (GravisNode node : nodes) {

            double x = rnd.nextDouble() * layoutX;
            double y = rnd.nextDouble() * layoutY;

            node.setPosition(x, y);
            node.setScale(scaling);
        }
    }
}
