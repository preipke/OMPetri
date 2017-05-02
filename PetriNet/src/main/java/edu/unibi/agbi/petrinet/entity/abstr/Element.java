/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IElement;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.HashMap;
import java.util.Map;
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
    protected final Map<String, Parameter> parameter;

    public Element() {
        filter = new TreeSet();
        parameter = new HashMap();
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
    public Map<String, Parameter> getParameters() {
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

    public enum Type
    {
        ARC, CLUSTER, CLUSTERARC, PLACE, TRANSITION;
    }
}
