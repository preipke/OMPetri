/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity.nodes;

import java.util.List;
import javafx.scene.paint.Color;

/**
 *
 * @author PR
 */
public interface IGravisNode
{
    /**
     * 
     * @param id
     * @param positionX
     * @param positionY
     * @param scaling 
     */
    public void initialize(String id, double positionX , double positionY , double scaling);
    
    public void addParentNode(IGravisNode parent);
    
    public void addChildNode(IGravisNode child);
    
    public List<IGravisNode> getChildren();
    
    public List<IGravisNode> getParents();
    
    public void setFill(Color color);
    
    public void setStroke(Color color);
    
    public void relocate(double positionX, double positionY);
}
