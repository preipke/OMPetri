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
        
        switch (elementType) {
            
            case ARC:
                
                switch (type) {
                    
                    default:
                        throw new Exception("Unhandled referencing type for arc parameter! Check factory implementation!");
                    
                }
//                break;
            
            case PLACE:
                
                switch (type) {
                    
                    case TOKEN:
                        value = "'" + reference.getId() + "'.t";
                        break;

                    default:
                        throw new Exception("Unhandled referencing type for place parameter! Check factory implementation!");

                }
                break;
            
            case TRANSITION:
                
                switch (type) {
                    
                    case SPEED:
                        value = "'" + reference.getId() + "'.actualSpeed";
                        break;

                    default:
                        throw new Exception("Unhandled referencing type for transition parameter! Check factory implementation!");
                    
                }
                break;

            default:
                throw new Exception("Unhandled element type for referencing parameter! Check factory implementation!");
        }
        
        return new ReferencingParameter(id, value, reference, type);
    }
}
