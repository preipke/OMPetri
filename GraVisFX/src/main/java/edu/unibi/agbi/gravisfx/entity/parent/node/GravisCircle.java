/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.node;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildLabel;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import edu.unibi.agbi.gravisfx.entity.IGravisParent;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildCircle;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildRectangle;

/**
 *
 * @author PR
 */
public class GravisCircle extends Circle implements IGravisNode, IGravisParent
{
    private final List<GravisShapeHandle> shapeHandles = new ArrayList();
    private final List<Shape> shapes = new ArrayList();

    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<IGravisConnection> connections = new ArrayList();

    private final GravisChildLabel label;
    
    private final GravisChildCircle circle;
    private final GravisChildRectangle rectangle;

    private int exportId = 0;

    public GravisCircle() {

        super();

        setRadius(GravisProperties.CIRCLE_RADIUS);

        label = new GravisChildLabel(this);
        label.xProperty().bind(translateXProperty().add(GravisProperties.LABEL_OFFSET_X));
        label.yProperty().bind(translateYProperty().add(GravisProperties.LABEL_OFFSET_Y));
        
        circle = new GravisChildCircle(this);
        circle.setRadius(GravisProperties.CIRCLE_RADIUS - GravisProperties.BASE_INNER_DISTANCE);
        circle.translateXProperty().bind(translateXProperty());
        circle.translateYProperty().bind(translateYProperty());

        double x1 = translateXProperty().get();
        double y1 = translateYProperty().get();

        double b = y1 + x1;
        double r = getRadius();
        double p = 2 * (y1 - x1 - b) / 2;
        double q = (x1 * x1 + b * b + y1 * y1 - 2 * b * y1 - r * r) / 2;

        double x2 = -p / 2 + Math.sqrt(p * p / 4 - q);
        double y2 = -x2 + b;

        double offsetX = Math.abs(x1 - x2);
        double offsetY = Math.abs(y1 - y2);

        rectangle = new GravisChildRectangle(this);
        rectangle.translateXProperty().bind(translateXProperty().subtract(offsetX));
        rectangle.translateYProperty().bind(translateYProperty().subtract(offsetY));
        rectangle.setArcWidth(GravisProperties.RECTANGLE_ARC_WIDTH);
        rectangle.setArcHeight(GravisProperties.RECTANGLE_ARC_HEIGHT);
        rectangle.setWidth(offsetX * 2);
        rectangle.setHeight(offsetY * 2);
        
        shapes.add(this);
        shapes.add(circle);
        shapes.add(rectangle);
        
        shapeHandles.add(new GravisShapeHandle(this));
        shapeHandles.addAll(circle.getElementHandles());
        shapeHandles.addAll(rectangle.getElementHandles());
    }

    @Override
    public Object getBean() {
        return GravisCircle.this;
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
    public final double getOffsetX() {
        return 0; // position is fixed to shape center
    }

    @Override
    public final double getOffsetY() {
        return 0; // position is fixed to shape center
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
        return connections;
    }

    @Override
    public final GravisChildLabel getLabel() {
        return label;
    }
    
    @Override
    public int getExportId() {
        return exportId;
    }
    
    @Override
    public void setExportId(int id) {
        this.exportId = id;
    }

    @Override
    public List<GravisShapeHandle> getParentElementHandles() {
        List<GravisShapeHandle> handles = new ArrayList();
        handles.add(this.shapeHandles.get(0));
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
    public void setInnerCircleVisible(boolean value) {
        this.circle.setVisible(value);
    }

    @Override
    public void setInnerRectangleVisible(boolean value) {
        this.rectangle.setVisible(value);
    }
}
