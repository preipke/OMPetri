/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.List;

/**
 *
 * @author PR
 */
public interface IElement
{
    public Element.Type getElementType();
    public String getId();
    public String getName();
    public void setName(String name);
    public List<String> getFilter();
    public List<Parameter> getParameter();
    public boolean isEnabled();
    public void setEnabled(boolean value);
}
