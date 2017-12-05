/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author PR
 */
public interface IElement
{
    public Element.Type getElementType();

    public void setId(String id);

    public String getId();

//    public String getName();
//
//    public void setName(String name);
    
    public boolean isDisabled();

    public void setDisabled(boolean value);
    
    public void addLocalParameter(Parameter param);
    
    public Parameter getLocalParameter(String id);
    
    public Collection<Parameter> getLocalParameters();

    public Set<Parameter> getRelatedParameters();
}
