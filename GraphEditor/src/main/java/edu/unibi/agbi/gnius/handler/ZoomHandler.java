/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.handler;

import edu.unibi.agbi.gravisfx.presentation.GraphPane;
import javafx.scene.input.ScrollEvent;

/**
 *
 * @author PR
 */
public class ZoomHandler
{
    private static final double scaleBase = 1.0;
    private static final double scaleFactor = 1.1;
    private static int scalePower = 0;
    
    private static final double SCALE_MAX = 10.d;
    private static final double SCALE_MIN = .01d;
    
    public static void applyZoomHandler(GraphPane graphPane) {
        
        graphPane.setOnScroll(( ScrollEvent event ) -> {
            
            double scale_t1, scale_t0;
            
            scale_t0 = scaleBase * Math.pow(scaleFactor , scalePower);
            
            if (event.getDeltaY() > 0) {
                scalePower++;
            } else {
                scalePower--;
            }
            
            scale_t1 = scaleBase * Math.pow(scaleFactor , scalePower);
            
            graphPane.getTopLayer().getScaleTransform().setX(scale_t1);
            graphPane.getTopLayer().getScaleTransform().setY(scale_t1);
            
            /**
             * Following is used to make sure focus is kept on the mouse pointer location.
             * TODO 
             * zooming out feels not perfect yet, find a solution.
             * apply max / min zoom.
             */
            double startX, startY, endX, endY;
            double translateX, translateY;
            
            startX = event.getX() - graphPane.getTopLayer().translateXProperty().get();
            startY = event.getY() - graphPane.getTopLayer().translateYProperty().get();
            
            if (event.getDeltaY() > 0) { // zoom in
                
                endX = startX * scale_t1 / scale_t0;
                endY = startY * scale_t1 / scale_t0;
                
                translateX = startX - endX;
                translateY = startY - endY;
                
            } else { // zoom out
                
                endX = startX * scale_t0 / scale_t1;
                endY = startY * scale_t0 / scale_t1;
                
                translateX = endX - startX;
                translateY = endY - startY;
            }
            /*
            System.out.println("Scale t_0: " + scale_t0);
            System.out.println("Scale t_1: " + scale_t1);
            System.out.println("P(t_0) | X=" + startX + " Y=" + startY);
            System.out.println("P(t_1) | X=" + endX + " Y=" + endY);
            System.out.println("translate| X=" + translateX);
            System.out.println("translate| Y=" + translateY);
            System.out.println("");
            */
            graphPane.getTopLayer().setTranslateX(graphPane.getTopLayer().translateXProperty().get() + translateX);
            graphPane.getTopLayer().setTranslateY(graphPane.getTopLayer().translateYProperty().get() + translateY);
        });
    }
}
