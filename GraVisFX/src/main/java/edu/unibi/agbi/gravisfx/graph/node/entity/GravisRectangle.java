/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.node.entity;

import edu.unibi.agbi.gravisfx.graph.node.IGravisNode;
import edu.unibi.agbi.gravisfx.PropertiesController;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * 
 * @author PR
 */
public final class GravisRectangle extends Rectangle implements IGravisNode
{
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<GravisEdge> edges = new ArrayList();
    
    public GravisRectangle(String id) {
        super();
        setId(id);
        init();
    }
    
    public GravisRectangle(String id, Paint color) {
        super();
        setId(id);
        setStroke(color);
        setFill(color);
        init();
    }
    
    public void init() {
        setWidth(PropertiesController.RECTANGLE_WIDTH);
        setHeight(PropertiesController.RECTANGLE_HEIGHT);
        setArcWidth(PropertiesController.RECTANGLE_ARC_WIDTH);
        setArcHeight(PropertiesController.RECTANGLE_ARC_HEIGHT);
    }
    
    /**
     * Position the center of the node at the given coordinates.
     * @param centerX
     * @param centerY 
     */
    @Override
    public void setTranslate(double centerX , double centerY) {
        
        centerX = centerX - getOffsetX();
        centerY = centerY - getOffsetY();
        
        setTranslateX(centerX);
        setTranslateY(centerY);
    }

    @Override
    public double getOffsetX() {
        return getBoundsInParent().getWidth() / 2;
    }

    @Override
    public double getOffsetY() {
        return getBoundsInParent().getHeight() / 2;
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
    public void addEdge(GravisEdge edge) {
        edges.add(edge);
    }
    
    @Override
    public List<GravisEdge> getEdges() {
        return edges;
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
}
