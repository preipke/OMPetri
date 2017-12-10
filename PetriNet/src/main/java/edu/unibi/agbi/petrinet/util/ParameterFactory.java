/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.util;

import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.parameter.GlobalParameter;
import edu.unibi.agbi.petrinet.model.parameter.LocalParameter;
import edu.unibi.agbi.petrinet.model.parameter.ReferencingParameter;
import edu.unibi.agbi.petrinet.model.parameter.ReferencingParameter.ReferenceType;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author PR
 */
public class ParameterFactory
{
    private final String propertiesPath = "/parameter.properties";
    private final Properties properties;
    
    public ParameterFactory() throws IOException {
        properties = new Properties();
        properties.load(ParameterFactory.class.getResourceAsStream(propertiesPath));
    }
    
    public Parameter createLocalParameter(String id, String value, String unit, IElement reference) {
        
        return new LocalParameter(id, value, unit, reference);
    }
    
    public Parameter createGlobalParameter(String id, String value, String unit) {
        
        return new GlobalParameter(id, value, unit);
    }
    
    public Parameter createReferencingParameter(IElement element, String paramId, ReferenceType type) throws Exception {
        
        String value = generateValueForReferencingParamter(element.getId(), type);
        
        return new ReferencingParameter(paramId, value, element, type);
    }

    public String generateIdForReferencingParameter(String elementId, ReferenceType referenceType) {

        // TODO generate pattern for additional types
        switch (referenceType) {

            case SPEED:
                return elementId;

            case TOKEN:
                return elementId;

            default:
                return null;
        }
    }
    
    public String generateValueForReferencingParamter(String elementId, ReferenceType type) throws Exception {

        switch (type) {

            case TOKEN:
                return properties.getProperty("regex.place.token").replace(".+", elementId);
//                return "'" + elementId + "'.t";

            case SPEED:
                return properties.getProperty("regex.transition.speed").replace(".+", elementId);

            default:
                throw new Exception("Value generation for given reference type not yet implemented!");
        }
    }

    public String recoverElementIdFromReferencingParameterId(String paramId) {

        String elementId;

        // TODO use regex pattern detection for additional types
        elementId = paramId; // simplest and only yet implemented case, paramId matches elementId

        return elementId;
    }

    public ReferenceType recoverReferenceTypeFromParameterId(IElement element, String parameterId) {

        // TODO use regex pattern detection for additional types
        switch (element.getElementType()) {

            case PLACE:
                return ReferenceType.TOKEN;

            case TRANSITION:
                return ReferenceType.SPEED;

            default:
                return null;
        }
    }
    
    public ReferenceType recoverReferenceTypeFromParameterValue(String parameterValue) {
        
        if (parameterValue.matches(
                properties.getProperty("regex.arc.tokenIn.actual"))) {
            
            return null;
            
        } else if (parameterValue.matches(
                properties.getProperty("regex.arc.tokenIn.total"))) {
            
            return null;
            
        } else if (parameterValue.matches(
                properties.getProperty("regex.arc.tokenOut.actual"))) {
            
            return null;
            
        } else if (parameterValue.matches(
                properties.getProperty("regex.arc.tokenOut.total"))) {
            
            return null;
            
        } else if (parameterValue.matches(
                properties.getProperty("regex.place.token"))) {
            
            return ReferenceType.TOKEN;
            
        } else if (parameterValue.matches(
                properties.getProperty("regex.transition.speed"))) {
            
            return ReferenceType.SPEED;
            
        } else if (parameterValue.matches(
                properties.getProperty("regex.transition.fire"))) {
            
            return null;
            
        } else {
            
            return null;
        }
    }
}
