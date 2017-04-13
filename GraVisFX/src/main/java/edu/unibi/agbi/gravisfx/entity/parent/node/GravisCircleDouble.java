/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.node;

import edu.unibi.agbi.gravisfx.entity.child.GravisSubCircle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisChildElement;

/**
 *
 * @author PR
 */
public class GravisCircleDouble extends GravisCircle {
    
    private final GravisSubCircle innerCircle;
    
    public GravisCircleDouble() {
        
        super();
        
        innerCircle = new GravisSubCircle(this);
        innerCircle.translateXProperty().bind(translateXProperty());
        innerCircle.translateYProperty().bind(translateYProperty());
        
        getElementHandles().add(innerCircle.getElementHandles().get(0));
    }
    
    @Override
    public Object getBean() {
        return GravisCircleDouble.this;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
    
    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        shapes.add(innerCircle);
        return shapes;
    }
    
    @Override
    public List<IGravisChildElement> getChildElements() {
        List<IGravisChildElement> childElements = new ArrayList();
        childElements.add(innerCircle);
        return childElements;
    }
}
