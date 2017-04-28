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
    private boolean isConstant = true;

    private final List<IArc> arcsIn;
    private final List<IArc> arcsOut;

    public Node(String internalId) {

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
    public List<IArc> getArcsIn() {
        return arcsIn;
    }

    @Override
    public List<IArc> getArcsOut() {
        return arcsOut;
    }
}
