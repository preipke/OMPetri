/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.child;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisElement;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisSubElement;
import edu.unibi.agbi.gravisfx.graph.entity.util.ElementHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisSubRectangle extends Rectangle implements IGravisSubElement
{
    private final List<ElementHandle> elementHandles;
    private final IGravisNode parentElement;
    
    public GravisSubRectangle(IGravisNode parentNode) {
        
        super();
        
        this.parentElement = parentNode;
        
        elementHandles = new ArrayList();
        elementHandles.add(new ElementHandle(this));
        
        setWidth(GravisProperties.RECTANGLE_WIDTH - GravisProperties.BASE_INNER_DISTANCE * 2);
        setHeight(GravisProperties.RECTANGLE_HEIGHT - GravisProperties.BASE_INNER_DISTANCE * 2);
        setArcWidth(GravisProperties.RECTANGLE_ARC_WIDTH - GravisProperties.BASE_INNER_DISTANCE * 2);
        setArcHeight(GravisProperties.RECTANGLE_ARC_HEIGHT - GravisProperties.BASE_INNER_DISTANCE * 2);
    }
    
    @Override
    public IGravisElement getParentElement() {
        return parentElement;
    }

    @Override
    public Object getBean() {
        return GravisSubRectangle.this;
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
}
