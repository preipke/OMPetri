/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Element;
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
     * Gets all parameters related to this element. This can either be
     * parameters local for this element or parameters that reference this
     * element.
     *
     * @return
     */
    public Set<String> getRelatedParameterIds();

    public boolean isEnabled();

    public void setEnabled(boolean value);
}
