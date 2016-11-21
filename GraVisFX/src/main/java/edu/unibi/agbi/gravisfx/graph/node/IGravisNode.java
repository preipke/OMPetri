/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.node;

import edu.unibi.agbi.gravisfx.graph.node.entity.GravisEdge;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/**
 *
 * @author PR
 */
public interface IGravisNode
{
    public void setTranslate(double positionX, double positionY);
    
    public double getOffsetX();
    
    public double getOffsetY();
    
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
