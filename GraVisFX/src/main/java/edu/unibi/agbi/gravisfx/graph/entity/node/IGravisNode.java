/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.entity.node;

import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public interface IGravisNode
{
    public void init(double centerX , double centerY , double scaling);
    
    public void addParentNode(IGravisNode parent);
    
    public void addChildNode(IGravisNode child);
    
    public List<IGravisNode> getParents();
    
    public List<IGravisNode> getChildren();
    
    public void setFill(Color color);
    
    public void setStroke(Color color);
    
    public String getId();
    
    public Shape getShape();
    
    public void relocate(double positionX, double positionY);
}
