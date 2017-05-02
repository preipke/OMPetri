/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.Map;
import java.util.Set;

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

    /**
     * Gets a set of all filter names related to this element. These names will
     * be returned by the data stream from the OpenModelica simulation
     * executable.
     *
     * @return
     */
    public Set<String> getFilter();

    /**
     * Gets all (local) parameters that have been created exclusively for this
     * element.
     *
     * @return
     */
    public Map<String, Parameter> getParameters();

    public boolean isEnabled();

    public void setEnabled(boolean value);
}
