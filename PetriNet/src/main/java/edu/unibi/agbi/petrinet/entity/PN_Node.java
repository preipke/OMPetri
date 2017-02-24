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
    private boolean isConstant = true;
    
    private final List<IPN_Arc> arcsIn;
    private final List<IPN_Arc> arcsOut;
    
    public PN_Node(String internalId) {
        
        id = internalId;
        
        arcsIn = new ArrayList();
        arcsOut = new ArrayList();
    }

    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }
    
    @Override
    public boolean isConstant() {
        return isConstant;
    }

    @Override
    public List<IPN_Arc> getArcsIn() {
        return arcsIn;
    }

    @Override
    public List<IPN_Arc> getArcsOut() {
        return arcsOut;
    }
}
