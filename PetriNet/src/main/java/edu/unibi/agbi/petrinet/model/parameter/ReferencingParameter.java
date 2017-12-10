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
public class ReferencingParameter extends Parameter
{
    private ReferenceType referenceType;
    
    public ReferencingParameter(String id, String value, IElement reference, ReferenceType type) {
        super(Parameter.Type.REFERENCE, id, value, reference);
    }
    
    public ReferenceType getReferenceType() {
        return referenceType;
    }
    
    public enum ReferenceType {
        TOKEN, SPEED
    }
}
