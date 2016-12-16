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
                super.bind(sourceXProperty, targetXProperty, sourceYProperty, targetYProperty);
            }
            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get() + source.getOffsetX();
                double y1 = sourceYProperty.get() + source.getOffsetY();

                double x2 = targetXProperty.get() + target.getOffsetX();
                double y2 = targetYProperty.get() + target.getOffsetY();
                
                double x3, y3;
                x3 = (x1 + x2) / 2 ;
                y3 = (y1 + y2) / 2 ;
                
                double x = (x1 - x2);
                double y = (y2 - y1);
                
                if (x < 1 && x > -1) {
                    if (x >= 0) {
                        x = 1;
                    } else {
                        x = -1;
                    }
                }
                
                if (y < 1 && y > -1) {
                    if (y >= 0) {
                        y = 1;
                    } else {
                        y = -1;
                    }
                }
                    
                double m2 = x / y;
                
                double b2 = y3 - m2 * x3;
                double r = ARC_GAP / 2;
                
//                System.out.println("####");
//                System.out.println("P1 ( " + x1 + " | " + y1 + " )");
//                System.out.println("P2 ( " + x2 + " | " + y2 + " )");
//                System.out.println("m2 = " + m2);
//                System.out.println("b2 = " + b2);
                
                double p = 2 * (m2 * b2 - m2 * y3 - x3) / (1 + m2 * m2);
                double q = (x3 * x3 + b2 * b2 + y3 * y3 - 2* b2 * y3 - r * r) / (1 + m2 * m2);
                
//                System.out.println("p = " + p);
//                System.out.println("q = " + q);
                
                double x4;
                
                if (y2 >= y1) {
                    x4 = - p / 2 + Math.sqrt(p * p / 4 - q);
                } else {
                    x4 = - p / 2 - Math.sqrt(p * p / 4 - q);
                }
                
                return x4;
            }
        };
        controlXProperty().bind(bindingControlX);
        
        /**
         * Control point Y coordinate.
         */
        DoubleBinding bindingControlY = new DoubleBinding()
        {
            {
                super.bind(controlXProperty());
            }
            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get() + source.getOffsetX();
                double y1 = sourceYProperty.get() + source.getOffsetY();

                double x2 = targetXProperty.get() + target.getOffsetX() + 0.0001;
                double y2 = targetYProperty.get() + target.getOffsetY() - 0.0001;
                
                double x = (x1 - x2);
                double y = (y2 - y1);
                
                if (x < 1 && x > -1) {
                    if (x >= 0) {
                        x = 1;
                    } else {
                        x = -1;
                    }
                }
                
                if (y < 1 && y > -1) {
                    if (y >= 0) {
                        y = 1;
                    } else {
                        y = -1;
                    }
                }
                    
                double m2 = x / y;
                
                return m2 * controlXProperty().get() + ((y1 + y2) - m2 * (x1 + x2)) / 2;
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

    @Override
    public void setTranslate(double positionX , double positionY) {
    }
}
