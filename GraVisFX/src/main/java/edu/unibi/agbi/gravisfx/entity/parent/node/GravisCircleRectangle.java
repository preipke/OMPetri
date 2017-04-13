/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.node;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.child.GravisSubRectangle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisChildElement;

/**
 *
 * @author PR
 */
public class GravisCircleRectangle extends GravisCircle {

    private final GravisSubRectangle innerRect;

    public GravisCircleRectangle() {

        super();

        double x1 = translateXProperty().get();
        double y1 = translateYProperty().get();
        
        double b = y1 + x1;
        double r = getRadius();
        double p = 2 * (y1 - x1 - b) / 2;
        double q = (x1 * x1 + b * b + y1 * y1 - 2 * b * y1 - r * r) / 2;
        
        double x2 = -p / 2 + Math.sqrt(p * p / 4 - q);
        double y2 = - x2 + b;
        
        double offsetX = Math.abs(x1-x2);
        double offsetY = Math.abs(y1-y2);

        innerRect = new GravisSubRectangle(this);
        innerRect.translateXProperty().bind(translateXProperty().subtract(offsetX));
        innerRect.translateYProperty().bind(translateYProperty().subtract(offsetY));
        innerRect.setArcWidth(GravisProperties.RECTANGLE_ARC_WIDTH);
        innerRect.setArcHeight(GravisProperties.RECTANGLE_ARC_HEIGHT);
        innerRect.setWidth(offsetX * 2);
        innerRect.setHeight(offsetY * 2);

        getElementHandles().add(innerRect.getElementHandles().get(0));
    }

    @Override
    public Object getBean() {
        return GravisCircleRectangle.this;
    }

    @Override
    public Shape getShape() {
        return this;
    }

    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        shapes.add(innerRect);
        return shapes;
    }

    @Override
    public List<IGravisChildElement> getChildElements() {
        List<IGravisChildElement> childElements = new ArrayList();
        childElements.add(innerRect);
        return childElements;
    }
}
