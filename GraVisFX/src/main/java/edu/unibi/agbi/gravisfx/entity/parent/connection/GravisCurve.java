/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.connection;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.util.ElementHandle;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCurve extends QuadCurve implements IGravisConnection
{
    private final List<ElementHandle> elementHandles = new ArrayList();
    
    private final IGravisNode source;
    private final IGravisNode target;
    
    public GravisCurve(IGravisNode source, IGravisNode target) {
        
        super();
        
        elementHandles.add(new ElementHandle(this));
        
        this.source = source;
        this.target = target;
        
        startXProperty().bind(source.translateXProperty().add(source.getOffsetX()));
        startYProperty().bind(source.translateYProperty().add(source.getOffsetY()));
        
        endXProperty().bind(target.translateXProperty().add(target.getOffsetX()));
        endYProperty().bind(target.translateYProperty().add(target.getOffsetY()));
        
        final DoubleProperty sourceXProperty = source.translateXProperty();
        final DoubleProperty sourceYProperty = source.translateYProperty();
        
        final DoubleProperty targetXProperty = target.translateXProperty();
        final DoubleProperty targetYProperty = target.translateYProperty();
        
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
                double r = GravisProperties.ARC_GAP / 2;
                
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
    public Object getBean() {
        return GravisCurve.this;
    }

    @Override
    public List<ElementHandle> getElementHandles() {
        return elementHandles;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
    
    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        return shapes;
    }
    
    @Override
    public IGravisNode getSource() {
        return source;
    }
    
    @Override
    public IGravisNode getTarget() {
        return target;
    }
}
