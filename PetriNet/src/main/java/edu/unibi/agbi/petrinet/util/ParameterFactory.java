/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.entity.abstr.Element;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.parameter.GlobalParameter;
import edu.unibi.agbi.petrinet.model.parameter.LocalParameter;
import edu.unibi.agbi.petrinet.model.parameter.ReferencingParameter;

/**
 *
 * @author PR
 */
public class ParameterFactory
{
    
    public Parameter createLocalParameter(String id, String value, String unit, IElement reference) {
        
        return new LocalParameter(id, value, unit, reference);
    }
    
    public Parameter createGlobalParameter(String id, String value, String unit) {
        
        return new GlobalParameter(id, value, unit);
    }
    
    public Parameter createReferencingParameter(String id, IElement reference, ReferencingParameter.ReferenceType type) throws Exception {
        
        Element.Type elementType = reference.getElementType();
        String value;
        
        switch (type) {
            
            case SPEED:
                if (elementType != Element.Type.TRANSITION) {
                    throw new Exception("Reference type doesn't suit the given element type! (" + elementType + " -> " + type + ")");
                }
                value = "'" + reference.getId() + "'.actualSpeed";
                break;
            
            case TOKEN:
                if (elementType != Element.Type.PLACE) {
                    throw new Exception("Reference type doesn't suit the given element type! (" + elementType + " -> " + type + ")");
                }
                value = "'" + reference.getId() + "'.t";
                break;
                
            default:
                throw new Exception("Unhandled referencing parameter type!");
        }
        
        return new ReferencingParameter(id, value, reference, type);
    }
}
