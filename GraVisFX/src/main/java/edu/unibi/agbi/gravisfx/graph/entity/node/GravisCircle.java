/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.node;

import edu.unibi.agbi.gravisfx.controller.PropertiesController;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCircle implements IGravisNode
{
    private final String id;
    
    private final Circle circle;
    
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    
    public GravisCircle(String nodeId) {
        id = nodeId;
        circle = new Circle();
        circle.setId(id);
    }
    
    @Override
    public void init(double centerX , double centerY , double scaling) {
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
        circle.setRadius(PropertiesController.CIRCLE_RADIUS * scaling);
    }
    
    @Override
    public void addParentNode(IGravisNode parent) {
        parents.add(parent);
    }
    
    @Override
    public List<IGravisNode> getParents() {
        return parents;
    }
    
    @Override
    public void addChildNode(IGravisNode child) {
        children.add(child);
    }
    
    @Override
    public List<IGravisNode> getChildren() {
        return children;
    }

    @Override
    public void setFill(Color color) {
        circle.setFill(color);
    }

    @Override
    public void setStroke(Color color) {
        circle.setStroke(color);
    }

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Shape getShape() {
        return circle;
    }

    @Override
    public void relocate(double positionX , double positionY) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
