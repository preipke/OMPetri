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
import edu.unibi.agbi.gravisfx.entity.child.GravisChildLabel;
import edu.unibi.agbi.gravisfx.entity.util.GravisShapeHandle;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
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

        /**
         * Control point X coordinate.
         */
        DoubleBinding bindingCurveControlX = new DoubleBinding()
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

                double x3, y3;
                x3 = (x1 + x2) / 2;
                y3 = (y1 + y2) / 2;

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
                    if (deltaY >= 0) {
                        deltaY = 1;
                    } else {
                        deltaY = -1;
                    }
                }

                double m = deltaX / deltaY;
                double b = y3 - m * x3;
                double r = GravisProperties.ARC_GAP / 2;

                double p = 2 * (m * b - m * y3 - x3) / (1 + m * m);
                double q = (x3 * x3 + b * b + y3 * y3 - 2 * b * y3 - r * r) / (1 + m * m);

                if (y2 >= y1) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q);
                } else {
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };

        /**
         * Control point Y coordinate.
         */
        DoubleBinding bindingCurveControlY = new DoubleBinding()
        {
            {
                super.bind(bindingCurveControlX);
            }

            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY() + 0.0001;

                double x = (x1 - x2);
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

                return x / y * bindingCurveControlX.get() + ((y1 + y2) - x / y * (x1 + x2)) / 2;
            }
        };

        /**
         * Line's end X coordinate. Changes it's value on any coordinate changes
         * for the source or target.
         */
        DoubleBinding bindingCurveEndX = new DoubleBinding()
        {
            {
                super.bind(
                        bindingCurveControlY
                );
            }

            /**
             * Computes the X coordinate. Computes the intersection point of the
             * line with a circle through the target coordinates.
             *
             * @return
             */
            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY() + 0.0001;

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

                double b = y1 - y / x * x1;
                double p = 2 * (y / x * b - y / x * y2 - x2) / (1 + y / x * y / x);
                double q = (x2 * x2 + b * b + y2 * y2 - 2 * b * y2 - GravisProperties.ARROW_TARGET_DISTANCE * GravisProperties.ARROW_TARGET_DISTANCE) / (1 + y / x * y / x);

                if (x2 <= x1) {
                    return -p / 2 + Math.sqrt(p * p / 4 - q);
                } else {
                    return -p / 2 - Math.sqrt(p * p / 4 - q);
                }
            }
        };

        /**
         * Line's end Y coordinate. Changes it's value on changes of the related
         * X coordinate.
         */
        DoubleBinding bindingCurveEndY = new DoubleBinding()
        {
            {
                super.bind(bindingCurveEndX);
            }

            /**
             * Uses the previously computed X value to determine the Y value.
             * Uses line function.
             *
             * @return
             */
            @Override
            protected double computeValue() {

                double x1 = source.translateXProperty().get() + source.getCenterOffsetX();
                double y1 = source.translateYProperty().get() + source.getCenterOffsetY();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY() + 0.0001;

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

                return y / x * bindingCurveEndX.get() + y1 - y / x * x1;
            }
        };
        
        DoubleBinding arrowAngle = new DoubleBinding()
        {
            {
                super.bind(bindingCurveEndY);
            }

            @Override
            protected double computeValue() {

                double x1 = bindingCurveControlX.get();
                double y1 = bindingCurveControlY.get();

                double x2 = target.translateXProperty().get() + target.getCenterOffsetX() + 0.0001;
                double y2 = target.translateYProperty().get() + target.getCenterOffsetY() + 0.0001;

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
        
        MoveTo mv = new MoveTo();
        mv.xProperty().bind(source.translateXProperty().add(source.getCenterOffsetX()));
        mv.yProperty().bind(source.translateYProperty().add(source.getCenterOffsetY()));

        QuadCurveTo qct = new QuadCurveTo();
        qct.controlXProperty().bind(bindingCurveControlX);
        qct.controlYProperty().bind(bindingCurveControlY);
        qct.xProperty().bind(bindingCurveEndX);
        qct.yProperty().bind(bindingCurveEndY);
        
        this.endXProperty = qct.xProperty();
        this.endYProperty = qct.yProperty();
        
        this.arrow = new GravisChildArrow(this);
        this.arrow.rotateProperty().bind(arrowAngle);
        this.arrow.translateXProperty().bind(this.endXProperty.subtract(GravisProperties.ARROW_HEIGHT / 2));
        this.arrow.translateYProperty().bind(this.endYProperty.subtract(GravisProperties.ARROW_WIDTH / 2));
        
        this.circle = new GravisChildCircle(this);
        this.circle.centerXProperty().bind(this.endXProperty);
        this.circle.centerYProperty().bind(this.endYProperty);
        this.circle.setRadius(GravisProperties.CIRCLE_SMALL_RADIUS);

        this.getElements().add(mv);
        this.getElements().add(qct);
        
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
