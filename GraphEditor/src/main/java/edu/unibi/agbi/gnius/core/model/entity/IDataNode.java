/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity;

import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;

import java.util.List;

/**
 *
 * @author PR
 */
public interface IDataNode
{
    public List<Object> getShapes();
    public PN_Element.Type getElementType();
    public List<Parameter> getParameter();
}
