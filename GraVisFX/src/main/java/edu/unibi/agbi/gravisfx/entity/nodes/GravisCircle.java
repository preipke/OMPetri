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
import javafx.scene.shape.Circle;

/**
 *
 * @author PR
 */
public class GravisCircle extends Circle implements IGravisNode
{
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    
    public GravisCircle() {
        super();
    }

    @Override
    public void initialize(String id, double centerX , double centerY , double scaling) {
        this.setId(id);
        this.setCenterX(centerX);
        this.setCenterY(centerY);
        this.setRadius(PropertiesController.CIRCLE_RADIUS);
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
