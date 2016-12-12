/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gravisfx.graph.node;

import edu.unibi.agbi.gravisfx.exception.RelationChangeDeniedException;
import javafx.scene.shape.Shape;

/**
 *
 * @author PR
 */
public interface IGravisSelectable
{
    public Shape getShape();
    
    public double getTranslateX();
    public double getTranslateY();
    
    public Object getRelatedObject();
    public void setRelatedObject(Object relatedObject) throws RelationChangeDeniedException;
    
    public void setHighlight(boolean value);
    public void putOnTop();
    
    public void setActiveStyleClass(String name);
    public String getActiveStyleClass();
}
