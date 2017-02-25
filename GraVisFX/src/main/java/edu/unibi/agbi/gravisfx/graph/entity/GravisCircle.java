/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity;

import edu.unibi.agbi.gravisfx.GravisProperties;
import edu.unibi.agbi.gravisfx.graph.entity.abst.GravisElementHandle;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public class GravisCircle extends Circle implements IGravisNode
{
    private final List<GravisElementHandle> elementHandles = new ArrayList();
    
    private final List<IGravisNode> children = new ArrayList();
    private final List<IGravisNode> parents = new ArrayList();
    private final List<IGravisConnection> edges = new ArrayList();
    
    public GravisCircle() {
        
        super();
        
        elementHandles.add(new GravisElementHandle(this));
        
        setRadius(GravisProperties.CIRCLE_RADIUS);
    }
    
    @Override
    public Object getBean() {
        return GravisCircle.this;
    }

    @Override
    public List<GravisElementHandle> getElementHandles() {
        return elementHandles;
    }

    @Override
    public double getOffsetX() {
        return 0; // position is fixed to shape center
    }

    @Override
    public double getOffsetY() {
        return 0; // position is fixed to shape center
    }
    
    @Override
    public Shape getShape() {
        return this;
    }
    
    @Override
    public List<Shape> getAllShapes() {
        List<Shape> shapes = new ArrayList();
        shapes.add(this);
        return shapes;
    }
    
    @Override
    public void addParentNode(IGravisNode parent) {
        parents.add(parent);
    }
    
    @Override
    public void addChildNode(IGravisNode child) {
        children.add(child);
    }
    
    @Override
    public void addEdge(IGravisConnection edge) {
        edges.add(edge);
    }
    
    @Override
    public List<IGravisNode> getParents() {
        return parents;
    }
    
    @Override
    public List<IGravisNode> getChildren() {
        return children;
    }
    
    @Override
    public List<IGravisConnection> getEdges() {
        return edges;
    }
    
    @Override
    public boolean removeChild(IGravisNode node) {
        return children.remove(node);
    }
    
    @Override
    public boolean removeParent(IGravisNode node) {
        return parents.remove(node);
    }
    
    @Override
    public boolean removeConnection(IGravisConnection edge) {
        return edges.remove(edge);
    }
}
