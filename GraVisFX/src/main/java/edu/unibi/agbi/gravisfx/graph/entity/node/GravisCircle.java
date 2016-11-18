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
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public class GravisCircle extends Circle implements IGravisNode
{
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<GravisEdge> edges = new ArrayList();
    
    public GravisCircle(String id) {
        super();
        setId(id);
    }
    
    public GravisCircle(String id, Paint color) {
        super();
        setId(id);
        setStroke(color);
        setFill(color);
    }
    
    @Override
    public void init(double centerX , double centerY , Scale scale) {
        setPosition(centerX , centerY);
        setScale(scale);
    }
    
    /**
     * Position the center of the node at the given coordinates.
     * @param centerX
     * @param centerY 
     */
    @Override
    public void setPosition(double centerX , double centerY) {
        setCenterX(centerX);
        setCenterY(centerY);
    }
    
    @Override
    public void setScale(Scale scaling) {
        setRadius(PropertiesController.CIRCLE_RADIUS * scaling.getX());
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
