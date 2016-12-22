/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pr
 */
public abstract class PN_Element implements IPN_Element {
    
    protected Type type;
    protected String id;
    
    private final List<Parameter> parameter;
    
    public PN_Element() {
        parameter = new ArrayList();
    }
    
    public List<Parameter> getParameter() {
        return parameter;
    }
    
    public Type getElementType() {
        return type;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    public enum Type {
        ARC, PLACE, TRANSITION;
    }
}
