/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.parent.node;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisSubElement;
import edu.unibi.agbi.gravisfx.graph.entity.child.GravisSubRectangle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisRectangleDouble extends GravisRectangle {
    
    private final GravisSubRectangle innerRectangle;
    
    public GravisRectangleDouble() {
        
        super();
        
        innerRectangle = new GravisSubRectangle(this);
        innerRectangle.translateXProperty().bind(translateXProperty().add(GravisProperties.BASE_INNER_DISTANCE));
        innerRectangle.translateYProperty().bind(translateYProperty().add(GravisProperties.BASE_INNER_DISTANCE));
        
        getElementHandles().add(innerRectangle.getElementHandles().get(0));
    }
    
    @Override
    public Object getBean() {
        return GravisRectangleDouble.this;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
    
    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        shapes.add(innerRectangle);
        return shapes;
    }
    
    @Override
    public List<IGravisSubElement> getChildElements() {
        List<IGravisSubElement> childElements = new ArrayList();
        childElements.add(innerRectangle);
        return childElements;
    }
}
