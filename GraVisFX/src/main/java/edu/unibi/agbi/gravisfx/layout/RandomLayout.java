/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.layout;

import edu.unibi.agbi.gravisfx.graph.Graph;
import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import java.util.List;
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

        IGravisNode[] nodes = graph.getNodes();
        
        Random rnd = new Random();

        for (IGravisNode node : nodes) {

            double x = rnd.nextDouble() * 500;
            double y = rnd.nextDouble() * 500;

            node.relocate(x, y);
        }
    }
}
