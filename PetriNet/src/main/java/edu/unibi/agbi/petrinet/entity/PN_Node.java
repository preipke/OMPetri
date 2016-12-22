/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.abstr.Arc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public abstract class PN_Node extends PN_Element implements IPN_Node
{
    private boolean enabled = true;
    
    private final List<Arc> arcsIn;
    private final List<Arc> arcsOut;
    
    public PN_Node(String internalId) {
        
        id = internalId;
        
        arcsIn = new ArrayList();
        arcsOut = new ArrayList();
    }

    @Override
    public List<Arc> getArcsIn() {
        return arcsIn;
    }

    @Override
    public List<Arc> getArcsOut() {
        return arcsOut;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.enabled = isEnabled;
    }
}
