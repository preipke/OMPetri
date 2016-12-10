/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unibi.agbi.gnius.core.model;

import edu.unibi.agbi.petrinet.model.entity.PN_Element;

/**
 *
 * @author PR
 */
public class NodeSelectionChoice
{
    private final PN_Element.Type type;
    private final String typeName;

    public NodeSelectionChoice(PN_Element.Type type , String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public PN_Element.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
