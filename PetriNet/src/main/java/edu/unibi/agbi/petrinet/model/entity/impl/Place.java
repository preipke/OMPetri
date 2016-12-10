/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity.impl;


import edu.unibi.agbi.petrinet.model.Parameter;
import edu.unibi.agbi.petrinet.model.entity.PN_Node;
import edu.unibi.agbi.petrinet.model.entity.PN_Element;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public abstract class Place extends PN_Node
{
    private Type placeType;
    
    public Place() {
        
        type = PN_Element.Type.PLACE;
        
        getParameter().add(new Parameter("Token (Start)", null, null, Parameter.Type.COMPUTE));
        getParameter().add(new Parameter("Token (Minimum)", null, null, Parameter.Type.COMPUTE));
        getParameter().add(new Parameter("Token (Maximum)", null, null, Parameter.Type.COMPUTE));
    }
    
    public Type getPlaceType() {
        return placeType;
    }
    
    public enum Type {
        CONTINUOUS, DEFAULT, DISCRETE;
    }
}
