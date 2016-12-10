/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.model.entity;

import edu.unibi.agbi.petrinet.model.entity.impl.Arc;
import edu.unibi.agbi.petrinet.model.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public abstract class PN_Node extends PN_Element
{
    private boolean isEnabled = true;
    
    private final List<Arc> arcsIn;
    private final List<Arc> arcsOut;
    
    public PN_Node() {
        super();
        
        getParameter().add(new Parameter("Name", null, null, Parameter.Type.INFO));
        getParameter().add(new Parameter("Label", null, null, Parameter.Type.INFO));
        getParameter().add(new Parameter("Description", null, null, Parameter.Type.INFO));
        
        arcsIn = new ArrayList();
        arcsOut = new ArrayList();
    }

    public List<Arc> getArcsIn() {
        return arcsIn;
    }

    public List<Arc> getArcsOut() {
        return arcsOut;
    }

    public boolean isIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
