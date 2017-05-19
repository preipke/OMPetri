/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IElement;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pr
 */
public abstract class Element implements IElement
{
    protected Type type;

    protected String id;
    protected String name;

    protected boolean isEnabled = true;

    protected final Set<String> filter;
    protected final Set<String> parameter;

    public Element() {
        filter = new TreeSet();
        parameter = new TreeSet();
    }

    @Override
    public Type getElementType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<String> getFilter() {
        return filter;
    }

    @Override
    public Set<String> getRelatedParameterIds() {
        return parameter;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    @Override
    public String toString() {
        return "(" + id + ") " + name;
    }

    public enum Type
    {
        ARC, CLUSTER, CLUSTERARC, PLACE, TRANSITION;
    }
}
