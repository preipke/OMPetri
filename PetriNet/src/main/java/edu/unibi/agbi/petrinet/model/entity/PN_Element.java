/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity;

import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pr
 */
public abstract class PN_Element {
    
    protected Type type;
    
    private static final String IDENT = "pn";
    private static int COUNT = 0;
    
    private final String id;
    
    private final List<Parameter> parameter;
    private final List<Object> shapes;
    
    public PN_Element() {
        
        synchronized (IDENT) {
            COUNT++;
            id = IDENT + COUNT;
        }
        
        shapes = new ArrayList();
        parameter = new ArrayList();
    }
    
    public List<Parameter> getParameter() {
        return parameter;
    }

    /**
     * @return the shapes
     */
    public List<Object> getShapes() {
        return shapes;
    }
    
    public String getId() {
        return id;
    }
    
    public Type getElementType() {
        return type;
    }
    
    public enum Type {
        ARC, PLACE, TRANSITION;
    }
}
