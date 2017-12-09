/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.parameter;

import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.model.Parameter;

/**
 *
 * @author PR
 */
public class LocalParameter extends Parameter
{
    public LocalParameter(String id, String value, String unit, IElement element) {
        super(Parameter.Type.LOCAL, id, value, element);
    }
}
