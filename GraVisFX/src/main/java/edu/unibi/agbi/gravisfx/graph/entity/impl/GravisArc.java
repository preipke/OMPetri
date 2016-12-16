/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.impl;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisEdge;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
@Deprecated
public class GravisArc extends Arc implements IGravisEdge
{
    private final IGravisNode source;
    private final IGravisNode target;
    
    private static final double arcDegreeAngle = 35;
    private static final double arcRadianAngle = Math.PI * 2 / 360 * arcDegreeAngle;
    
    public GravisArc(IGravisNode source, IGravisNode target) {
        
        super();
        
        setStartAngle(arcDegreeAngle);
        setType(ArcType.CHORD);
        
        this.source = source;
        this.target = target;
        
        final DoubleProperty sourceXProperty = source.getShape().translateXProperty();
        final DoubleProperty sourceYProperty = source.getShape().translateYProperty();
        
        final DoubleProperty targetXProperty = target.getShape().translateXProperty();
        final DoubleProperty targetYProperty = target.getShape().translateYProperty();
        
        final DoubleProperty lengtProperty = lengthProperty();
        
        final DoubleProperty radiusXProperty = radiusXProperty();
        final DoubleProperty radiusYProperty = radiusYProperty();
        
        final DoubleProperty centerXProperty = centerXProperty();
        final DoubleProperty centerYProperty = centerYProperty();
        
        /**
         * Compute radius.
         */
        DoubleBinding radiusBinding = new DoubleBinding() {
            
            {
                super.bind(sourceXProperty , sourceYProperty , targetXProperty , targetYProperty);
            }

            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get();
                double y1 = sourceYProperty.get();

                double x2 = targetXProperty.get();
                double y2 = targetYProperty.get();
                
                double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                double radius = distance / 2 / Math.sin(arcRadianAngle / 2);
                
//                System.out.println("Computing Arc:");
//                System.out.println("P1 = ( " + x1 + " | " + y1 + " )");
//                System.out.println("P2 = ( " + x2 + " | " + y2 + " )");
//                System.out.println("Distance = " + distance);
//                System.out.println("Radius = " + radius);
                return radius;
            }
        };
        radiusXProperty.bind(radiusBinding);
        radiusYProperty.bind(radiusBinding);
        
        /**
         * Compute center x position.
         */
        DoubleBinding centerXBinding = new DoubleBinding()
        {
            {
                super.bind(radiusXProperty);
            }

            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get();
                double y1 = sourceYProperty.get();

                double x2 = targetXProperty.get();
                double y2 = targetYProperty.get();

                double radius = radiusXProperty.get();

                double a = y2 - y1;
                double b = x1 * x1 - x2 * x2 + y1 * y1 - y2 * y2;
                double m = x1 - x2;

                double div = 1 + (m * m) / (a * a);

                double p = (x1 + m * b / (a * a) + m * y1 / a) / div;
                double q = (x1 - radius * radius + (b / a + y1) * (b / a + y1)) / div;

                return (-p / 2) + Math.sqrt((p * p / 4) - q);
            }
        };
        centerXProperty.bind(centerXBinding);
        
        /**
         * Compute center y position.
         */
        DoubleBinding centerYBinding = new DoubleBinding()
        {
            {
                super.bind(centerXProperty);
            }

            @Override
            protected double computeValue() {

                double x1 = sourceXProperty.get();
                double y1 = sourceYProperty.get();

                double radius = radiusXProperty.get();
                double centerX = centerXProperty.get();

                double p = y1;
                double q = y1 * y1 - radius * radius + centerX * centerX - centerX * x1 - x1 * x1;

//                System.out.println("P3 = ( " + centerX + " | " + (-p / 2 + Math.sqrt((p * p / 4) - q)) + " ) - Circle center");
                return (-p / 2) + Math.sqrt((p * p / 4) - q);
            }
        };
        centerYProperty.bind(centerYBinding);
        
        /**
         * Compute length.
         */
        DoubleBinding lengthBinding = new DoubleBinding() {

            {
                super.bind(centerYBinding);
            }

            @Override
            protected double computeValue() {
                return 2 * arcDegreeAngle * radiusXProperty.get();
            }
        };
        lengtProperty.bind(lengthBinding);
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
