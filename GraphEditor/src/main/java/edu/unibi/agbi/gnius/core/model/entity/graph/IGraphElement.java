/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.graph;

import edu.unibi.agbi.gnius.core.model.entity.data.IDataElement;
import edu.unibi.agbi.gravisfx.graph.entity.IGravisElement;

/**
 *
 * @author PR
 */
public interface IGraphElement extends IGravisElement
{
    public IDataElement getRelatedDataElement();
    
    public void setHighlighted(boolean value);
    
    public void setActiveStyleClass(String name);
    public String getActiveStyleClass();
    
    public void setSelected(boolean value);
    public void putOnTop();
}
