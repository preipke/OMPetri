/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author pr
 */
public abstract class Element implements IElement
{
    protected String id;
//    protected String name;
    
    protected final Type elementType;
    
    protected boolean isDisabled = false;

    protected Map<String,Parameter> parametersLocal;
    protected Set<Parameter> parametersRelated;

    public Element(String id, Type elementType) {
        this.elementType = elementType;
        this.id = id;
        this.parametersRelated = new HashSet();
        this.parametersLocal = new HashMap();
    }

    @Override
    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    @Override
    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public Type getElementType() {
        return elementType;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public void setName(String name) {
//        this.name = name;
//    }

    @Override
    public Set<Parameter> getRelatedParameters() {
        return parametersRelated;
    }
    
    @Override
    public void addLocalParameter(Parameter param) {
        parametersLocal.put(param.getId(), param);
    }
    
    @Override
    public Parameter getLocalParameter(String id) {
        return parametersLocal.get(id);
    }
    
    @Override
    public Collection<Parameter> getLocalParameters() {
        return parametersLocal.values();
    }
    
//    @Override
//    public String toString() {
//        return name + " (" + id + ")";
//    }
    
    @Override
    public String toString() {
        return id;
    }

    public enum Type
    {
        ARC, PLACE, TRANSITION;
    }
}
