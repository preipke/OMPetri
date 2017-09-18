/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.root.IGravisRoot;

/**
 *
 * @author PR
 */
public class GravisChildArrow extends Path implements IGravisChild
{
    private final List<GravisShapeHandle> elementHandles;
    private final IGravisRoot parentElement;

    public GravisChildArrow(IGravisRoot parentElement) {

        super();

        this.parentElement = parentElement;

        elementHandles = new ArrayList();
        elementHandles.add(new GravisShapeHandle(this));

        getElements().add(new MoveTo(GravisProperties.ARROW_WIDTH / 3d, GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0, 0));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH, GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0, GravisProperties.ARROW_HEIGHT));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH / 3d, GravisProperties.ARROW_HEIGHT / 2d));
    }

    @Override
    public final double getCenterOffsetX() {
        return 0;
    }

    @Override
    public final double getCenterOffsetY() {
        return 0;
    }

    @Override
    public List<GravisShapeHandle> getElementHandles() {
        return elementHandles;
    }

    @Override
    public IGravisRoot getParentShape() {
        return parentElement;
    }

    @Override
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        return shapes;
    }
}
