/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.util;

import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;

import javafx.geometry.Point2D;

/**
 *
 * @author PR
 */
public class Calculator
{
    public static Point2D getCenter(IGravisNode[] nodes) {
        
        double x = 0, y = 0;
        
        for (IGravisNode node : nodes) {
            x += node.getTranslateX();
            y += node.getTranslateY();
        }
        
        x = x / nodes.length;
        y = y / nodes.length;
        
        return new Point2D(x , y);
    }
}
