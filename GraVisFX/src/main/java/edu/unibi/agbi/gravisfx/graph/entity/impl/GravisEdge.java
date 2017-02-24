/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.impl;

import edu.unibi.agbi.gravisfx.graph.entity.IGravisConnection;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisNode;
import edu.unibi.agbi.gravisfx.graph.layer.EdgeLayer;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisEdge extends Line implements IGravisConnection
{
    private final IGravisNode source;
    private IGravisNode target;
    
    /**
     * 
     * @param source must not be null, final
     * @param target can be null and set later
     */
    public GravisEdge(IGravisNode source, IGravisNode target) {
        super();
        this.source = source;
        this.target = target;
//        System.out.println("Width = " + getBoundsInParent().getWidth());
//        System.out.println("Height = " + getBoundsInParent().getHeight());
        startXProperty().bind(source.translateXProperty().add(source.getOffsetX()));
        startYProperty().bind(source.translateYProperty().add(source.getOffsetY()));
//        System.out.println("Offset X = " + source.getOffsetX());
//        System.out.println("Offset Y = " + source.getOffsetY());
        if (target != null) {
            endXProperty().bind(target.translateXProperty().add(target.getOffsetX()));
            endYProperty().bind(target.translateYProperty().add(target.getOffsetY()));
        } else {
            endXProperty().set(startXProperty().get());
            endYProperty().set(startYProperty().get());
        }
    }
    
    public void setTarget(IGravisNode target) {
        this.target = target;
        if (target != null) {
            endXProperty().bind(target.translateXProperty().add(target.getOffsetX()));
            endYProperty().bind(target.translateYProperty().add(target.getOffsetY()));
        }
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
    public List<Shape> getShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        return shapes;
    }

    @Override
    public void setTranslate(double positionX , double positionY) {
    }
    
    @Override
    public void putOnTop() {
        EdgeLayer edgeLayer = (EdgeLayer) getParent();
        edgeLayer.getChildren().remove(this);
        edgeLayer.getChildren().add(this);
    }
}
