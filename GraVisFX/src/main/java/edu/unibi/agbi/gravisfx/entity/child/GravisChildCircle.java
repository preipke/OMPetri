/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.entity.parent.IGravisParent;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisChildCircle extends Circle implements IGravisChild
{
    private final List<GravisShapeHandle> elementHandles;
    private final IGravisParent parentElement;

    public GravisChildCircle(IGravisParent parentElement) {

        super();

        this.parentElement = parentElement;

        this.elementHandles = new ArrayList();
        this.elementHandles.add(new GravisShapeHandle(this));
    }

    @Override
    public IGravisParent getParentShape() {
        return parentElement;
    }

    @Override
    public Object getBean() {
        return GravisChildCircle.this;
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

    @Override
    public final double getCenterOffsetX() {
        return 0;
    }

    @Override
    public final double getCenterOffsetY() {
        return 0;
    }
}
