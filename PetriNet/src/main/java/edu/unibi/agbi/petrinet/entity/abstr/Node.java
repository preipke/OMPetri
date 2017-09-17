/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.petrinet.entity.abstr;

import edu.unibi.agbi.petrinet.entity.IArc;
import edu.unibi.agbi.petrinet.entity.INode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PR
 */
public abstract class Node extends Element implements INode
{
    protected boolean isConstant = false;

    protected final List<IArc> arcsIn;
    protected final List<IArc> arcsOut;

    public Node(String id, Type elementType) {
        super(id, elementType);
        arcsIn = new ArrayList();
        arcsOut = new ArrayList();
    }

    @Override
    public List<IArc> getArcsIn() {
        return arcsIn;
    }

    @Override
    public List<IArc> getArcsOut() {
        return arcsOut;
    }

    @Override
    public boolean isConstant() {
        return isConstant;
    }

    @Override
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }
}
