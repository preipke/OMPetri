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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * 
 * @author PR
 */
public class GravisRectangle implements IGravisNode
{
    private final String id;
    
    private final Rectangle rectangle;
    
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    
    public GravisRectangle(String nodeId) {
        id = nodeId;
        rectangle = new Rectangle();
        rectangle.setId(id);
    }
    
    @Override
    public void init(double centerX , double centerY , double scaling) {
        
        rectangle.setX(centerX - PropertiesController.RECTANGLE_WIDTH / 2 * scaling);
        rectangle.setY(centerY - PropertiesController.RECTANGLE_HEIGHT / 2 * scaling);
        rectangle.setWidth(PropertiesController.RECTANGLE_WIDTH * scaling);
        rectangle.setHeight(PropertiesController.RECTANGLE_HEIGHT * scaling);
        rectangle.setArcWidth(PropertiesController.RECTANGLE_ARC_WIDTH * scaling);
        rectangle.setArcHeight(PropertiesController.RECTANGLE_ARC_HEIGHT * scaling);
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
        rectangle.setFill(color);
    }

    @Override
    public void setStroke(Color color) {
        rectangle.setStroke(color);
    }

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public Shape getShape() {
        return rectangle;
    }

    @Override
    public void relocate(double positionX , double positionY) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
