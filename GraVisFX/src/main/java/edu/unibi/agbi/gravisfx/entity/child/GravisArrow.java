/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisElement;
import edu.unibi.agbi.gravisfx.entity.util.ElementHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisChildElement;

/**
 *
 * @author PR
 */
public class GravisArrow extends Path implements IGravisChildElement
{
    private final List<ElementHandle> elementHandles;
    private final IGravisConnection parentElement;

    public GravisArrow(IGravisConnection parentElement) {

        super();

        this.parentElement = parentElement;

        elementHandles = new ArrayList();
        elementHandles.add(new ElementHandle(this));

        getElements().add(new MoveTo(GravisProperties.ARROW_WIDTH / 3d, GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0, 0));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH, GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0, GravisProperties.ARROW_HEIGHT));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH / 3d, GravisProperties.ARROW_HEIGHT / 2d));
    }

    @Override
    public IGravisElement getParentElement() {
        return parentElement;
    }

    @Override
    public Object getBean() {
        return GravisArrow.this;
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
