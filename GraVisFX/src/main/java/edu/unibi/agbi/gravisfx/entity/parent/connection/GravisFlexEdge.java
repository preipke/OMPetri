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
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisFlexEdge extends Path implements IGravisConnection, IGravisParent
{
    private final DoubleProperty endXProperty;
    private final DoubleProperty endYProperty;
    private final BooleanProperty isDoubleLinkedProperty = new SimpleBooleanProperty(true);

    private final List<GravisShapeHandle> elementHandles = new ArrayList();
    private final List<Shape> shapes = new ArrayList();

    private final IGravisNode source;
    private final IGravisNode target;

    private final GravisChildArrow arrow;
    private final GravisChildCircle circle;

    private final GravisType type;
    
    public GravisFlexEdge(String id, IGravisNode source, GravisType type) {

        super();
        setId(id);
        this.source = source;
        this.target = null;
        this.type = type;

        MoveTo mv = new MoveTo();
        mv.xProperty().bind(source.translateXProperty().add(source.getCenterOffsetX()));
        mv.yProperty().bind(source.translateYProperty().add(source.getCenterOffsetY()));

        LineTo lt = new LineTo();
        this.endXProperty = lt.xProperty();
        this.endYProperty = lt.yProperty();
        this.endXProperty.set(source.translateXProperty().add(source.getCenterOffsetX()).get());
        this.endYProperty.set(source.translateYProperty().add(source.getCenterOffsetY()).get());

        this.arrow = new GravisChildArrow(this);
        this.arrow.rotateProperty().bind(this.getArrowAngleBinding(endYProperty, endXProperty, endYProperty));
        this.arrow.translateXProperty().bind(this.endXProperty.subtract(GravisProperties.ARROW_HEIGHT / 2));
        this.arrow.translateYProperty().bind(this.endYProperty.subtract(GravisProperties.ARROW_WIDTH / 2));

        this.circle = new GravisChildCircle(this);
        this.circle.centerXProperty().bind(this.endXProperty);
        this.circle.centerYProperty().bind(this.endYProperty);
        this.circle.setRadius(GravisProperties.CIRCLE_SMALL_RADIUS);

        this.getElements().add(mv);
        this.getElements().add(lt);

        this.elementHandles.add(new GravisShapeHandle(this));
        this.elementHandles.addAll(this.arrow.getElementHandles());
        this.elementHandles.addAll(this.circle.getElementHandles());
        
        this.shapes.add(this);
        this.shapes.add(this.arrow);
        this.shapes.add(this.circle);
    }

    public GravisFlexEdge(String id, IGravisNode source, IGravisNode target, GravisType type) {

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

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX();
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY();

                double deltaX = (x2 - x1);
                double deltaY = (y2 - y1);

                if (deltaX < 1 && deltaX > -1) {
                    if (deltaX > 0) {
                        deltaX = 1;
                    } else if (deltaX == 0) {
                        return 0;
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
                
                if (!isDoubleLinkedProperty.get()) {
                    return 0;
                }

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX();
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY();

                double deltaX = (x1 - x2);
                double deltaY = (y2 - y1);

                if (deltaX < 1 && deltaX > -1) {
                    if (deltaX >= 0) {
                        deltaX = 1;
                    } else {
                        deltaX = -1;
                    }
                }

                if (deltaY < 1 && deltaY > -1) {
                    if (deltaY > 0) {
                        deltaY = 1;
                    } else if (deltaY == 0) {
                        return 0;
                    } else {
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
                
                if (!isDoubleLinkedProperty.get()) {
                    return source.translateXProperty().get() + source.getCenterOffsetX();
                }

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
                } else {
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
                
                if (!isDoubleLinkedProperty.get()) {
                    return source.translateYProperty().get() + source.getCenterOffsetY();
                }

                double x0 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y0 = source.translateYProperty().get() + source.getCenterOffsetY();
                
                double m = slopeReverse.get();
                if (m == 0) {
                    if (x0 < target.translateXProperty().get()) {
                        return y0 - GravisProperties.ARC_GAP / 2;
                    } else {
                        return y0 + GravisProperties.ARC_GAP / 2;
                    }
                } else {
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

                double x0 = target.translateXProperty().get() + target.getCenterOffsetX();
                double y0 = target.translateYProperty().get() + target.getCenterOffsetY();

                double m = slope.get();
                if (m == 0) {
                    return x0;
                }

                double b = y0 - m * x0;
                double r = GravisProperties.ARROW_TARGET_DISTANCE;

                double p = 2 * (m * b - m * y0 - x0) / (1 + m * m);
                double q = (x0 * x0 + b * b + y0 * y0 - 2 * b * y0 - r * r) / (1 + m * m);

                if (x0 <= (source.translateXProperty().get() + source.getCenterOffsetX())) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q); // higher x coordinate
                } else {
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

                if (slope.get() == 0) {
                    if (y0 < (source.translateYProperty().get() + source.getCenterOffsetY())) {
                        return y0 + GravisProperties.ARROW_TARGET_DISTANCE;
                    } else {
                        return y0 - GravisProperties.ARROW_TARGET_DISTANCE;
                    }
                }

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
                
                if (!isDoubleLinkedProperty.get()) {
                    return targetOffsetX.get();
                }

                /**
                 * Compute target point location.
                 */
                double x0 = targetOffsetX.get();
                double y0 = targetOffsetY.get();

                double m = slopeReverse.get();
                if (m == 0) {
                    return x0;
                }

                double b = y0 - m * x0;
                double r = GravisProperties.ARC_GAP / 2;

                double p = 2 * (m * b - m * y0 - x0) / (1 + m * m);
                double q = (x0 * x0 + b * b + y0 * y0 - 2 * b * y0 - r * r) / (1 + m * m);

                if (y0 > source.translateYProperty().get() + source.getCenterOffsetY()) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q); // higher x coordinate
                } else {
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
                
                if (!isDoubleLinkedProperty.get()) {
                    return targetOffsetY.get();
                }

                double x0 = targetOffsetX.get();
                double y0 = targetOffsetY.get();
                double m = slopeReverse.get();

                if (m == 0) {
                    if (x0 < target.translateXProperty().get()) {
                        return y0 - GravisProperties.ARC_GAP / 2;
                    } else {
                        return y0 + GravisProperties.ARC_GAP / 2;
                    }
                } else {
                    return m * lineEndX.get() + y0 - m * x0;
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
        this.arrow.rotateProperty().bind(this.getArrowAngleBinding(endYProperty, endXProperty, endYProperty));
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
    
    /**
     * 
     * @param trigger
     * @return 
     */
    private DoubleBinding getArrowAngleBinding(Observable trigger, DoubleProperty endX, DoubleProperty endY) {

        DoubleBinding arrowAngle = new DoubleBinding()
        {
            {
                super.bind(trigger);
            }

            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX() + 0.0001;
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY() + 0.0001;

                double x2 = endX.get();
                double y2 = endY.get();

                double x = (x2 - x1);
                double y = (y2 - y1);

                if (x < 1 && x > -1) {
                    if (x >= 0) {
                        x = 1;
                    } else {
                        x = -1;
                    }
                }

                if (y < 1 && y > -1) {
                    if (y >= 0) {
                        y = 1;
                    } else {
                        y = -1;
                    }
                }

                /**
                 * Winkelverlauf im Uhrzeigersinn: Oben links: 0 bis 90 +180
                 * Oben rechts: -90 bis 0 Unten rechts: 0 bis 90 Unten links:
                 * -90 bis 0 +180
                 */
                if (x2 < x1) {
                    return Math.toDegrees(Math.atan(y / x)) + 180;
                } else {
                    return Math.toDegrees(Math.atan(y / x));
                }
            }
        };
        
        return arrowAngle;
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
    public final double getCenterOffsetX() {
        return 0;
    }

    @Override
    public final double getCenterOffsetY() {
        return 0;
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
    public List<GravisShapeHandle> getElementHandles() {
        return elementHandles;
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
    public Object getBean() {
        return GravisFlexEdge.this;
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
    public GravisType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return source.toString() + " \u2192 " + target.toString();
    }
}
