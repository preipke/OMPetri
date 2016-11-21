/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.view.handler;

import edu.unibi.agbi.gravisfx.graph.entity.node.IGravisNode;
import edu.unibi.agbi.gravisfx.view.GraphPane;
import javafx.geometry.Bounds;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 *
 * @author PR
 */
public class MouseGestures
{
    //private static double scaleValue = 1.0;
    //private static double scaleDelta = 0.1;
    private static double scaleBase = 1.0;
    private static double scaleFactor = 1.1;
    private static int scalePowerFactor = 0;
    
    private static double SCALE_MAX = 10.d;
    private static double SCALE_MIN = .01d;
    
    private static Double eventMousePressedX = null;
    private static Double eventMousePressedY = null;
    
    /**
     * Registers several mouse and scroll event handlers.
     * @param graphPane 
     */
    public static void register(GraphPane graphPane) {
        
        graphPane.setOnScroll(( ScrollEvent event ) -> {
            
            if (event.getDeltaY() > 0) { // zoom in
                scalePowerFactor++;
            } else { // zoom out
                scalePowerFactor--;
            }
            
            double oldScale = graphPane.getTopLayer().getScaleX();
            double scale = scaleBase * Math.pow(scaleFactor , scalePowerFactor);
            
            graphPane.getTopLayer().getScaleTransform().setX(scale);
            graphPane.getTopLayer().getScaleTransform().setY(scale);
            
            // NEW
            /*if (Double.compare(scale , SCALE_MIN) < 0) {
                scale = SCALE_MIN;
            } else if (Double.compare(scale , SCALE_MAX) > 0) {
                scale = SCALE_MAX;
            }
            
            double f = (scale / oldScale) - 1;
            Bounds bounds = graphPane.getTopLayer().localToScene(graphPane.getTopLayer().getBoundsInLocal());
            double dx = (event.getX() - (bounds.getWidth() / 2 + bounds.getMinX()));
            double dy = (event.getY() - (bounds.getHeight() / 2 + bounds.getMinY()));
            
            System.out.println("f=" + f);
            System.out.println("dx=" + dx);
            System.out.println("dy=" + dy);
            
            graphPane.getTopLayer().setTranslateX(graphPane.getTopLayer().getTranslateX() + f * dx);
            graphPane.getTopLayer().setTranslateY(graphPane.getTopLayer().getTranslateY() + f * dy);
            
            System.out.println("translateX =" + f * dx);
            System.out.println("translateY =" + f * dy);
            */
            
            /*
            double translateX, translateY;
            
            if (event.getDeltaY() > 0) { // zoom in
                
                translateX = event.getX() - event.getX() * scale;
                translateY = event.getY() - event.getY() * scale;
                
            } else { // zoom out
                
                translateX = event.getX() - event.getX() / scale;
                translateY = event.getY() - event.getY() / scale;
            }

            graphPane.getTopLayer().setTranslateX(graphPane.getTopLayer().translateXProperty().get() + translateX);
            graphPane.getTopLayer().setTranslateY(graphPane.getTopLayer().translateYProperty().get() + translateY);
            */
        });

        graphPane.setOnMousePressed(( MouseEvent event ) -> {
            eventMousePressedX = event.getX();
            eventMousePressedY = event.getY();
            System.out.println("Event: X=" + event.getX() + " Y=" + event.getY());
            System.out.println("TopLayer Translate: X=" + ((GraphPane)event.getTarget()).getTopLayer().getTranslateX()+ " Y=" + ((GraphPane)event.getTarget()).getTopLayer().getTranslateX() );
            System.out.println("Scale: " + scaleBase * Math.pow(scaleFactor , scalePowerFactor));
        });

        graphPane.setOnMouseReleased(( MouseEvent event ) -> {
            eventMousePressedX = null;
            eventMousePressedY = null;
        });

        graphPane.setOnMouseDragged(( MouseEvent event ) -> {
            if (IGravisNode.class.isAssignableFrom(event.getTarget().getClass())) {

                IGravisNode node = (IGravisNode)event.getTarget();
                node.setPosition(
                        (event.getX() - graphPane.getTopLayer().translateXProperty().get()) / graphPane.getTopLayer().getScaleTransform().getX() ,
                        (event.getY() - graphPane.getTopLayer().translateYProperty().get()) / graphPane.getTopLayer().getScaleTransform().getX()
                );
            } else if (GraphPane.class.isAssignableFrom(event.getTarget().getClass())) {

                graphPane.getTopLayer().setTranslateX((event.getX() - eventMousePressedX + graphPane.getTopLayer().translateXProperty().get()));
                graphPane.getTopLayer().setTranslateY((event.getY() - eventMousePressedY + graphPane.getTopLayer().translateYProperty().get()));

                eventMousePressedX = event.getX();
                eventMousePressedY = event.getY();
            }
        });
    }
}
