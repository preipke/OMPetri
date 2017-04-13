/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data;

import edu.unibi.agbi.gnius.core.model.entity.graph.IGraphElement;
import java.util.List;
import edu.unibi.agbi.petrinet.entity.IElement;

/**
 *
 * @author PR
 */
public interface IDataElement extends IElement 
{
    public String getDescription();
    public void setDescription(String text);
    public String getLabelText();
    public void setLabelText(String text);
    public List<IGraphElement> getGraphElements();
}
