/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.child;

import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import edu.unibi.agbi.gravisfx.entity.root.IGravisRoot;

/**
 *
 * @author PR
 */
public class GravisChildLabel extends Text implements IGravisChild
{
    private final List<GravisShapeHandle> elementHandles;
    private final IGravisRoot parentElement;

    public GravisChildLabel(IGravisRoot parentElement) {

        super();

        this.parentElement = parentElement;

        elementHandles = new ArrayList();
        elementHandles.add(new GravisShapeHandle(this));
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
