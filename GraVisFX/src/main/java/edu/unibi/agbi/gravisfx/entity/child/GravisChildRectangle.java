/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.IGravisElement;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisChild;

/**
 *
 * @author PR
 */
public class GravisChildRectangle extends Rectangle implements IGravisChild
{
    private final List<GravisShapeHandle> elementHandles;
    private final IGravisNode parentElement;

    public GravisChildRectangle(IGravisNode parentNode) {

        super();

        this.parentElement = parentNode;

        elementHandles = new ArrayList();
        elementHandles.add(new GravisShapeHandle(this));

        setWidth(GravisProperties.RECTANGLE_WIDTH - GravisProperties.BASE_INNER_DISTANCE * 2);
        setHeight(GravisProperties.RECTANGLE_HEIGHT - GravisProperties.BASE_INNER_DISTANCE * 2);
        setArcWidth(GravisProperties.RECTANGLE_ARC_WIDTH - GravisProperties.BASE_INNER_DISTANCE * 2);
        setArcHeight(GravisProperties.RECTANGLE_ARC_HEIGHT - GravisProperties.BASE_INNER_DISTANCE * 2);
    }

    @Override
    public IGravisElement getParentShape() {
        return parentElement;
    }

    @Override
    public Object getBean() {
        return GravisChildRectangle.this;
    }

    @Override
    public List<GravisShapeHandle> getElementHandles() {
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
