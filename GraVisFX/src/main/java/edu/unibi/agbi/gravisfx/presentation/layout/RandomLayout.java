/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.presentation.layout;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import java.util.Random;

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
        
        double layoutX = graphPane.getWidth();
        double layoutY = graphPane.getHeight();
        
        double scale = graphPane.getTopLayer().getScale().getX();

        IGravisNode[] nodes = graph.getNodes();
        
        Random rnd = new Random();
        
        graph.getTopLayer().setTranslateX(0);
        graph.getTopLayer().setTranslateY(0);

        for (IGravisNode node : nodes) {

            double x = rnd.nextDouble() * layoutX / scale;
            double y = rnd.nextDouble() * layoutY / scale;

            node.translateXProperty().set(x);
            node.translateYProperty().set(y);
        }
    }
}
