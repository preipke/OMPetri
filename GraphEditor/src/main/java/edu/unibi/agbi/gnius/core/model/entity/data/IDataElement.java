/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model.entity.data;

import edu.unibi.agbi.petrinet.entity.IPN_Element;
import edu.unibi.agbi.petrinet.entity.PN_Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.List;

/**
 *
 * @author PR
 */
public interface IDataElement extends IPN_Element
{
    public PN_Element.Type getElementType();
    public List<Parameter> getParameter();
}
