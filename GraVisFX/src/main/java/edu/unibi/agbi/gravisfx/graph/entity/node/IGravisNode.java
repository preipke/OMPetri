/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.node;

import java.util.List;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public interface IGravisNode
{
    public void init(double centerX , double centerY , Scale scaling);
    
    public void setPosition(double positionX, double positionY);
    
    public void setScale(Scale scale);
    
    public void addParentNode(IGravisNode parent);
    
    public void addChildNode(IGravisNode child);
    
    public void addEdge(GravisEdge edge);
    
    public List<IGravisNode> getParents();
    
    public List<IGravisNode> getChildren();
    
    public List<GravisEdge> getEdges();
    
    public void setFill(Paint color);
    
    public void setStroke(Paint color);
    
    public String getId();
    
    public Shape getShape();
}
