/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.parent.connection;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.entity.GravisType;
import edu.unibi.agbi.gravisfx.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.entity.IGravisParent;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildArrow;
import edu.unibi.agbi.gravisfx.entity.child.GravisChildCircle;
import edu.unibi.agbi.gravisfx.entity.parent.node.GravisCircle;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCurve extends Path implements IGravisConnection, IGravisParent
{
    private final DoubleProperty endXProperty;
    private final DoubleProperty endYProperty;

    private final List<GravisShapeHandle> elementHandles = new ArrayList();
    private final List<Shape> shapes = new ArrayList();

    private final IGravisNode source;
    private final IGravisNode target;

    private final GravisChildArrow arrow;
    private final GravisChildCircle circle;

    private final GravisType type;

    /**
     *
     * @param id
     * @param source
     * @param target
     * @param type
     */
    public GravisCurve(String id, IGravisNode source, IGravisNode target, GravisType type) {

        super();
        setId(id);
        this.source = source;
        this.target = target;
        this.type = type;

        DoubleBinding slope = new DoubleBinding()
        {
            {
                super.bind(
                        source.translateXProperty(),
                        source.translateYProperty(),
                        target.translateXProperty(),
                        target.translateYProperty()
                );
            }

            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY() + 0.0001;

                double deltaX = (x2 - x1);
                double deltaY = (y2 - y1);

                if (deltaX < 1 && deltaX > -1) {
                    if (deltaX >= 0) {
                        deltaX = 1;
                    } else {
                        deltaX = -1;
                    }
                }

                if (deltaY < 1 && deltaY > -1) {
                    if (deltaY >= 0) {
                        deltaY = 1;
                    } else {
                        deltaY = -1;
                    }
                }

                return deltaY / deltaX;
            }
        };

        DoubleBinding slopeReverse = new DoubleBinding()
        {
            {
                super.bind(slope);
            }

            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX();
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY();

                double deltaX = (x1 - x2);
                double deltaY = (y2 - y1);

                if (deltaX < 1 && deltaX > -1) {
                    if (deltaX >= 0) {
                        deltaX = 1;
                    }{
                        deltaX = -1;
                    }
                }

                if (deltaY < 1 && deltaY > -1) {
                    if (deltaY > 0) {
                        deltaY = 1;
                    } else if (deltaY == 0) {
                        return 0;
                    }{
                        deltaY = -1;
                    }
                }
                
                return deltaX / deltaY;
            }
        };

        DoubleBinding lineStartX = new DoubleBinding()
        {
            {
                super.bind(slopeReverse);
            }

            @Override
            protected double computeValue() {

                double x0 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y0 = source.translateYProperty().get() + source.getCenterOffsetY();

                double m = slopeReverse.get();
                
                if (m == 0) {
                    return x0;
                }
                
                double b = y0 - m * x0;
                double r = GravisProperties.ARC_GAP / 2;

                double p = 2 * (m * b - m * y0 - x0) / (1 + m * m);
                double q = (x0 * x0 + b * b + y0 * y0 - 2 * b * y0 - r * r) / (1 + m * m);

                if (y0 <= target.translateYProperty().get() + target.getCenterOffsetY()) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q); // higher x coordinate
                }{
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };

        DoubleBinding lineStartY = new DoubleBinding()
        {
            {
                super.bind(lineStartX);
            }

            @Override
            protected double computeValue() {

                double x0 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y0 = source.translateYProperty().get() + source.getCenterOffsetY();
                double m = slopeReverse.get();
                
                if (m == 0) {
                    if (x0 < target.translateXProperty().get()) {
                        return y0 - GravisProperties.ARC_GAP / 2;
                    }{
                        return y0 + GravisProperties.ARC_GAP / 2;
                    }
                }{
                    return m * lineStartX.get() + y0 - m * x0;
                }

            }
        };

        DoubleBinding targetOffsetX = new DoubleBinding()
        {
            {
                super.bind(lineStartY);
            }

            @Override
            protected double computeValue() {

                double x0 = target.translateXProperty().get() + target.getCenterOffsetX() + 0.0001;
                double y0 = target.translateYProperty().get() + target.getCenterOffsetY() + 0.0001;

                double m = slope.get();
                double b = y0 - m * x0;
                double r = GravisProperties.ARROW_TARGET_DISTANCE;

                double p = 2 * (m * b - m * y0 - x0) / (1 + m * m);
                double q = (x0 * x0 + b * b + y0 * y0 - 2 * b * y0 - r * r) / (1 + m * m);

                if (x0 <= (source.translateXProperty().get() + source.getCenterOffsetX())) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q); // higher x coordinate
                }{
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };

        DoubleBinding targetOffsetY = new DoubleBinding()
        {
            {
                super.bind(targetOffsetX);
            }

            @Override
            protected double computeValue() {

                double x0 = target.translateXProperty().get() + target.getCenterOffsetX();
                double y0 = target.translateYProperty().get() + target.getCenterOffsetY();

                return slope.get() * targetOffsetX.get() + y0 - slope.get() * x0;
            }
        };

        DoubleBinding lineEndX = new DoubleBinding()
        {
            {
                super.bind(targetOffsetY);
            }

            @Override
            protected double computeValue() {

                /**
                 * Compute target point location.
                 */
                double x0 = targetOffsetX.get() + 0.0001;
                double y0 = targetOffsetY.get();

                double m = slopeReverse.get();
                if (m == 0) {
                    return x0;
                }
                
                double b = y0 - m * x0;
                double r = GravisProperties.ARC_GAP / 2;

                double p = 2 * (m * b - m * y0 - x0) / (1 + m * m);
                double q = (x0 * x0 + b * b + y0 * y0 - 2 * b * y0 - r * r) / (1 + m * m);

                if (y0 > source.translateYProperty().get() + source.getCenterOffsetY() + 0.0001) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q); // higher x coordinate
                }{
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };

        DoubleBinding lineEndY = new DoubleBinding()
        {
            {
                super.bind(lineEndX);
            }

            @Override
            protected double computeValue() {

                double x0 = targetOffsetX.get();
                double y0 = targetOffsetY.get();
                double m = slopeReverse.get();
                
                if (m == 0) {
                    if (x0 < target.translateXProperty().get()) {
                        return y0 - GravisProperties.ARC_GAP / 2;
                    }{
                        return y0 + GravisProperties.ARC_GAP / 2;
                    }
                }{
                    return m * lineEndX.get() + y0 - m * x0;
                }
            }
        };

        DoubleBinding arrowAngle = new DoubleBinding()
        {
            {
                super.bind(lineEndY);
            }

            @Override
            protected double computeValue() {

                /**
                 * Winkel je Koordinatenabschnitt: 
                 * Unten links: -90 bis 0 +180 * Oben links: 0 bis 90 +180 * Oben rechts: -90 bis 0 * Unten rechts: 0 bis 90 
                 */
                if (lineEndX.get() < lineStartX.get()) {
                    return Math.toDegrees(Math.atan(slope.get())) + 180;
                }{
                    return Math.toDegrees(Math.atan(slope.get()));
                }
            }
        };

        MoveTo mv = new MoveTo();
        mv.xProperty().bind(lineStartX);
        mv.yProperty().bind(lineStartY);

        LineTo lt = new LineTo();
        lt.xProperty().bind(lineEndX);
        lt.yProperty().bind(lineEndY);

        this.getElements().add(mv);
        this.getElements().add(lt);

        this.endXProperty = lt.xProperty();
        this.endYProperty = lt.yProperty();

        this.arrow = new GravisChildArrow(this);
        this.arrow.rotateProperty().bind(arrowAngle);
        this.arrow.translateXProperty().bind(this.endXProperty.subtract(GravisProperties.ARROW_HEIGHT / 2));
        this.arrow.translateYProperty().bind(this.endYProperty.subtract(GravisProperties.ARROW_WIDTH / 2));

        this.circle = new GravisChildCircle(this);
        this.circle.centerXProperty().bind(this.endXProperty);
        this.circle.centerYProperty().bind(this.endYProperty);
        this.circle.setRadius(GravisProperties.CIRCLE_SMALL_RADIUS);

        this.elementHandles.add(new GravisShapeHandle(this));
        this.elementHandles.addAll(this.arrow.getElementHandles());
        this.elementHandles.addAll(this.circle.getElementHandles());

        this.shapes.add(this);
        this.shapes.add(this.arrow);
        this.shapes.add(this.circle);
    }

    @Override
    public Object getBean() {
        return GravisCurve.this;
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
        return shapes;
    }

    @Override
    public DoubleProperty endXProperty() {
        return endXProperty;
    }

    @Override
    public DoubleProperty endYProperty() {
        return endYProperty;
    }

    @Override
    public IGravisNode getSource() {
        return source;
    }

    @Override
    public IGravisNode getTarget() {
        return target;
    }

    @Override
    public void setArrowHeadVisible(boolean value) {
        this.arrow.setVisible(value);
    }

    @Override
    public void setCircleHeadVisible(boolean value) {
        this.circle.setVisible(value);
    }

    @Override
    public List<GravisShapeHandle> getParentElementHandles() {
        List<GravisShapeHandle> handles = new ArrayList();
        handles.add(elementHandles.get(0));
        return handles;
    }

    @Override
    public List<GravisShapeHandle> getChildElementHandles() {
        List<GravisShapeHandle> handles = new ArrayList();
        handles.addAll(arrow.getElementHandles());
        handles.addAll(circle.getElementHandles());
        return handles;
    }

    @Override
    public GravisType getType() {
        return type;
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
