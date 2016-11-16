/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.nodes;

import edu.unibi.agbi.gravisfx.controller.PropertiesController;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 
 * @author PR
 */
public class GravisRectangle extends Rectangle implements IGravisNode
{
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    
    public GravisRectangle(String nodeId) {
        super();
    }

    @Override
    public void initialize(String id, double centerX , double centerY , double scaling) {
        this.setId(id);
        // TODO care for values not to be negative! might cause problems
        this.setX(centerX - PropertiesController.RECTANGLE_WIDTH / 2 * scaling);
        this.setY(centerY - PropertiesController.RECTANGLE_HEIGHT / 2 * scaling);
        this.setWidth(PropertiesController.RECTANGLE_WIDTH * scaling);
        this.setHeight(PropertiesController.RECTANGLE_HEIGHT * scaling);
        this.setArcWidth(PropertiesController.RECTANGLE_ARC_WIDTH * scaling);
        this.setArcHeight(PropertiesController.RECTANGLE_ARC_HEIGHT * scaling);
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
        super.setFill(color);
    }

    @Override
    public void setStroke(Color color) {
        super.setStroke(color);
    }
}
