/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import java.util.List;

/**
 *
 * @author PR
 */
public interface IElement
{
    public String getId();
    public void setFilterNames(List<String> names);
    public void addFilterName(String name);
    public List<String> getFilterNames();
    public Element.Type getElementType();
    public boolean isEnabled();
}
