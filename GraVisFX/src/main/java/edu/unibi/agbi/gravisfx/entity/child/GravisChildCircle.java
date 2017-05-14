/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.entity.IGravisElement;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisChild;

/**
 *
 * @author PR
 */
public class GravisChildCircle extends Circle implements IGravisChild
{
    private final List<GravisShapeHandle> elementHandles;
    private final IGravisElement parentElement;

    public GravisChildCircle(IGravisElement parentElement) {

        super();

        this.parentElement = parentElement;

        this.elementHandles = new ArrayList();
        this.elementHandles.add(new GravisShapeHandle(this));
    }

    @Override
    public IGravisElement getParentShape() {
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
}
