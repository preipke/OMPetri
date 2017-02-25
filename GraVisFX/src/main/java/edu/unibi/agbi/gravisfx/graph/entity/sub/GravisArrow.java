/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.sub;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.graph.entity.abst.GravisElementHandle;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisElement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisArrow extends Path implements IGravisElement,IGravisSubElement
{
    private final List<GravisElementHandle> elementHandles = new ArrayList();
    private final IGravisElement parentElement;
    
    public GravisArrow(IGravisElement parentElement) {
        
        super();
        
        this.parentElement = parentElement;
        
        elementHandles.add(new GravisElementHandle(this));
        
        getElements().add(new MoveTo(GravisProperties.ARROW_WIDTH / 3d , GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0 , 0));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH , GravisProperties.ARROW_HEIGHT / 2d));
        getElements().add(new LineTo(0 , GravisProperties.ARROW_HEIGHT));
        getElements().add(new LineTo(GravisProperties.ARROW_WIDTH / 3d , GravisProperties.ARROW_HEIGHT / 2d));
    }

    @Override
    public Object getBean() {
        return GravisArrow.this;
    }

    @Override
    public List<GravisElementHandle> getElementHandles() {
        return elementHandles;
    }

    @Override
    public Shape getShape() {
        return this;
    }

    @Override
    public List<Shape> getAllShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        return shapes;
    }
    
    @Override
    public IGravisElement getParentElement() {
        return parentElement;
    }
}
