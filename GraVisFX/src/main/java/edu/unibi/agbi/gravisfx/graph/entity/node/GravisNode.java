/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.node;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public abstract class GravisNode
{
    private final String id;
    
    protected final Shape shape;
    
    private final List<GravisNode> children = new ArrayList();
    private final List<GravisNode> parents = new ArrayList();
    private final List<GravisEdge> edges = new ArrayList();
    
    public GravisNode(String id, Shape shape) {
        this.id = id;
        this.shape = shape;
        this.shape.setId(id);
    }
    
    public GravisNode(String id, Shape shape, Color color) {
        this.id = id;
        this.shape = shape;
        this.shape.setId(id);
        shape.setStroke(color);
        shape.setFill(color);
    }
    
    public void setPosition(double centerX , double centerY) {
        shape.relocate(centerY , centerY);
    }
    
    public void setScale(Scale scale) {
        shape.setScaleX(scale.getX());
        shape.setScaleY(scale.getY());
    }
    
    public void addParentNode(GravisNode parent) {
        parents.add(parent);
    }
    
    public List<GravisNode> getParents() {
        return parents;
    }
    
    public void addChildNode(GravisNode child) {
        children.add(child);
    }
    
    public List<GravisNode> getChildren() {
        return children;
    }
    
    public void addEdge(GravisEdge edge) {
        edges.add(edge);
    }
    
    public List<GravisEdge> getEdges() {
        return edges;
    }

    public void setFill(Color color) {
        shape.setFill(color);
    }

    public void setStroke(Color color) {
        shape.setStroke(color);
    }

    public String getId() {
        return id;
    }
    
    public Shape getShape() {
        return shape;
    }
}
