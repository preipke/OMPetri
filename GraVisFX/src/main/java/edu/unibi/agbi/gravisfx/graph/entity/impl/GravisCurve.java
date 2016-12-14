/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.impl;

import edu.unibi.agbi.gravisfx.PropertiesController;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCurve extends QuadCurve implements IGravisEdge
{
    private final IGravisNode source;
    private final IGravisNode target;
    
    private static final double ARC_GAP = PropertiesController.ARC_GAP;
    
    public GravisCurve(IGravisNode source, IGravisNode target) {
        
        super();
        
        this.source = source;
        this.target = target;
        
        startXProperty().bind(source.getShape().translateXProperty().add(source.getOffsetX()));
        startYProperty().bind(source.getShape().translateYProperty().add(source.getOffsetY()));
        
        endXProperty().bind(target.getShape().translateXProperty().add(target.getOffsetX()));
        endYProperty().bind(target.getShape().translateYProperty().add(target.getOffsetY()));
        
        final DoubleProperty sourceXProperty = source.getShape().translateXProperty();
        final DoubleProperty sourceYProperty = source.getShape().translateYProperty();
        
        final DoubleProperty targetXProperty = target.getShape().translateXProperty();
        final DoubleProperty targetYProperty = target.getShape().translateYProperty();
        
        /**
         * Control point X coordinate.
         */
        DoubleBinding bindingControlX = new DoubleBinding()
        {
            {
                super.bind(sourceXProperty, targetXProperty);
            }
            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get();
                double y1 = sourceYProperty.get();

                double x2 = targetXProperty.get();
                double y2 = targetYProperty.get();
                
                double x3, y3;
                x3 = (x1 + x2) / 2;
                y3 = (y1 + y2) / 2;
                
//                System.out.println("Computing Curve:");
//                System.out.println("P1 = ( " + x1 + " | " + y1 + " )");
//                System.out.println("P2 = ( " + x2 + " | " + y2 + " )");
//                System.out.println("-> P3 = ( " + x3 + " | " + y3 + " )");
                
                double m1 = (y2 - y1) / (x2 - x1);
                double m2 = - 1 / m1;
                
                double angle = Math.atan(m2);
                
                // mittels pythagoras abschnitt auf x und y dazu addieren.
                // länge bestimmen
                
                double xGap = Math.cos(angle) * ARC_GAP;
                double yGap = Math.sin(angle) * ARC_GAP;
                
                x3 = x3 + xGap;
                y3 = y3 + yGap;
                
//                System.out.println("---> P3 = ( " + x3 + " | " + y3 + " )");
                
                return x3;
            }
        };
        controlXProperty().bind(bindingControlX);
        
        /**
         * Control point Y coordinate.
         */
        DoubleBinding bindingControlY = new DoubleBinding()
        {
            {
                super.bind(sourceYProperty, targetYProperty);
            }
            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get();
                double y1 = sourceYProperty.get();

                double x2 = targetXProperty.get();
                double y2 = targetYProperty.get();
                
                double y3 = (y1 + y2) / 2;
                
                double m1 = (y2 - y1) / (x2 - x1);
                double m2 = - 1 / m1;
                
                double angle = Math.abs(Math.atan(m2));
                
                double yGap = Math.sin(angle) * ARC_GAP / 2;
                
                y3 = y3 + yGap;
                
                return y3;
            }
        };
        controlYProperty().bind(bindingControlY);
    }
    
    
    @Override
    public IGravisNode getSource() {
        return source;
    }
    
    @Override
    public IGravisNode getTarget() {
        return target;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
}
