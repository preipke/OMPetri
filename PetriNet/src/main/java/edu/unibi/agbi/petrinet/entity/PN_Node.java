/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity;

import edu.unibi.agbi.petrinet.entity.impl.Arc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public abstract class PN_Node extends PN_Element implements IPN_Node
{
    private static final String IDENT = "pn";
    private static int COUNT = 0;
    
    private boolean isEnabled = true;
    
    private final List<Arc> arcsIn;
    private final List<Arc> arcsOut;
    
    public PN_Node() {
        super();
        
        synchronized (IDENT) {
            COUNT++;
            id = IDENT + COUNT;
        }
        
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
