/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.entity;

import edu.unibi.agbi.gravisfx.entity.child.GravisLabel;
import java.util.List;

/**
 * Interface for interactive nodes within the graph. Used by the parent component
 * of a node only, i.e. Circle, Rectangle, DoubleCircle, DoubleRectanle.
 * @author PR
 */
public interface IGravisNode extends IGravisElement
{
    public double getOffsetX();
    public double getOffsetY();
    
    public List<IGravisNode> getParents();
    public List<IGravisNode> getChildren();
    public List<IGravisConnection> getConnections();
    
    public boolean isChildShapesEnabled();
    public void setChildShapesEnabled(boolean value);
    public List<IGravisSubElement> getChildElements();
    
    public GravisLabel getLabel();
}
