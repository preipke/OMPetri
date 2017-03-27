/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pr
 */
public abstract class Element implements IElement {
    
    protected Type type;
    
    protected String id;
    protected List<String> filterNames;
    
    protected boolean isEnabled = true;
    
    private final List<Parameter> parameter;
    
    public Element() {
        parameter = new ArrayList();
    }
    
    @Override
    public void setFilterNames(List<String> names) {
        filterNames = names;
    }
    
    @Override
    public void addFilterName(String name) {
        filterNames.add(name);
    }
    
    @Override
    public List<String> getFilterNames() {
        return filterNames;
    }
    
    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    @Override
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
