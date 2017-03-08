/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import java.util.List;

/**
 *
 * @author PR
 */
public interface IPN_Element
{
    public String getId();
    public void setFilterNames(List<String> names);
    public void addFilterName(String name);
    public List<String> getFilterNames();
    public PN_Element.Type getElementType();
    public boolean isEnabled();
}
