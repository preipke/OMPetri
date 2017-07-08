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
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisEdge extends Path implements IGravisConnection, IGravisParent
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
     * @param type 
     */
    public GravisEdge(String id, IGravisNode source, GravisType type) {

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

    /**
     * 
     * @param id
     * @param source
     * @param target
     * @param type 
     */
    public GravisEdge(String id, IGravisNode source, IGravisNode target, GravisType type) {

        super();
        setId(id);
        this.source = source;
        this.target = target;
        this.type = type;

        /**
         * Line's end X coordinate. Changes it's value on any coordinate changes
         * for the source or target.
         */
        DoubleBinding bindingLineEndX = new DoubleBinding()
        {
            {
                super.bind(
                        source.translateXProperty(),
                        target.translateXProperty(),
                        source.translateYProperty(),
                        target.translateYProperty()
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
        DoubleBinding bindingLineEndY = new DoubleBinding()
        {
            {
                super.bind(bindingLineEndX);
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

                return y / x * bindingLineEndX.get() + y1 - y / x * x1;
            }
        };


        MoveTo mv = new MoveTo();
        mv.xProperty().bind(source.translateXProperty().add(source.getCenterOffsetX()));
        mv.yProperty().bind(source.translateYProperty().add(source.getCenterOffsetY()));

        LineTo lt = new LineTo();
        this.endXProperty = lt.xProperty();
        this.endYProperty = lt.yProperty();
        this.endXProperty.bind(bindingLineEndX);
        this.endYProperty.bind(bindingLineEndY);

        this.arrow = new GravisChildArrow(this);
        this.arrow.rotateProperty().bind(this.getArrowAngleBinding(bindingLineEndY, endXProperty, endYProperty));
        this.arrow.translateXProperty().bind(this.endXProperty.subtract(GravisProperties.ARROW_HEIGHT / 2));
        this.arrow.translateYProperty().bind(this.endYProperty.subtract(GravisProperties.ARROW_WIDTH / 2));

        this.circle = new GravisChildCircle(this);
        this.circle.centerXProperty().bind(this.endXProperty);
        this.circle.centerYProperty().bind(this.endYProperty);
        this.circle.setRadius(GravisProperties.CIRCLE_SMALL_RADIUS);

        this.getElements().add(mv);
        this.getElements().add(lt);
        
        this.elementHandles.add(new GravisShapeHandle(this));
        this.elementHandles.addAll(arrow.getElementHandles());
        this.elementHandles.addAll(circle.getElementHandles());
        
        this.shapes.add(this);
        this.shapes.add(arrow);
        this.shapes.add(circle);
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
    public Object getBean() {
        return GravisEdge.this;
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
    
    @Override
    public String toString() {
        return getId() + " (x = " + getTranslateX() + ", y = " + getTranslateY() + ")";
    }
}
