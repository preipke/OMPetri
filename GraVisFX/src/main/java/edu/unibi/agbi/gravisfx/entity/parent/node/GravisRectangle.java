/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.node;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildCircle;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildLabel;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildRectangle;

/**
 *
 * @author PR
 */
public class GravisRectangle extends Rectangle implements IGravisNode
{
    private final List<GravisShapeHandle> shapeHandles = new ArrayList();
    private final List<Shape> shapes = new ArrayList();

    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<IGravisConnection> edges = new ArrayList();

    private final GravisChildLabel label;
    private final GravisChildCircle circle;
    private final GravisChildRectangle rectangle;
    
    private final GravisType type;

    /**
     * 
     * @param id
     * @param type 
     */
    public GravisRectangle(String id, GravisType type) {

        super();
        setId(id);
        setWidth(GravisProperties.RECTANGLE_WIDTH);
        setHeight(GravisProperties.RECTANGLE_HEIGHT);
        setArcWidth(GravisProperties.RECTANGLE_ARC_WIDTH);
        setArcHeight(GravisProperties.RECTANGLE_ARC_HEIGHT);
        this.type = type;

        label = new GravisChildLabel(this);
        label.xProperty().bind(translateXProperty().add(getCenterOffsetX() + GravisProperties.LABEL_OFFSET_X));
        label.yProperty().bind(translateYProperty().add(getCenterOffsetY() + GravisProperties.LABEL_OFFSET_Y));
        
        circle = new GravisChildCircle(this);
        circle.setRadius(GravisProperties.CIRCLE_RADIUS - GravisProperties.BASE_INNER_DISTANCE);
        circle.translateXProperty().bind(translateXProperty());
        circle.translateYProperty().bind(translateYProperty());
        
        rectangle = new GravisChildRectangle(this);
        rectangle.setWidth(GravisProperties.RECTANGLE_WIDTH - GravisProperties.BASE_INNER_DISTANCE * 2);
        rectangle.setHeight(GravisProperties.RECTANGLE_HEIGHT - GravisProperties.BASE_INNER_DISTANCE * 2);
        rectangle.translateXProperty().bind(translateXProperty().add(GravisProperties.BASE_INNER_DISTANCE));
        rectangle.translateYProperty().bind(translateYProperty().add(GravisProperties.BASE_INNER_DISTANCE));
        
        shapes.add(this);
        shapes.add(circle);
        shapes.add(rectangle);

        shapeHandles.add(new GravisShapeHandle(this));
        shapeHandles.addAll(circle.getElementHandles());
        shapeHandles.addAll(rectangle.getElementHandles());
    }
    
    public GravisChildCircle getCircle() {
        return circle;
    }
    
    public GravisChildRectangle getRectangle() {
        return rectangle;
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
        return shapes;
    }

    @Override
    public final List<GravisShapeHandle> getElementHandles() {
        return shapeHandles;
    }

    @Override
    public final double getCenterOffsetX() {
        return getWidth() / 2;
    }

    @Override
    public final double getCenterOffsetY() {
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
    public final GravisChildLabel getLabel() {
        return label;
    }

    @Override
    public List<GravisShapeHandle> getParentElementHandles() {
        List<GravisShapeHandle> handles = new ArrayList();
        handles.add(shapeHandles.get(0));
        return handles;
    }

    @Override
    public List<GravisShapeHandle> getChildElementHandles() {
        List<GravisShapeHandle> handles = new ArrayList();
        handles.addAll(circle.getElementHandles());
        handles.addAll(rectangle.getElementHandles());
        return handles;
    }
    
    @Override 
    public GravisType getType() {
        return type;
    }

    @Override
    public void setInnerCircleVisible(boolean value) {
        this.circle.setVisible(value);
    }

    @Override
    public void setInnerRectangleVisible(boolean value) {
        this.rectangle.setVisible(value);
    }
}
