/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.node;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.IGravisSubElement;
import edu.unibi.agbi.gravisfx.entity.child.GravisLabel;
import edu.unibi.agbi.gravisfx.entity.util.ElementHandle;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * 
 * @author PR
 */
public class GravisRectangle extends Rectangle implements IGravisNode
{
    private final List<ElementHandle> elementHandles = new ArrayList();
    
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<IGravisConnection> edges = new ArrayList();
    
    private final GravisLabel label;
    
    private boolean isChildShapesEnabled = true;
    
    public GravisRectangle() {
        
        super();
        
        elementHandles.add(new ElementHandle(this));
        
        setWidth(GravisProperties.RECTANGLE_WIDTH);
        setHeight(GravisProperties.RECTANGLE_HEIGHT);
        setArcWidth(GravisProperties.RECTANGLE_ARC_WIDTH);
        setArcHeight(GravisProperties.RECTANGLE_ARC_HEIGHT);
        
        label = new GravisLabel(this);
        label.xProperty().bind(translateXProperty().add(getOffsetX() + GravisProperties.LABEL_OFFSET_X));
        label.yProperty().bind(translateYProperty().add(getOffsetY() + GravisProperties.LABEL_OFFSET_Y));
    }
    
    @Override
    public Object getBean() {
        return GravisRectangle.this;
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
    public final List<ElementHandle> getElementHandles() {
        return elementHandles;
    }

    @Override
    public final double getOffsetX() {
        return getWidth() / 2;
    }

    @Override
    public final double getOffsetY() {
        return getHeight() / 2;
    }
    
    @Override
    public final List<IGravisNode> getParents() {
        return parents;
    }
    
    @Override
    public final List<IGravisNode> getChildren() {
        return children;
    }
    
    @Override
    public final List<IGravisConnection> getConnections() {
        return edges;
    }
    
    @Override
    public final boolean isChildShapesEnabled() {
        return isChildShapesEnabled;
    }
    
    @Override
    public final void setChildShapesEnabled(boolean value) {
        isChildShapesEnabled = value;
    }
    
    @Override
    public List<IGravisSubElement> getChildElements() {
        return new ArrayList();
    }
    
    @Override
    public final GravisLabel getLabel() {
        return label;
    }
}
